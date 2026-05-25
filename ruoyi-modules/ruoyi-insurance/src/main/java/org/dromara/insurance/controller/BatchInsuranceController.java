package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.domain.dto.BatchInsuredImportDto;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.vo.BatchPreviewVO;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.service.IInsuranceApplyRecordService;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.insurance.utils.DynamicInsureFieldUtils;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private final DictService dictService;

    /**
     * 导入并预解析投保人员名单 (前置审查)
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/importData")
    public R<Map<String, Object>> importData(@RequestParam("productId") Long productId,
                                             @RequestPart("file") MultipartFile file) throws IOException {
        List<DynamicInsureFieldUtils.Field> dynamicFields = queryDynamicFields(productId);
        BatchRawImportListener listener = new BatchRawImportListener(dynamicFields, dictService);
        FastExcel.read(file.getInputStream(), listener).headRowNumber(0).sheet().doRead();

        Map<String, Object> result = new HashMap<>();
        result.put("validCount", listener.getValidCount());
        result.put("invalidCount", listener.getInvalidCount());
        result.put("auditList", listener.getResultList());
        return R.ok("解析完成", result);
    }

    /**
     * 下载产品专属批量投保模板
     */
    @SaCheckLogin
    @GetMapping("/template")
    public void template(@RequestParam("productId") Long productId, HttpServletResponse response) throws IOException {
        List<String> fixedHeads = Arrays.asList(
            "投保人姓名",
            "投保人证件类型",
            "投保人证件号",
            "投保人手机号",
            "投保人地址",
            "被保人姓名",
            "与投保人关系",
            "被保人证件类型",
            "被保人证件号",
            "被保人手机号",
            "被保人地址"
        );
        List<DynamicInsureFieldUtils.Field> dynamicFields = queryDynamicFields(productId);
        String fileName = URLEncoder.encode("人员清单导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        FastExcel.write(response.getOutputStream())
            .head(DynamicInsureFieldUtils.buildHead(fixedHeads, dynamicFields))
            .autoCloseStream(false)
            .registerWriteHandler(new BatchTemplateDropDownHandler(buildDropDownOptions(dynamicFields)))
            .sheet("人员清单")
            .doWrite(Collections.emptyList());
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

    private List<DynamicInsureFieldUtils.Field> queryDynamicFields(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        InsuranceProductSaveBo productData = insuranceProductConfigService.getProductFull(productId);
        String schema = productData == null || productData.getProduct() == null ? null : productData.getProduct().getInsureFormSchema();
        return DynamicInsureFieldUtils.parseSchema(schema);
    }

    private Map<Integer, List<String>> buildDropDownOptions(List<DynamicInsureFieldUtils.Field> dynamicFields) {
        Map<Integer, List<String>> options = new LinkedHashMap<>();
        List<String> idTypeOptions = dictOptions("insurance_id_type");
        List<String> relationOptions = dictOptions("insurance_relationship_to_insured");
        options.put(1, idTypeOptions);
        options.put(6, relationOptions);
        options.put(7, idTypeOptions);

        int colIndex = 11;
        for (DynamicInsureFieldUtils.Field field : Optional.ofNullable(dynamicFields).orElseGet(Collections::emptyList)) {
            if ("address".equals(field.getType())) {
                colIndex += 2;
                continue;
            }
            if (isOptionField(field)) {
                List<String> fieldOptions = DynamicInsureFieldUtils.optionLabels(field);
                if (!fieldOptions.isEmpty()) {
                    options.put(colIndex, fieldOptions);
                }
            }
            colIndex++;
        }
        return options;
    }

    private List<String> dictOptions(String dictType) {
        Map<String, String> dictMap = Optional.ofNullable(dictService.getAllDictByDictType(dictType)).orElseGet(Collections::emptyMap);
        List<String> options = new ArrayList<>();
        dictMap.forEach((value, label) -> {
            if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(label)) {
                options.add(value + "-" + label);
            }
        });
        return options;
    }

    private static boolean isOptionField(DynamicInsureFieldUtils.Field field) {
        return "select".equals(field.getType()) || "radio".equals(field.getType()) || "checkbox".equals(field.getType());
    }

    private static class BatchRawImportListener extends AnalysisEventListener<Map<Integer, String>> {
        private final List<DynamicInsureFieldUtils.Field> dynamicFields;
        private final DictService dictService;
        private final List<BatchInsuredImportDto> resultList = new ArrayList<>();
        private int validCount;
        private int invalidCount;

        BatchRawImportListener(List<DynamicInsureFieldUtils.Field> dynamicFields, DictService dictService) {
            this.dynamicFields = dynamicFields == null ? Collections.emptyList() : dynamicFields;
            this.dictService = dictService;
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            Integer rowIndex = context.readRowHolder().getRowIndex();
            if (rowIndex == null || rowIndex == 0) {
                return;
            }
            BatchInsuredImportDto dto = buildDto(data);
            validateDto(dto);
            resultList.add(dto);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("批量投保 Excel 解析完成，共解析 [{}] 条，成功 [{}] 条，失败 [{}] 条", resultList.size(), validCount, invalidCount);
        }

        private BatchInsuredImportDto buildDto(Map<Integer, String> row) {
            BatchInsuredImportDto dto = new BatchInsuredImportDto();
            dto.setAppName(cell(row, 0));
            dto.setAppCertType(normalizeDictValue("insurance_id_type", cell(row, 1)));
            dto.setAppCertNo(cell(row, 2));
            dto.setAppPhone(cell(row, 3));
            dto.setAppAddress(cell(row, 4));
            dto.setName(cell(row, 5));
            dto.setRelation(normalizeDictValue("insurance_relationship_to_insured", cell(row, 6)));
            dto.setCertType(normalizeDictValue("insurance_id_type", cell(row, 7)));
            dto.setCertNo(cell(row, 8));
            dto.setPhone(cell(row, 9));
            dto.setAddress(cell(row, 10));

            int colIndex = 11;
            for (DynamicInsureFieldUtils.Field field : dynamicFields) {
                if ("address".equals(field.getType())) {
                    dto.getExtraData().put(field.getKey(), DynamicInsureFieldUtils.normalizeImportValue(field, cell(row, colIndex), cell(row, colIndex + 1)));
                    colIndex += 2;
                } else {
                    dto.getExtraData().put(field.getKey(), DynamicInsureFieldUtils.normalizeImportValue(field, cell(row, colIndex), null));
                    colIndex += 1;
                }
            }
            return dto;
        }

        private void validateDto(BatchInsuredImportDto dto) {
            try {
                ValidatorUtils.validate(dto);
                for (DynamicInsureFieldUtils.Field field : dynamicFields) {
                    String error = DynamicInsureFieldUtils.validateValue(field, dto.getExtraData().get(field.getKey()));
                    if (StringUtils.isNotBlank(error)) {
                        dto.getErrors().put("extraData." + field.getKey(), error);
                    }
                }
            } catch (ConstraintViolationException e) {
                for (ConstraintViolation<?> item : e.getConstraintViolations()) {
                    dto.getErrors().put(item.getPropertyPath().toString(), item.getMessage());
                }
            } catch (Exception e) {
                dto.getErrors().put("systemError", "数据解析异常：" + e.getMessage());
            }
            dto.setIsValid(dto.getErrors().isEmpty());
            if (dto.getIsValid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }

        private String cell(Map<Integer, String> row, int index) {
            return StringUtils.trimToEmpty(row.get(index));
        }

        private String splitDictCode(String value) {
            if (StringUtils.isBlank(value)) {
                return value;
            }
            return value.split("-")[0].trim().split(" ")[0].trim();
        }

        private String normalizeDictValue(String dictType, String value) {
            if (StringUtils.isBlank(value)) {
                return value;
            }
            String trimValue = StringUtils.trimToEmpty(value);
            String splitValue = splitDictCode(trimValue);
            Map<String, String> dictMap = Optional.ofNullable(dictService.getAllDictByDictType(dictType)).orElseGet(Collections::emptyMap);
            if (dictMap.containsKey(splitValue)) {
                return splitValue;
            }
            if (dictMap.containsKey(trimValue)) {
                return trimValue;
            }
            for (Map.Entry<String, String> entry : dictMap.entrySet()) {
                if (trimValue.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return splitValue;
        }

        public List<BatchInsuredImportDto> getResultList() {
            return resultList;
        }

        public int getValidCount() {
            return validCount;
        }

        public int getInvalidCount() {
            return invalidCount;
        }
    }

    private static class BatchTemplateDropDownHandler implements SheetWriteHandler {
        private static final int FIRST_ROW = 1;
        private static final int LAST_ROW = 1000;
        private final Map<Integer, List<String>> dropDownOptions;
        private int hiddenSheetIndex;

        BatchTemplateDropDownHandler(Map<Integer, List<String>> dropDownOptions) {
            this.dropDownOptions = dropDownOptions == null ? Collections.emptyMap() : dropDownOptions;
        }

        @Override
        public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            if (dropDownOptions.isEmpty()) {
                return;
            }
            Sheet sheet = writeSheetHolder.getSheet();
            Workbook workbook = writeWorkbookHolder.getWorkbook();
            DataValidationHelper helper = sheet.getDataValidationHelper();
            dropDownOptions.forEach((colIndex, options) -> addDropDown(workbook, sheet, helper, colIndex, options));
        }

        private void addDropDown(Workbook workbook, Sheet sheet, DataValidationHelper helper, Integer colIndex, List<String> options) {
            if (colIndex == null || options == null || options.isEmpty()) {
                return;
            }
            DataValidationConstraint constraint;
            if (options.size() > 10 || options.stream().mapToInt(String::length).sum() > 200) {
                String name = writeOptionsToHiddenSheet(workbook, options, colIndex);
                constraint = helper.createFormulaListConstraint(name);
            } else {
                constraint = helper.createExplicitListConstraint(options.toArray(new String[0]));
            }
            CellRangeAddressList addressList = new CellRangeAddressList(FIRST_ROW, LAST_ROW, colIndex, colIndex);
            DataValidation validation = helper.createValidation(constraint, addressList);
            if (validation instanceof XSSFDataValidation) {
                validation.setSuppressDropDownArrow(true);
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation.createErrorBox("提示", "此值与单元格定义数据不一致");
                validation.setShowErrorBox(true);
                validation.createPromptBox("填写说明", "请从下拉选项中选择，或填写对应字典值");
                validation.setShowPromptBox(true);
            }
            sheet.addValidationData(validation);
        }

        private String writeOptionsToHiddenSheet(Workbook workbook, List<String> options, int colIndex) {
            String sheetName = WorkbookUtil.createSafeSheetName("batchOptions_" + hiddenSheetIndex++);
            Sheet optionSheet = workbook.createSheet(sheetName);
            workbook.setSheetHidden(workbook.getSheetIndex(optionSheet), true);
            for (int i = 0; i < options.size(); i++) {
                Row row = optionSheet.createRow(i);
                row.createCell(0).setCellValue(options.get(i));
            }
            String nameName = "batch_options_" + hiddenSheetIndex + "_" + colIndex;
            Name name = workbook.createName();
            name.setNameName(nameName);
            name.setRefersToFormula(String.format("%s!$A$1:$A$%d", sheetName, options.size()));
            return nameName;
        }
    }
}
