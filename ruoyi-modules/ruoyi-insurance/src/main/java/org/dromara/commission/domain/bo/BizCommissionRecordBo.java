package org.dromara.commission.domain.bo;

import org.dromara.commission.domain.BizCommissionRecord;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 佣金分配明细业务对象 biz_commission_record
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizCommissionRecord.class, reverseConvertGenerate = false)
public class BizCommissionRecordBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联保单ID
     */
    @NotNull(message = "关联保单ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long policyId;

    /**
     * 保单号
     */
    @NotBlank(message = "保单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String policyNo;

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long productId;

    /**
     * 佣金计算基数
     */
    @NotNull(message = "佣金计算基数不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal commissionBase;

    /**
     * 业务员ID
     */
    @NotNull(message = "业务员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long salesUserId;

    /**
     * 业务员实发金额
     */
    @NotNull(message = "业务员实发金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salesAmount;

    /**
     * 团队长ID
     */
    @NotNull(message = "团队长ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long teamUserId;

    /**
     * 团队长实发金额
     */
    @NotNull(message = "团队长实发金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal teamAmount;

    /**
     * 总负责人ID
     */
    @NotNull(message = "总负责人ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long projectUserId;

    /**
     * 总负责人实发金额
     */
    @NotNull(message = "总负责人实发金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal projectAmount;

    /**
     * 算账依据
     */
    @NotNull(message = "算账依据不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer calcStrategy;

    /**
     * 流水状态
     */
    @NotNull(message = "流水状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 删除标志
     */
    private String delFlag;

    /**
     * 业务佣金比例
     */
    @NotNull(message = "业务佣金比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salesRatio;

    /**
     * 团队佣金比例
     */
    @NotNull(message = "团队佣金比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal teamRatio;

    /**
     * 总负责人佣金比例
     */
    @NotNull(message = "总负责人佣金比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal projectRatio;

    /**
     * 业务员姓名
     */
    @NotBlank(message = "业务员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String salesUserName;

    /**
     * 团队负责人姓名
     */
    @NotBlank(message = "团队负责人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String teamUserName;

    /**
     * 总负责人姓名
     */
    @NotBlank(message = "总负责人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectUserName;


}
