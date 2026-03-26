package org.dromara.commission.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AppCommissionItemVo {
    private String policyNo;          // 保单号
    private String productName;       // 产品名称
    private BigDecimal commissionBase;// 保费基数(实交保费)
    private BigDecimal ratio;         // 结算比例 (后端已抹平映射)
    private BigDecimal amount;        // 预计入账金额 (后端已抹平映射)
    private Integer status;           // 状态: 0-待生效, 1-已结算
    private Date createTime;          // 出单时间
}
