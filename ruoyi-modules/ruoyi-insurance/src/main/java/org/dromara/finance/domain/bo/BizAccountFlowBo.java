package org.dromara.finance.domain.bo;

import org.dromara.finance.domain.BizAccountFlow;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户明细业务对象 biz_account_flow
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizAccountFlow.class, reverseConvertGenerate = false)
public class BizAccountFlowBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 资金归属的用户ID
     */
    private Long userId;

    private String userName;

    private String userNickName;

    /**
     * 流水类型
     */
    private Integer flowType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    private BigDecimal balanceBefore;

    /**
     * 变动后的账户总余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号
     */
    private String bizNo;

    /**
     * 流水摘要说明
     */
    private String remark;

    private Date createTime;
}
