package org.dromara.commission.domain.vo;

import java.math.BigDecimal;
import org.dromara.commission.domain.BizCommissionRecord;
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
 * 佣金分配明细视图对象 biz_commission_record
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizCommissionRecord.class)
public class BizCommissionRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 关联保单ID
     */
    @ExcelProperty(value = "关联保单ID")
    private Long policyId;

    /**
     * 保单号
     */
    @ExcelProperty(value = "保单号")
    private String policyNo;

    /**
     * 产品ID
     */
    @ExcelProperty(value = "产品ID")
    private Long productId;

    /**
     * 佣金计算基数
     */
    @ExcelProperty(value = "佣金计算基数")
    private BigDecimal commissionBase;

    /**
     * 业务员ID
     */
    @ExcelProperty(value = "业务员ID")
    private Long salesUserId;

    /**
     * 业务员实发金额
     */
    @ExcelProperty(value = "业务员实发金额")
    private BigDecimal salesAmount;

    /**
     * 团队长ID
     */
    @ExcelProperty(value = "团队长ID")
    private Long teamUserId;

    /**
     * 团队长实发金额
     */
    @ExcelProperty(value = "团队长实发金额")
    private BigDecimal teamAmount;

    /**
     * 总负责人ID
     */
    @ExcelProperty(value = "总负责人ID")
    private Long projectUserId;

    /**
     * 总负责人实发金额
     */
    @ExcelProperty(value = "总负责人实发金额")
    private BigDecimal projectAmount;

    /**
     * 算账依据
     */
    @ExcelProperty(value = "算账依据", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "commission_calc_strategy")
    private Integer calcStrategy;

    /**
     * 流水状态
     */
    @ExcelProperty(value = "流水状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_policy_status")
    private Integer status;

    /**
     * 乐观锁
     */
    @ExcelProperty(value = "乐观锁")
    private Integer version;

    /**
     * 删除标志
     */
    @ExcelProperty(value = "删除标志")
    private String delFlag;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 业务佣金比例
     */
    @ExcelProperty(value = "业务佣金比例")
    private BigDecimal salesRatio;

    /**
     * 团队佣金比例
     */
    @ExcelProperty(value = "团队佣金比例")
    private BigDecimal teamRatio;

    /**
     * 总负责人佣金比例
     */
    @ExcelProperty(value = "总负责人佣金比例")
    private BigDecimal projectRatio;

    /**
     * 业务员姓名
     */
    @ExcelProperty(value = "业务员姓名")
    private String salesUserName;

    /**
     * 团队负责人姓名
     */
    @ExcelProperty(value = "团队负责人姓名")
    private String teamUserName;

    /**
     * 总负责人姓名
     */
    @ExcelProperty(value = "总负责人姓名")
    private String projectUserName;

    /**
     * 业务员佣金发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer salesStatus;

    /**
     * 团队长津贴发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer teamStatus;

    /**
     * 总监津贴发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer projectStatus;


}
