package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 保险产品-图文详情对象 biz_insurance_product_detail
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "biz_insurance_product_detail", autoResultMap = true)
public class InsuranceProductDetail extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 产品ID (关联 biz_insurance_product.id)
     */
    private Long productId;

    /**
     * 产品特点图 (JSON数组：["url1", "url2"])
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> featureImages;

    /**
     * 理赔流程图 (JSON数组：["url1"])
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> claimImages;

    /**
     * 朋友圈营销文案
     */
    private String marketingCopy;

    /**
     * 朋友圈营销素材图片 (JSON数组：["url1", "url2"])
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> marketingImages;

    /**
     * 投保须知 (JSON对象数组：[{"title":"", "content":"", "sort":1}])
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<NoticeItem> insureNotice;

    /**
     * 条款须知文件 (JSON对象数组：[{"clauseName":"", "fileUrl":"", "sort":1}])
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ClauseItem> clauseFiles;

    /**
     * 理赔说明 (对应前端的步骤条)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<StepItem> claimInstructions;

    /**
     * 卡密产品规格 (JSON对象数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CardSpecItem> cardSpecs;

    /**
     * 乐观锁版本
     */
    @Version
    private Long version;

    /**
     * 删除标记(0-未删除 1-删除)
     */
    @TableLogic
    private String delFlag;


    @Data
    public static class NoticeItem {
        private String title;
        private String content;
        private Integer sort;
    }
    @Data
    public static class ClauseItem {
        private String clauseName;
        private String fileUrl;
        private Integer sort;
    }

    @Data
    public static class StepItem {
        private String title;   // 步骤标题 (如：第一步：报案)
        private String content; // 步骤内容 (如：拨打95511...)
        private Integer sort;   // 排序号
    }

    @Data
    public static class CardSpecItem {
        private String specId;
        private String specName;
        private BigDecimal price;
        /**
         * 运费支付方式：prepaid-线上支付，collect-到付
         */
        private String freightPayType;
        private BigDecimal freight;
        private Integer stock;
        private Integer sort;
        private Integer status;
    }
}
