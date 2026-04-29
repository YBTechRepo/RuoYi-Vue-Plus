package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 产品配置业务对象 biz_insurance_product
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceProductConfig.class, reverseConvertGenerate = false)
public class InsuranceProductConfigBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productName;

    /**
     * 保险公司
     */
    @NotBlank(message = "保险公司不能为空", groups = { AddGroup.class, EditGroup.class })
    private String companyCode;

    /**
     * 产品类型
     */
    @NotBlank(message = "产品类型不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "产品模式不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer productMode;

    /**
     * 最低保费
     */
    @NotNull(message = "最低保费不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal minPremium;

    /**
     * 投保链接
     */
    @NotBlank(message = "投保链接不能为空", groups = { AddGroup.class, EditGroup.class })
    private String proposalUrl;

    /**
     * 产品图片
     */
    @NotBlank(message = "产品图片不能为空", groups = { AddGroup.class, EditGroup.class })
    private String imgUrl;

    /**
     * 产品说明
     */
    @NotBlank(message = "产品说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String description;

    /**
     * 产品状态
     */
    @NotNull(message = "产品状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 产品排序
     */
    private Integer sort;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 乐观锁版本
     */
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
}
