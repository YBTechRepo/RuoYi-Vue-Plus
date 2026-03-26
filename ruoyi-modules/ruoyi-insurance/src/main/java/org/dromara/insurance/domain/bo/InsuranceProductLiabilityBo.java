package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceProductLiability;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 保险产品-保障责任业务对象 biz_insurance_product_liability
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceProductLiability.class, reverseConvertGenerate = false)
public class InsuranceProductLiabilityBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 产品ID (关联 biz_insurance_product.id)
     */
    private Long productId;

    /**
     * 责任名称 (如：意外身故/伤残)
     */
    private String liabilityName;

    /**
     * 保障额度说明 (如：50万、按比例赔付)
     */
    private String insuredAmountDesc;

    /**
     * 详情描述 (选填的补充说明)
     */
    private String description;

    /**
     * 排序号 (升序)
     */
    private Long sort;

    /**
     * 乐观锁版本
     */
    private Long version;

    /**
     * 删除标记(0-未删除 1-删除)
     */
    private String delFlag;


}
