package org.dromara.insurance.service;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("dev")
class SignedBatchTemplateDescriptorTest {

    @BeforeAll
    static void initializeJsonUtils() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("objectMapper", new ObjectMapper());
        new SpringUtil().postProcessBeanFactory(beanFactory);
    }

    @Test
    void containsApplicationFieldsAndSortedProductExtensionFields() throws Exception {
        ApplicationFormGuard guard = mock(ApplicationFormGuard.class);
        InsuranceApplicationFormService forms = mock(InsuranceApplicationFormService.class);
        IInsuranceProductConfigService products = mock(IInsuranceProductConfigService.class);
        ObjectMapper json = new ObjectMapper();
        SignedBatchTemplateDescriptor service = new SignedBatchTemplateDescriptor(guard, forms, products, json);

        InsuranceProductConfig product = signedProduct();
        when(guard.product(1L)).thenReturn(product);
        when(forms.metadata("form-a", "v3")).thenReturn(json.readTree("""
            {"fields":[
              {"key":"postcode","label":"邮政编码","type":"digit","required":true},
              {"key":"gender","label":"性别","type":"select","required":true,
               "options":[{"label":"男","value":"M"},{"label":"女","value":"F"}]}
            ]}
            """));
        InsuranceProductConfigBo productBo = new InsuranceProductConfigBo();
        productBo.setInsureFormSchema("""
            [
              {"key":"later","label":"后字段","type":"text","required":false,"sort":20},
              {"key":"first","label":"先字段","type":"checkbox","required":true,"sort":10,
               "options":[{"label":"甲","value":"A"},{"label":"乙","value":"B"}]}
            ]
            """);
        InsuranceProductSaveBo full = new InsuranceProductSaveBo();
        full.setProduct(productBo);
        when(products.getProductFull(1L)).thenReturn(full);

        var descriptor = service.describe(1L);
        assertEquals(List.of("postcode", "gender"),
            descriptor.getApplicationFields().stream().map(SignedBatchTemplateDescriptor.ApplicationField::key).toList());
        assertEquals(List.of("first", "later"),
            descriptor.getDynamicFields().stream().map(item -> item.getKey()).toList());
        assertEquals(List.of("M-男", "F-女"), descriptor.getApplicationFields().get(1).options());
        assertEquals(SignedBatchTemplateDescriptor.MODE, descriptor.metadata().get("templateMode"));
        assertFalse(descriptor.getSchemaHash().isBlank());
    }

    @Test
    void ordinaryProductCannotUseSignedDescriptor() {
        ApplicationFormGuard guard = mock(ApplicationFormGuard.class);
        InsuranceProductConfig product = new InsuranceProductConfig();
        product.setApplicationFormRequired(false);
        when(guard.product(1L)).thenReturn(product);
        SignedBatchTemplateDescriptor service = new SignedBatchTemplateDescriptor(guard,
            mock(InsuranceApplicationFormService.class), mock(IInsuranceProductConfigService.class), new ObjectMapper());
        assertThrows(ServiceException.class, () -> service.describe(1L));
    }

    private InsuranceProductConfig signedProduct() {
        InsuranceProductConfig product = new InsuranceProductConfig();
        product.setId(1L);
        product.setApplicationFormRequired(true);
        product.setApplicationTemplateCode("form-a");
        product.setApplicationTemplateVersion("v3");
        return product;
    }
}
