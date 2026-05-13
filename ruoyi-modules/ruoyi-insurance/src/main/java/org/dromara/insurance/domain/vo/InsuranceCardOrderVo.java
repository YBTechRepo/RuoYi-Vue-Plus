package org.dromara.insurance.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.insurance.domain.InsuranceCardOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡密订单视图对象 biz_insurance_card_order
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceCardOrder.class)
public class InsuranceCardOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ExcelProperty(value = "租户编号")
    private String tenantId;

    @ExcelProperty(value = "租户名称")
    private String tenantName;

    @ExcelProperty(value = "订单号")
    private String orderNo;

    private Long productId;

    @ExcelProperty(value = "产品编码")
    private String productCode;

    @ExcelProperty(value = "产品名称")
    private String productName;

    @ExcelProperty(value = "业务员姓名")
    private String agentName;

    private Long agentUserId;
    private Long agentDeptId;

    @ExcelProperty(value = "联系人")
    private String customerName;

    @ExcelProperty(value = "联系人手机号")
    private String customerMobile;

    @ExcelProperty(value = "订单状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_card_order_status")
    private Integer status;

    @ExcelProperty(value = "产品模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_mode")
    private Integer productMode;

    private Integer insureMode;
    private Integer paymentMode;
    private Date payTime;
    private String specId;

    @ExcelProperty(value = "卡密规格")
    private String specName;

    @ExcelProperty(value = "购买数量")
    private Integer goodsQuantity;

    @ExcelProperty(value = "商品金额")
    private BigDecimal goodsAmount;

    @ExcelProperty(value = "运费金额")
    private BigDecimal freightAmount;

    @ExcelProperty(value = "运费支付方式")
    private String freightPayType;

    @ExcelProperty(value = "应付金额")
    private BigDecimal payAmount;

    @ExcelProperty(value = "选择保险公司", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_card_company")
    private String selectedCompanyCode;

    @ExcelProperty(value = "收货人姓名")
    private String receiverName;

    @ExcelProperty(value = "收货人手机号")
    private String receiverMobile;

    @ExcelProperty(value = "收货地址")
    private String receiverAddress;

    @ExcelProperty(value = "快递公司")
    private String expressCompany;

    @ExcelProperty(value = "快递单号")
    private String expressNo;

    @ExcelProperty(value = "发货时间")
    private Date deliveryTime;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    private String delFlag;
    private Integer version;
}



