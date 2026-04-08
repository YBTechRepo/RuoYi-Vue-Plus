package org.dromara.insurance.domain.vo;

import org.dromara.insurance.domain.InsuranceProductLiability;
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
 * 保险产品-保障责任视图对象 biz_insurance_product_liability
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProductLiability.class)
public class InsuranceProductLiabilityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    //@ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 产品ID (关联 biz_insurance_product.id)
     */
    //@ExcelProperty(value = "产品ID (关联 biz_insurance_product.id)")
    private Long productId;

    /**
     * 责任名称 (如：意外身故/伤残)
     */
    //@ExcelProperty(value = "责任名称 (如：意外身故/伤残)")
    private String liabilityName;

    /**
     * 保障额度说明 (如：50万、按比例赔付)
     */
    //@ExcelProperty(value = "保障额度说明 (如：50万、按比例赔付)")
    private String insuredAmountDesc;

    /**
     * 详情描述 (选填的补充说明)
     */
    //@ExcelProperty(value = "详情描述 (选填的补充说明)")
    private String description;

    /**
     * 排序号 (升序)
     */
    //@ExcelProperty(value = "排序号 (升序)")
    private Long sort;

    /**
     * 乐观锁版本
     */
    //@ExcelProperty(value = "乐观锁版本")
    private Long version;

    /**
     * 删除标记(0-未删除 1-删除)
     */
    //@ExcelProperty(value = "删除标记(0-未删除 1-删除)")
    private String delFlag;


}
