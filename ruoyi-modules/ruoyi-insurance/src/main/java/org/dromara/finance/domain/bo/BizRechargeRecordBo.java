package org.dromara.finance.domain.bo;

import org.dromara.finance.domain.BizRechargeRecord;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import org.dromara.common.translation.annotation.Translation;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.translation.constant.TransConstant;

/**
 * 充值申请业务对象 biz_recharge_record
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizRechargeRecord.class, reverseConvertGenerate = false)
public class BizRechargeRecordBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 充值单号
     */
    private String rechargeNo;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人账号
     */
    private String userName;

    /**
     * 申请人姓名
     */
    private String userNickName;

    /**
     * 用户填写的申请充值金额
     */
    private BigDecimal applyAmount;

    /**
     * 财务实际核准到账的金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付凭证(转账截图)URL
     */
    private String voucherImg;

    /**
     * 审核状态: 0=待审核, 1=已通过, 2=已驳回
     */
    private Integer status;

    /**
     * 审核/驳回备注说明
     */
    private String auditRemark;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    private Date createTime;
}
