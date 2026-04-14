package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.idev.excel.FastExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.dto.BatchInsuredImportDto;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.vo.BatchPreviewVO;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.listener.BatchInsuredImportListener;
import org.dromara.insurance.service.IInsuranceApplyRecordService;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 批量投保 - 控制器
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/batch")
public class BatchInsuranceController {

    private final IInsuranceProductConfigService insuranceProductConfigService;

    private final IBizUserAccountService bizUserAccountService;

    private final IInsuranceApplyRecordService insuranceApplyRecordService;

    /**
     * 导入并预解析投保人员名单 (前置审查)
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/importData")
    public R<Map<String, Object>> importData(@RequestPart("file") MultipartFile file) throws IOException {

        // 1. 初始化监听器
        BatchInsuredImportListener listener = new BatchInsuredImportListener();

        // 2. 利用 FastExcel.read 将 InputStream 怼入流水线
        FastExcel.read(file.getInputStream(), BatchInsuredImportDto.class, listener)
            .sheet()
            // 可以做一下最大行数防护判断，比如不允许超过 3000 行
            .doRead();

        // 3. 构建前端所需的响应报文结构
        Map<String, Object> result = new HashMap<>();
        result.put("validCount", listener.getValidCount());
        result.put("invalidCount", listener.getInvalidCount());
        result.put("auditList", listener.getResultList());
        return R.ok("解析完成", result);
    }

    /**
     * 保费预览
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/preview")
    public R<BatchPreviewVO> preview(@RequestBody BatchSubmitDTO batchSubmitDTO) {
        log.info("批量投保计算-请求参数：{}", JsonUtils.toJsonString(batchSubmitDTO));

        // 1. 参数校验
        if (batchSubmitDTO.getAuditList() == null) {
            return R.fail("投保人员名单不能为空");
        }
        int validCount = batchSubmitDTO.getAuditList().size();
        log.info("批量投保计算-人数：{}", validCount);

        // 2. 获取产品信息
        InsuranceSalesProductVo productVo = insuranceProductConfigService.querySalesProductById(batchSubmitDTO.getProductId());
        if (productVo == null) {
            return R.fail("产品不存在或已下架");
        }

        // 3. 获取当前登录用户 ID 并查询账户
        Long userId = LoginHelper.getUserId();
        BizUserAccountVo accountVo = bizUserAccountService.queryById(userId);
        log.info("userId：{}", userId);

        // 4. 组装预览数据
        BatchPreviewVO previewVO = new BatchPreviewVO();
        previewVO.setProductName(productVo.getProductName());

        // 保费与费率处理 (使用默认值防止 NPE)
        BigDecimal grossPremium = productVo.getMinPremium() != null ? productVo.getMinPremium() : BigDecimal.ZERO;
        BigDecimal commissionRate = productVo.getDisplayCommissionRate() != null ? productVo.getDisplayCommissionRate() : BigDecimal.ZERO;

        // 计算单人净保费：gross * (1 - rate)，保留两位小数
        BigDecimal netPremium = grossPremium.multiply(BigDecimal.ONE.subtract(commissionRate))
            .setScale(2, RoundingMode.HALF_UP);

        previewVO.setGrossPremium(grossPremium);
        previewVO.setCommissionRate(commissionRate.setScale(2, RoundingMode.HALF_UP));
        previewVO.setNetPremium(netPremium);
        previewVO.setValidCount(validCount);

        // 计算应付总额：netPremium * 人数
        BigDecimal totalAmount = netPremium.multiply(new BigDecimal(validCount))
            .setScale(2, RoundingMode.HALF_UP);
        previewVO.setTotalAmount(totalAmount);

        // 余额校验
        BigDecimal balance = (accountVo != null && accountVo.getBalance() != null) ? accountVo.getBalance() : BigDecimal.ZERO;
        previewVO.setWalletBalance(balance);
        previewVO.setIsBalanceSufficient(balance.compareTo(totalAmount) >= 0);

        return R.ok(previewVO);
    }

    /**
     * 批量投保提交
     */
    @SaCheckLogin
    @PostMapping("/submit")
    @RepeatSubmit()
    public R<Map<String, String>> submit(@RequestBody BatchSubmitDTO batchSubmitDTO) {
        String batchOrderNo = insuranceApplyRecordService.submitBatch(batchSubmitDTO);
        Map<String, String> result = new HashMap<>();
        result.put("batchOrderNo", batchOrderNo);
        return R.ok("出单成功", result);
    }
}
