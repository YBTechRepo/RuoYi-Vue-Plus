package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 佣金配置业务对象 biz_insurance_product_commission
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceProductCommission.class, reverseConvertGenerate = false)
public class InsuranceProductCommissionBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long productId;

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
     * 基础佣金比例
     */
    @NotNull(message = "基础佣金比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal commissionRate;

    /**
     * 费率生效时间
     */
    @NotNull(message = "费率生效时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date effectiveTime;

    /**
     * 费率失效时间
     */
    @NotNull(message = "费率失效时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date expirationTime;

    /**
     * 启用状态
     */
    @NotNull(message = "启用状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 删除标志
     */
    private String delFlag;


}
