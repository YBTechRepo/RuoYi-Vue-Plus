package org.dromara.insurance.domain.vo;

import org.dromara.insurance.domain.InsuranceProductDetail;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 保险产品-图文详情视图对象 biz_insurance_product_detail
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProductDetail.class)
public class InsuranceProductDetailVo implements Serializable {

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
     * 产品特点图 (JSON数组：["url1", "url2"])
     */
    //@ExcelProperty(value = "产品特点图 (JSON数组")
    private List<String> featureImages;

    /**
     * 理赔流程图 (JSON数组：["url1"])
     */
    //@ExcelProperty(value = "理赔流程图 (JSON数组")
    private List<String> claimImages;

    /**
     * 投保须知 (JSON对象数组：[{"title":"", "content":"", "sort":1}])
     */
    //@ExcelProperty(value = "投保须知 (JSON对象数组)")
    private List<InsuranceProductDetail.NoticeItem> insureNotice;

    /**
     * 条款须知文件 (JSON对象数组：[{"clauseName":"", "fileUrl":"", "sort":1}])
     */
    //@ExcelProperty(value = "条款须知文件 (JSON对象数组)")
    private List<InsuranceProductDetail.ClauseItem> clauseFiles;

    /**
     * 理赔说明步骤
     */
    private List<InsuranceProductDetail.StepItem> claimInstructions;

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
