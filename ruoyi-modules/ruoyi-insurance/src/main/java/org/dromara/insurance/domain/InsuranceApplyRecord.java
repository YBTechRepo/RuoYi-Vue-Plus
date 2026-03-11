package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 投保记录对象 biz_insurance_apply_record
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_apply_record")
public class InsuranceApplyRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 业务员姓名
     */
    private String agentName;

    /**
     * 业务员ID
     */
    private Long agentUserId;

    /**
     * 所属机构ID
     */
    private Long agentDeptId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户手机号
     */
    private String customerMobile;

    /**
     * 登记保费
     */
    private Long premium;

    /**
     * 订单状态
     */
    private Long status;

    /**
     * 结算状态
     */
    private Long commissionStatus;

    /**
     * 删除标识
     */
    @TableLogic
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @Version
    private Long version;


}
