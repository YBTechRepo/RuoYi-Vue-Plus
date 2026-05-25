package org.dromara.insurance.listener;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.insurance.domain.dto.BatchInsuredImportDto;
import org.dromara.insurance.utils.DynamicInsureFieldUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 批量投保导入监听器
 */
@Slf4j
public class BatchInsuredImportListener extends AnalysisEventListener<BatchInsuredImportDto> {

    @Getter
    private final List<BatchInsuredImportDto> resultList = new ArrayList<>();

    @Getter
    private int validCount = 0;

    @Getter
    private int invalidCount = 0;

    private final List<DynamicInsureFieldUtils.Field> dynamicFields;

    public BatchInsuredImportListener() {
        this(Collections.emptyList());
    }

    public BatchInsuredImportListener(List<DynamicInsureFieldUtils.Field> dynamicFields) {
        this.dynamicFields = dynamicFields == null ? Collections.emptyList() : dynamicFields;
    }

    @Override
    public void invoke(BatchInsuredImportDto data, AnalysisContext context) {
        try {
            // 1. 触发 JSR303 强校验 (基于 @NotBlank, @Length, @Pattern 等)
            ValidatorUtils.validate(data);
            validateDynamicFields(data);

            // 2. 如果顺利走通，则记录为成功数据
            data.setIsValid(true);
            validCount++;

        } catch (ConstraintViolationException e) {
            // 3. 拦截到错误，抽取错误的具体字段与提示文本
            data.setIsValid(false);
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();

            for (ConstraintViolation<?> item : violations) {
                // 此时 propertyPath 就是校验失败的字段名，如 "appCertNo"
                String errorField = item.getPropertyPath().toString();
                String errorMsg = item.getMessage();
                // 存入 Map 投递给前端用作表单红框渲染
                data.getErrors().put(errorField, errorMsg);
            }
            invalidCount++;
        } catch (IllegalArgumentException e) {
            data.setIsValid(false);
            if (data.getErrors().isEmpty()) {
                data.getErrors().put("extraData", e.getMessage());
            }
            invalidCount++;
        } catch (Exception e) {
            // 处理其他解析层面的未知异常
            data.setIsValid(false);
            data.getErrors().put("systemError", "数据解析异常：" + e.getMessage());
            invalidCount++;
        }

        // 统一添加到汇总集合
        resultList.add(data);
    }

    private void validateDynamicFields(BatchInsuredImportDto data) {
        for (DynamicInsureFieldUtils.Field field : dynamicFields) {
            String error = DynamicInsureFieldUtils.validateValue(field, data.getExtraData().get(field.getKey()));
            if (org.dromara.common.core.utils.StringUtils.isNotBlank(error)) {
                data.getErrors().put("extraData." + field.getKey(), error);
                throw new IllegalArgumentException(error);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成，共解析 [{}] 条数据，成功 [{}] 条，失败 [{}] 条",
            resultList.size(), validCount, invalidCount);
    }
}

