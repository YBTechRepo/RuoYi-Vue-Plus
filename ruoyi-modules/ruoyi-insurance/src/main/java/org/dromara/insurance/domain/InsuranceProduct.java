package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;

/**
 * 产品配置对象 biz_insurance_product
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product")
public class InsuranceProduct extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 保险公司编码
     */
    private String companyCode;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 产品模式
     */
    private Long productMode;

    /**
     * 最低保费
     */
    private Long minPremium;

    /**
     * 投保链接
     */
    private String proposalUrl;

    /**
     * 产品图片
     */
    private String imgUrl;

    /**
     * 产品说明
     */
    private String description;

    /**
     * 产品状态
     */
    private Long status;

    /**
     * 产品排序
     */
    private Long sort;

    /**
     * 删除标识
     */
    private Long deletedFlag;


}
