package org.dromara.finance.domain.vo;

import org.dromara.common.translation.annotation.Translation;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.finance.domain.BizRechargeRecord;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 充值申请视图对象 biz_recharge_record
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizRechargeRecord.class)
public class BizRechargeRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户号
     */
    @ExcelProperty(value = "租户号")
    private String tenantId;

    /**
     * 租户名称
     */
    @ExcelProperty(value = "租户名称")
    private String tenantName;

    /**
     * id
     */
    //@ExcelProperty(value = "id")
    private Long id;

    /**
     * 充值单号
     */
    @ExcelProperty(value = "充值单号")
    private String rechargeNo;

    /**
     * 申请人ID
     */
    @ExcelProperty(value = "申请人ID")
    private Long userId;

    @ExcelProperty(value = "申请人账号")
    private String userName;

    @ExcelProperty(value = "申请人姓名")
    private String userNickName;

    /**
     * 用户填写的申请充值金额
     */
    @ExcelProperty(value = "用户填写的申请充值金额")
    private BigDecimal applyAmount;

    /**
     * 财务实际核准到账的金额
     */
    @ExcelProperty(value = "财务实际核准到账的金额")
    private BigDecimal actualAmount;

    /**
     * 支付凭证(转账截图)URL
     */
    @ExcelProperty(value = "支付凭证(转账截图)URL")
    private String voucherImg;

    /**
     * 支付凭证(转账截图)URLUrl
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "voucherImg")
    private String voucherImgUrl;
    /**
     * 审核状态: 0=待审核, 1=已通过, 2=已驳回
     */
    @ExcelProperty(value = "审核状态: 0=待审核, 1=已通过, 2=已驳回", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "finance_recharge_audit_status")
    private Integer status;

    /**
     * 审核/驳回备注说明
     */
    @ExcelProperty(value = "审核/驳回备注说明")
    private String auditRemark;

    @ExcelProperty(value = "申请时间")
    private Date createTime;

    /**
     * 审核人ID
     */
    @ExcelProperty(value = "审核人ID")
    private Long auditBy;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    /**
     * 乐观锁版本号
     */
    //@ExcelProperty(value = "乐观锁版本号")
    private Integer version;


}
