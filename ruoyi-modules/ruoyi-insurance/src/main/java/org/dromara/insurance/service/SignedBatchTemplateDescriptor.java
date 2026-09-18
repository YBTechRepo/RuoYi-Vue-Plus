package org.dromara.insurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.insurance.utils.DynamicInsureFieldUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 签字产品专用批量模板的后端描述；模板模式不接受前端指定。 */
@Service
@RequiredArgsConstructor
public class SignedBatchTemplateDescriptor {
    public static final String MODE = "SIGNED_APPLICATION";
    public static final String VERSION = "1";
    public static final String META_SHEET = "__batch_meta";

    private final ApplicationFormGuard guard;
    private final InsuranceApplicationFormService applicationFormService;
    private final IInsuranceProductConfigService productConfigService;
    private final ObjectMapper json;

    public Descriptor describe(Long productId) {
        InsuranceProductConfig product = guard.product(productId);
        if (product == null) throw new ServiceException("产品不存在");
        if (!Boolean.TRUE.equals(product.getApplicationFormRequired())) {
            throw new ServiceException("该产品使用普通批量投保模板");
        }
        if (StringUtils.isBlank(product.getApplicationTemplateCode())
            || StringUtils.isBlank(product.getApplicationTemplateVersion())) {
            throw new ServiceException("产品未配置投保单模板版本");
        }

        JsonNode metadata = applicationFormService.metadata(
            product.getApplicationTemplateCode(), product.getApplicationTemplateVersion());
        List<ApplicationField> applicationFields = new ArrayList<>();
        for (JsonNode field : metadata.path("fields")) {
            List<String> options = new ArrayList<>();
            for (JsonNode option : field.path("options")) {
                String label = option.path("label").asText();
                String value = option.path("value").asText();
                options.add(value.equals(label) ? value : value + "-" + label);
            }
            applicationFields.add(new ApplicationField(
                field.path("key").asText(), field.path("label").asText(),
                field.path("type").asText("text"), field.path("required").asBoolean(false), options));
        }

        InsuranceProductSaveBo productData = productConfigService.getProductFull(productId);
        String schema = productData == null || productData.getProduct() == null
            ? null : productData.getProduct().getInsureFormSchema();
        List<DynamicInsureFieldUtils.Field> dynamicFields = DynamicInsureFieldUtils.parseSchema(schema);

        Map<String, Object> digestSource = new LinkedHashMap<>();
        digestSource.put("mode", MODE);
        digestSource.put("version", VERSION);
        digestSource.put("applicationTemplateCode", product.getApplicationTemplateCode());
        digestSource.put("applicationTemplateVersion", product.getApplicationTemplateVersion());
        digestSource.put("applicationFields", applicationFields);
        digestSource.put("dynamicFields", dynamicFields);
        try {
            String schemaHash = ApplicationFormTemplate.hash(
                json.writeValueAsString(digestSource).getBytes(StandardCharsets.UTF_8));
            return new Descriptor(product, applicationFields, dynamicFields, schemaHash);
        } catch (Exception e) {
            throw new ServiceException("批量模板字段摘要计算失败");
        }
    }

    public record ApplicationField(String key, String label, String type, boolean required, List<String> options) {}

    @Getter
    public static final class Descriptor {
        private final InsuranceProductConfig product;
        private final List<ApplicationField> applicationFields;
        private final List<DynamicInsureFieldUtils.Field> dynamicFields;
        private final String schemaHash;

        private Descriptor(InsuranceProductConfig product, List<ApplicationField> applicationFields,
                           List<DynamicInsureFieldUtils.Field> dynamicFields, String schemaHash) {
            this.product = product;
            this.applicationFields = Collections.unmodifiableList(applicationFields);
            this.dynamicFields = Collections.unmodifiableList(dynamicFields);
            this.schemaHash = schemaHash;
        }

        public Map<String, String> metadata() {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("templateMode", MODE);
            values.put("templateVersion", VERSION);
            values.put("schemaHash", schemaHash);
            values.put("applicationTemplateCode", product.getApplicationTemplateCode());
            values.put("applicationTemplateVersion", product.getApplicationTemplateVersion());
            return values;
        }
    }
}
