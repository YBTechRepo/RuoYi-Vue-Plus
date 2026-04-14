package org.dromara.insurance.domain.dto;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.hibernate.validator.constraints.Length;

import java.util.HashMap;
import java.util.Map;

@Data
public class BatchInsuredImportDto {
    /* =================== 投保人信息 =================== */
    @ExcelProperty(value = "投保人姓名", index = 0)
    @NotBlank(message = "投保人姓名不能为空")
    private String appName;

    @ExcelProperty(value = "投保人证件类型", index = 1, converter = DictCodeSplitConverter.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    @NotBlank(message = "投保人证件类型不能为空")
    private String appCertType;

    @ExcelProperty(value = "投保人证件号", index = 2)
    @NotBlank(message = "投保人证件号不能为空")
    @Length(min = 18, max = 18, message = "投保人证件号必须为18位")
    private String appCertNo;

    @ExcelProperty(value = "投保人手机号", index = 3)
    @NotBlank(message = "投保人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "投保人手机号格式错误")
    private String appPhone;

    @ExcelProperty(value = "投保人地址", index = 4)
    @NotBlank(message = "投保人地址不能为空")
    private String appAddress;

    /* =================== 关 系 =================== */
    @ExcelProperty(value = "与投保人关系", index = 6, converter = DictCodeSplitConverter.class)
    @ExcelDictFormat(dictType = "insurance_relationship_to_insured")
    @NotBlank(message = "与投保人关系不能为空")
    private String relation;

    /* =================== 被保人信息 =================== */
    @ExcelProperty(value = "被保人姓名", index = 5)
    @NotBlank(message = "被保人姓名不能为空")
    private String name;

    @ExcelProperty(value = "被保人证件类型", index = 7, converter = DictCodeSplitConverter.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    @NotBlank(message = "被保人证件类型不能为空")
    private String certType;

    @ExcelProperty(value = "被保人证件号", index = 8)
    @NotBlank(message = "被保人证件号不能为空")
    @Length(min = 18, max = 18, message = "被保人身份证号必须为18位")
    private String certNo;

    @ExcelProperty(value = "被保人手机号", index = 9)
    @NotBlank(message = "被保人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "被保人手机号格式错误")
    private String phone;

    @ExcelProperty(value = "被保人地址", index = 10)
    @NotBlank(message = "被保人地址不能为空")
    private String address;

    /* =================== 前端组件交互辅助字段 (不在 Excel 内) =================== */
    @ExcelIgnore
    private Boolean isValid; // 用于标识此行数据是否完全合法
    @ExcelIgnore
    private Boolean isEditing = false; // 前端 UI 状态位（可选，由于前端自己写了默认值，这里可以不加）
    @ExcelIgnore
    private Map<String, String> errors = new HashMap<>(); // 存放具体的错误字段和错误信息集合

    /**
     * FastExcel 自定义转换器：专门处理类似 "01 - 身份证" 这种带编号的数据
     * 解析时自动劈开字符串，只截取左侧的字典主键 "01"
     */
    public static class DictCodeSplitConverter implements Converter<String> {
        @Override
        public Class<?> supportJavaTypeKey() {
            return String.class;
        }
        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }
        @Override
        public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String stringValue = cellData.getStringValue();
            if (StringUtils.isNotBlank(stringValue)) {
                // 常见的连接符处理：支持横杠或空格分割
                // 例如 "01 - 身份证" -> 分割后取第 0 位即 "01"
                return stringValue.split("-")[0].trim().split(" ")[0].trim();
            }
            return null;
        }
    }
}
