package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceProductDetail;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 保险产品-图文详情业务对象 biz_insurance_product_detail
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceProductDetail.class, reverseConvertGenerate = false)
public class InsuranceProductDetailBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 产品ID (关联 biz_insurance_product.id)
     */
    private Long productId;

    /**
     * 产品特点图 (JSON数组：["url1", "url2"])
     */
    private List<String> featureImages;

    /**
     * 理赔流程图 (JSON数组：["url1"])
     */
    private List<String> claimImages;

    /**
     * 投保须知 (JSON对象数组：[{"title":"", "content":"", "sort":1}])
     */
    private List<InsuranceProductDetail.NoticeItem> insureNotice;

    /**
     * 条款须知文件 (JSON对象数组：[{"clauseName":"", "fileUrl":"", "sort":1}])
     */
    private List<InsuranceProductDetail.ClauseItem> clauseFiles;

    /**
     * 理赔说明步骤
     */
    private List<InsuranceProductDetail.StepItem> claimInstructions;

    /**
     * 乐观锁版本
     */
    private Long version;

    /**
     * 删除标记(0-未删除 1-删除)
     */
    private String delFlag;
}
