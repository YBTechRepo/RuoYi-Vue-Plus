package org.dromara.commission.domain.bo;

import org.dromara.commission.domain.BizCommissionProduct;
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
 * 特殊产品费率配置业务对象 biz_commission_product
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizCommissionProduct.class, reverseConvertGenerate = false)
public class BizCommissionProductBo extends BaseEntity {

    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
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
     * 项目负责人比例
     */
    @NotNull(message = "项目负责人比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal projectRatio;

    /**
     * 团队负责人比例
     */
    @NotNull(message = "团队负责人比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal teamRatio;

    /**
     * 业务员比例
     */
    @NotNull(message = "业务员比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salesRatio;

    /**
     * 规则策略
     */
    @NotNull(message = "规则策略不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer ruleStrategy;

    /**
     * 生效开始时间
     */
    @NotNull(message = "生效开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date effectiveStart;

    /**
     * 生效结束时间
     */
    @NotNull(message = "生效结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date effectiveEnd;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
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
