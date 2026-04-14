package org.dromara.insurance.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BatchPreviewVO {
    private String productName;
    private BigDecimal grossPremium;// 单人毛保费
    private BigDecimal commissionRate;// 佣金比例
    private BigDecimal netPremium;// 单人净保费（后端计算）
    private Integer validCount;// 有效人数
    private BigDecimal totalAmount;// 应付总额（后端计算 = netPremium × validCount）
    private BigDecimal walletBalance;// 当前钱包余额（后端查询）
    private Boolean isBalanceSufficient;// 余额是否充足（后端判断）
}
