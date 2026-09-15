package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 渠道回调产品编码映射对象 biz_insurance_product_channel_mapping
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product_channel_mapping")
public class InsuranceProductChannelMapping extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 平台产品ID
     */
    private Long productId;

    /**
     * 回调中的保险公司/渠道类型
     */
    private String companyType;

    /**
     * 回调中的产品编码
     */
    private String sourceProductCode;

    /**
     * 回调中的产品名称，仅用于配置展示
     */
    private String sourceProductName;

    @Version
    private Integer version;
}
