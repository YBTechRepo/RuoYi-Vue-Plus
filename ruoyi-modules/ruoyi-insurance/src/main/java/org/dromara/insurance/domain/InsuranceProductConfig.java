package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.insurance.domain.bo.ServiceFeeConfig;

import java.math.BigDecimal;
import java.io.Serial;
import java.util.List;

/**
 * 产品配置对象 biz_insurance_product
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product")
public class InsuranceProductConfig extends TenantEntity {

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
     * 保险公司
     */
    private String companyCode;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属分类名称
     */
    private String categoryName;

    /**
     * 营销标签
     */
    private String marketingTags;

    /**
     * 产品模式
     */
    private Integer productMode;

    /**
     * 最低保费
     */
    private BigDecimal minPremium;

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
    private Integer status;

    /**
     * 产品排序
     */
    private Integer sort;

    /**
     * 删除标识
     */
    @TableLogic
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @Version
    private Integer version;

    /**
     * 产品特点 (逗号拼接)
     */
    private String productFeatures;

    /**
     * 服务费配置 (JSON数组)
     */
    private String serviceFeeConfig;

    private Integer insureMode;

    private Integer paymentMode;

    /**
     * 运费支付方式：prepaid-线上支付，collect-到付
     */
    private String freightPayType;

    /**
     * 运费金额
     */
    private BigDecimal freight;

//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private List<ServiceFeeConfig> serviceFeeConfig;
}
