package org.dromara.insurance.domain.bo;

import lombok.Data;
import org.dromara.insurance.domain.InsuranceProductDetail;

import java.util.List;

@Data
public class InsuranceProductSaveBo {
    private InsuranceProductConfigBo product;

    // 【第二部分：保障责任】(批量存入 liability 表)
    private List<InsuranceProductLiabilityBo> liabilityList;

    // 【第三部分：投保须知】(存入 detail 表 JSON 字段)
    private List<InsuranceProductDetail.NoticeItem> insureNotice;

    // 【第四部分：产品特点图】(存入 detail 表 JSON 字段)
    private List<String> featureImages;

    // 【第五部分：理赔流程图】(存入 detail 表 JSON 字段)
    private List<String> claimImages;

    // 【朋友圈营销文案】(存入 detail 表)
    private String marketingCopy;

    // 【朋友圈营销素材图片】(存入 detail 表 JSON 字段)
    private List<String> marketingImages;

    // 【第六部分：条款须知文件】(存入 detail 表 JSON 字段)
    private List<InsuranceProductDetail.ClauseItem> clauseFiles;

    /**
     * 理赔说明步骤
     */
    private List<InsuranceProductDetail.StepItem> claimInstructions;

    /**
     * 卡密产品规格
     */
    private List<InsuranceProductDetail.CardSpecItem> cardSpecs;

    private String imgUrl;
}
