package org.dromara.finance.domain.vo;

import org.dromara.finance.domain.BizAccountFlow;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 账户明细视图对象 biz_account_flow
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizAccountFlow.class)
public class BizAccountFlowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 资金归属的用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    @ExcelProperty(value = "用户账号")
    private String userName;

    @ExcelProperty(value = "用户姓名")
    private String userNickName;

    /**
     * 流水类型
     */
    @ExcelProperty(value = "流水类型")
    @ExcelDictFormat(dictType = "finance_account_flow_type")
    private Integer flowType;

    /**
     * 变动金额
     */
    @ExcelProperty(value = "变动金额")
    private BigDecimal amount;

    @ExcelProperty(value = "变动前的账户总余额")
    private BigDecimal balanceBefore;

    /**
     * 变动后的账户总余额
     */
    @ExcelProperty(value = "变动后的账户总余额")
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号
     */
    @ExcelProperty(value = "关联业务单号")
    private String bizNo;

    /**
     * 流水摘要说明
     */
    @ExcelProperty(value = "流水摘要说明")
    private String remark;

    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
