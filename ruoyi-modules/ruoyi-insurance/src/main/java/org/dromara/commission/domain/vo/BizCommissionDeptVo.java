package org.dromara.commission.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.commission.domain.BizCommissionDept;
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
 * 机构费率配置视图对象 biz_commission_dept
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizCommissionDept.class)
public class BizCommissionDeptVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 部门ID
     */
    @ExcelProperty(value = "部门ID")
    private Long deptId;

    /**
     * 部门名称
     */
    @ExcelProperty(value = "部门名称")
    private String deptName;

    /**
     * 项目负责人比例
     */
    @ExcelProperty(value = "项目负责人比例")
    private BigDecimal projectRatio;

    /**
     * 团队负责人比例
     */
    @ExcelProperty(value = "团队负责人比例")
    private BigDecimal teamRatio;

    /**
     * 业务员比例
     */
    @ExcelProperty(value = "业务员比例")
    private BigDecimal salesRatio;

    /**
     * 生效开始时间
     */
    @ExcelProperty(value = "生效开始时间")
    private Date effectiveStart;

    /**
     * 生效结束时间
     */
    @ExcelProperty(value = "生效结束时间")
    private Date effectiveEnd;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_commission_status")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @ExcelProperty(value = "乐观锁版本号")
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
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;


}
