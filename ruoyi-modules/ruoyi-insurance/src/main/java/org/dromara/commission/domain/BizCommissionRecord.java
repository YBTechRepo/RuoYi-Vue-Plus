package org.dromara.commission.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

import java.io.Serial;

/**
 * 佣金分配明细对象 biz_commission_record
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_commission_record")
public class BizCommissionRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联保单ID
     */
    private Long policyId;

    /**
     * 保单号
     */
    private String policyNo;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 佣金计算基数
     */
    private BigDecimal commissionBase;

    /**
     * 业务员ID
     */
    private Long salesUserId;

    /**
     * 业务员实发金额
     */
    private BigDecimal salesAmount;

    /**
     * 团队长ID
     */
    private Long teamUserId;

    /**
     * 团队长实发金额
     */
    private BigDecimal teamAmount;

    /**
     * 总负责人ID
     */
    private Long projectUserId;

    /**
     * 总负责人实发金额
     */
    private BigDecimal projectAmount;

    /**
     * 算账依据
     */
    private Integer calcStrategy;

    /**
     * 流水状态
     */
    private Integer status;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;

    /**
     * 业务佣金比例
     */
    private BigDecimal salesRatio;

    /**
     * 团队佣金比例
     */
    private BigDecimal teamRatio;

    /**
     * 总负责人佣金比例
     */
    private BigDecimal projectRatio;

    /**
     * 业务员姓名
     */
    private String salesUserName;

    /**
     * 团队负责人姓名
     */
    private String teamUserName;

    /**
     * 总负责人姓名
     */
    private String projectUserName;


}
