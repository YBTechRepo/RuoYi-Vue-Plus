package org.dromara.insurance.utils;

import cn.hutool.core.collection.CollUtil;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 投保扩展字段解析与格式化工具。
 */
public class DynamicInsureFieldUtils {

    private DynamicInsureFieldUtils() {
    }

    public static List<Field> parseSchema(String schemaJson) {
        if (StringUtils.isBlank(schemaJson)) {
            return Collections.emptyList();
        }
        List<Map> schemaList = JsonUtils.parseArray(schemaJson, Map.class);
        if (CollUtil.isEmpty(schemaList)) {
            return Collections.emptyList();
        }
        return schemaList.stream()
            .map(item -> {
                Field field = new Field();
                field.setKey(Objects.toString(item.get("key"), ""));
                field.setLabel(Objects.toString(item.get("label"), ""));
                field.setType(Objects.toString(item.get("type"), "text"));
                field.setRequired(Boolean.TRUE.equals(item.get("required")));
                field.setSort(toInt(item.get("sort")));
                field.setOptions(parseOptions(item.get("options")));
                return field;
            })
            .filter(item -> StringUtils.isNotBlank(item.getKey()) && StringUtils.isNotBlank(item.getLabel()))
            .sorted(Comparator.comparing(item -> Optional.ofNullable(item.getSort()).orElse(0)))
            .collect(Collectors.toList());
    }

    public static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
        }
        if (value instanceof Map<?, ?> map) {
            String regionText = Objects.toString(map.get("regionText"), "");
            String detail = Objects.toString(map.get("detail"), "");
            String text = (regionText + " " + detail).trim();
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
            return map.values().stream().map(String::valueOf).filter(StringUtils::isNotBlank).collect(Collectors.joining(" "));
        }
        return String.valueOf(value);
    }

    public static Object normalizeImportValue(Field field, String rawValue, String detailValue) {
        String type = field.getType();
        if ("address".equals(type)) {
            Map<String, Object> address = new LinkedHashMap<>();
            address.put("regionText", StringUtils.trimToEmpty(rawValue));
            address.put("detail", StringUtils.trimToEmpty(detailValue));
            return address;
        }
        if ("checkbox".equals(type)) {
            if (StringUtils.isBlank(rawValue)) {
                return Collections.emptyList();
            }
            return Arrays.stream(rawValue.split("[、,，]"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(item -> normalizeOptionValue(field, item))
                .collect(Collectors.toList());
        }
        if ("select".equals(type) || "radio".equals(type)) {
            return normalizeOptionValue(field, StringUtils.trimToEmpty(rawValue));
        }
        return StringUtils.trimToEmpty(rawValue);
    }

    public static String validateValue(Field field, Object value) {
        String text = formatValue(value);
        if (field.isRequired() && StringUtils.isBlank(text)) {
            return field.getLabel() + "不能为空";
        }
        if (StringUtils.isBlank(text)) {
            return "";
        }
        if ("number".equals(field.getType()) || "money".equals(field.getType())) {
            try {
                new BigDecimal(text);
            } catch (Exception e) {
                return field.getLabel() + "必须为数字";
            }
        }
        if (("select".equals(field.getType()) || "radio".equals(field.getType())) && CollUtil.isNotEmpty(field.getOptions())) {
            boolean matched = field.getOptions().stream().anyMatch(option -> Objects.equals(option.getLabel(), text) || Objects.equals(option.getValue(), text));
            if (!matched) {
                return field.getLabel() + "不在可选范围内";
            }
        }
        if ("checkbox".equals(field.getType()) && CollUtil.isNotEmpty(field.getOptions()) && value instanceof Collection<?> values) {
            for (Object item : values) {
                String itemText = Objects.toString(item, "");
                boolean matched = field.getOptions().stream().anyMatch(option -> Objects.equals(option.getLabel(), itemText) || Objects.equals(option.getValue(), itemText));
                if (!matched) {
                    return field.getLabel() + "包含非法选项";
                }
            }
        }
        return "";
    }

    public static String formatValue(Field field, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> formatOptionLabel(field, item)).filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
        }
        if (value instanceof Map<?, ?>) {
            return formatValue(value);
        }
        return formatOptionLabel(field, value);
    }

    public static List<List<String>> buildHead(List<String> fixedHeads, List<Field> fields) {
        List<List<String>> head = fixedHeads.stream().map(Collections::singletonList).collect(Collectors.toCollection(ArrayList::new));
        for (Field field : fields) {
            if ("address".equals(field.getType())) {
                head.add(Collections.singletonList(headerLabel(field, "地区")));
                head.add(Collections.singletonList(headerLabel(field, "详细地址")));
            } else {
                head.add(Collections.singletonList(headerLabel(field, null)));
            }
        }
        return head;
    }

    public static String headerLabel(Field field, String suffix) {
        String label = field.getLabel() + (field.isRequired() ? "*" : "");
        if (StringUtils.isNotBlank(suffix)) {
            return label + "-" + suffix;
        }
        if (CollUtil.isNotEmpty(field.getOptions()) && ("select".equals(field.getType()) || "radio".equals(field.getType()) || "checkbox".equals(field.getType()))) {
            String options = field.getOptions().stream().map(Option::getLabel).filter(StringUtils::isNotBlank).collect(Collectors.joining("/"));
            return StringUtils.isBlank(options) ? label : label + "(" + options + ")";
        }
        return label;
    }

    public static List<String> optionLabels(Field field) {
        if (field == null || CollUtil.isEmpty(field.getOptions())) {
            return Collections.emptyList();
        }
        return field.getOptions().stream()
            .map(Option::getLabel)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return new BigDecimal(String.valueOf(value)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private static List<Option> parseOptions(Object rawOptions) {
        if (!(rawOptions instanceof Collection<?> options)) {
            return Collections.emptyList();
        }
        return options.stream()
            .filter(item -> item instanceof Map<?, ?>)
            .map(item -> {
                Map<?, ?> map = (Map<?, ?>) item;
                Option option = new Option();
                option.setLabel(Objects.toString(map.get("label"), ""));
                option.setValue(Objects.toString(map.get("value"), ""));
                return option;
            })
            .collect(Collectors.toList());
    }

    private static String normalizeOptionValue(Field field, String value) {
        if (StringUtils.isBlank(value) || CollUtil.isEmpty(field.getOptions())) {
            return value;
        }
        return field.getOptions().stream()
            .filter(option -> Objects.equals(option.getLabel(), value) || Objects.equals(option.getValue(), value))
            .map(Option::getValue)
            .findFirst()
            .orElse(value);
    }

    private static String formatOptionLabel(Field field, Object value) {
        String text = Objects.toString(value, "");
        if (StringUtils.isBlank(text) || CollUtil.isEmpty(field.getOptions())) {
            return text;
        }
        return field.getOptions().stream()
            .filter(option -> Objects.equals(option.getValue(), text) || Objects.equals(option.getLabel(), text))
            .map(Option::getLabel)
            .findFirst()
            .orElse(text);
    }

    public static class Field {
        private String key;
        private String label;
        private String type;
        private boolean required;
        private Integer sort;
        private List<Option> options;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    public static class Option {
        private String label;
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
