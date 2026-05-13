package org.dromara.finance.domain.vo;

import org.dromara.finance.domain.BizUserAccount;
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
 * 账户信息视图对象 biz_user_account
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizUserAccount.class)
public class BizUserAccountVo implements Serializable {

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
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 用户编号
     */
    @ExcelProperty(value = "用户编号")
    private String userName;

    /**
     * 用户姓名
     */
    @ExcelProperty(value = "用户姓名")
    private String userNickName;

    /**
     * 可用充值余额
     */
    @ExcelProperty(value = "可用充值余额")
    private BigDecimal balance;

    /**
     * 账户状态
     */
    @ExcelProperty(value = "账户状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "finance_account_status")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @ExcelProperty(value = "乐观锁版本号")
    private Integer version;


}
