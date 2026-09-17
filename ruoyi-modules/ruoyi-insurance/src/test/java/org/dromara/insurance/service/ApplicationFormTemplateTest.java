package org.dromara.insurance.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("dev")
class ApplicationFormTemplateTest {
    private static ApplicationFormTemplate template;
    @BeforeAll
    static void setup() throws Exception {
        template = new ApplicationFormTemplate();
    }

    static byte[] signature(boolean blank) throws Exception {
        BufferedImage image = new BufferedImage(360, 140, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 360, 140);
        if (!blank) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3));
            g.drawPolyline(new int[]{30, 50, 45, 95, 130, 120, 180, 210, 280},
                new int[]{90, 30, 110, 60, 100, 35, 50, 95, 45}, 9);
        }
        g.dispose();
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    static Map<String, Object> sample() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("orderNo", "TEST-SIGN-001");
        values.put("applicantName", "测试家长");
        values.put("applicantPhone", "13800000000");
        values.put("applicantAddress", "河南省郑州市金水区测试路1号");
        values.put("applicantPostcode", "450000");
        values.put("applicantCertNo", "110101199001010010");
        values.put("applicantCertTypeLabel", "身份证");
        values.put("applicantHousehold", "城镇户口");
        values.put("insuredName", "测试学生");
        values.put("insuredPhone", "13800000000");
        values.put("insuredAddress", "河南省郑州市金水区测试路1号");
        values.put("insuredPostcode", "450000");
        values.put("insuredCertNo", "110101201601010015");
        values.put("insuredCertTypeLabel", "身份证");
        values.put("insuredBirthday", "2016-01-01");
        values.put("insuredGender", "男");
        values.put("insuredHousehold", "城镇户口");
        values.put("relationLabel", "父母");
        values.put("beneficiary", "法定");
        values.put("businessType", "新投保业务");
        values.put("studentType", "小学生");
        values.put("schoolType", "全日制（走读）");
        values.put("schoolName", "测试小学");
        values.put("schoolClass", "三年级一班");
        values.put("medicalInsurance", "有");
        values.put("disputeMode", "诉讼");
        values.put("insuredSignerRole", "法定监护人");
        values.put("insuredSignerName", "测试家长");
        values.put("premium", "50.00");
        values.put("premiumUpper", "伍拾元整");
        values.put("paymentMethod", "余额支付");
        values.put("insurancePeriod", "自2026年10月01日零时起至2027年09月30日二十四时止");
        values.put("signatureDate", "2026年09月15日");
        String[] amounts = {"60000", "30000", "5000", "10000", "10800"};
        String[] premiums = {"30", "5", "5", "5", "5"};
        for (int i = 0; i < 5; i++) {
            values.put("liability" + i + "Amount", amounts[i]);
            values.put("liability" + i + "Rate", "");
            values.put("liability" + i + "Premium", premiums[i]);
        }
        return values;
    }

    @Test
    void preservesCompleteTemplateAndEmbedsAllThreeSignatures() throws Exception {
        assertEquals(18, template.metadata().path("pageCount").asInt());
        assertEquals(3, template.metadata().path("templates").size());
        assertTrue(template.metadata().path("configFields").isMissingNode(),
            "固定模板不再暴露保险责任和保费配置项");
        String statements = template.metadata().path("statements").toString();
        assertTrue(statements.contains("每次事故免赔额为100元"));
        assertTrue(statements.contains("给付标准为60元/人/天"));
        assertTrue(statements.contains("单次给付天数最高不超过30天"));
        assertFalse(statements.contains("每次事故免赔额为200元"));

        byte[] sig = signature(false);
        byte[] pdf = template.render(sample(), Map.of("special", sig, "applicant", sig, "insured", sig));
        Path output = Path.of("target/application-form-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdf);
        try (var doc = PDDocument.load(pdf)) {
            assertEquals(18, doc.getNumberOfPages(), "生成文件须保持最终版18页，不再追加补充页");
            String text = new PDFTextStripper().getText(doc);
            assertTrue(text.contains("测试家长"));
            assertTrue(text.contains("测试学生"));
            assertTrue(text.contains("伍拾元整"));
            assertTrue(text.contains("住院津贴"));
            assertFalse(text.contains("余额支付"), "缴费日期及方式应保持底版空白");
            int images = 0;
            for (int pageNumber : new int[]{1, 2}) {
                for (var name : doc.getPage(pageNumber).getResources().getXObjectNames()) {
                    if (doc.getPage(pageNumber).getResources().getXObject(name) instanceof PDImageXObject) images++;
                }
            }
            assertTrue(images >= 3, "三张签字图像须位于原版特别约定及声明页");
        }
    }

    @Test
    void rejectsOverflowRatherThanTruncatingInformation() {
        Map<String, Object> values = sample();
        values.put("applicantName", "长姓名".repeat(100));
        assertThrows(ServiceException.class, () -> template.render(values, Map.of()));
    }

    @Test
    void exposesThreeNamedFixedPremiumTemplates() throws Exception {
        Map<String, String> expected = Map.of(
            "hh-student-2025", "伍拾元整",
            "hh-student-2025-plan2", "陆拾元整",
            "hh-student-2025-plan3", "玖拾元整");
        Map<String, BigDecimal> premiums = Map.of(
            "hh-student-2025", new BigDecimal("50"),
            "hh-student-2025-plan2", new BigDecimal("60"),
            "hh-student-2025-plan3", new BigDecimal("90"));
        Map<String, String> names = Map.of(
            "hh-student-2025", "方案一（50元）- 学生、幼儿意外伤害保险投保单",
            "hh-student-2025-plan2", "方案二（60元）- 学生、幼儿意外伤害保险投保单",
            "hh-student-2025-plan3", "方案三（90元）- 学生、幼儿意外伤害保险投保单");
        byte[] signature = signature(false);
        Map<String, byte[]> signatures = Map.of(
            "special", signature, "applicant", signature, "insured", signature);
        Path outputDirectory = Path.of("target");
        Files.createDirectories(outputDirectory);
        for (var metadata : template.metadata().path("templates")) {
            String code = metadata.path("code").asText();
            assertEquals(names.get(code), metadata.path("name").asText());
            assertEquals(premiums.get(code), template.premium(code, "v1"));
            assertEquals(12, template.insuranceMonths(code, "v1"));
            String selectedStatements = template.metadata(code, "v1").path("statements").toString();
            assertTrue(selectedStatements.contains("给付标准为"
                + ("hh-student-2025".equals(code) ? "60" : "100") + "元/人/天"));
            byte[] rendered = template.render(code, "v1", sample(), signatures);
            Files.write(outputDirectory.resolve("application-form-sample-"
                + metadata.path("premium").asText() + ".pdf"), rendered);
            try (var pdf = PDDocument.load(rendered)) {
                assertEquals(18, pdf.getNumberOfPages());
                assertTrue(new PDFTextStripper().getText(pdf).contains(expected.get(code)));
                assertFalse(new PDFTextStripper().getText(pdf).contains("余额支付"),
                    "缴费日期及方式应保持底版空白");
            }
        }
        var placementKeys = new java.util.HashSet<String>();
        template.metadata().path("placements").forEach(item -> placementKeys.add(item.path("key").asText()));
        assertTrue(java.util.Collections.disjoint(placementKeys, java.util.Set.of(
            "premiumUpper", "liability0Amount", "liability0Premium", "liability1Amount", "liability1Premium",
            "liability2Amount", "liability2Premium", "liability3Amount", "liability3Premium",
            "liability4Amount", "liability4Premium")));
    }

    @Test
    void keepsGeneratedValuesInsideTheirCellsAndMatchesTemplateTypography() throws Exception {
        try (var pdf = PDDocument.load(template.render(sample(), Map.of()))) {
            var firstPage = new PDFTextStripperByArea();
            firstPage.addRegion("applicantNameLabel", new Rectangle2D.Float(62, 295, 76, 21));
            firstPage.addRegion("applicantNameValue", new Rectangle2D.Float(138, 295, 222, 21));
            firstPage.addRegion("applicantCertNo", new Rectangle2D.Float(413, 336, 143, 21));
            firstPage.extractRegions(pdf.getPage(0));
            assertFalse(compact(firstPage.getTextForRegion("applicantNameLabel")).contains("测试家长"));
            assertTrue(compact(firstPage.getTextForRegion("applicantNameValue")).contains("测试家长"));
            assertTrue(compact(firstPage.getTextForRegion("applicantCertNo")).contains("110101199001010010"));

            var schoolPage = new PDFTextStripperByArea();
            schoolPage.addRegion("schoolName", new Rectangle2D.Float(462, 604, 95, 25));
            schoolPage.addRegion("schoolClass", new Rectangle2D.Float(462, 628, 95, 18));
            schoolPage.extractRegions(pdf.getPage(0));
            assertTrue(compact(schoolPage.getTextForRegion("schoolName")).contains("测试小学"));
            assertTrue(compact(schoolPage.getTextForRegion("schoolClass")).contains("三年级一班"));

            var firstLiabilityPage = new PDFTextStripperByArea();
            firstLiabilityPage.addRegion("blankMainContinuation", new Rectangle2D.Float(241, 723, 171, 20));
            firstLiabilityPage.addRegion("disease", new Rectangle2D.Float(241, 743, 171, 22));
            firstLiabilityPage.extractRegions(pdf.getPage(0));
            assertFalse(compact(firstLiabilityPage.getTextForRegion("blankMainContinuation")).contains("60000"));
            assertTrue(compact(firstLiabilityPage.getTextForRegion("disease")).contains("30000"));

            var secondLiabilityPage = new PDFTextStripperByArea();
            secondLiabilityPage.addRegion("accidentMedical", new Rectangle2D.Float(241, 114, 171, 32));
            secondLiabilityPage.extractRegions(pdf.getPage(1));
            assertTrue(compact(secondLiabilityPage.getTextForRegion("accidentMedical")).contains("5000"));
        }
        assertEquals("SimSun", template.metadata().path("fontFamily").asText(),
            "动态填写字体须与模板正文一致");
        template.metadata().path("placements").forEach(placement ->
            assertEquals(9D, placement.path("fontSize").asDouble(), 0.001D,
                () -> placement.path("key").asText() + " 字号须统一为9pt"));
    }

    @Test
    void rendersDynamicSelectionsAsSolidBlocksAndSkipsTemplateDefaults() throws Exception {
        var dynamicCheckKeys = new java.util.HashSet<String>();
        template.metadata().path("checks").forEach(check -> dynamicCheckKeys.add(check.path("key").asText()));
        assertFalse(dynamicCheckKeys.contains("businessType"), "底版已预选新投保业务，不应二次绘制");
        assertFalse(dynamicCheckKeys.contains("disputeMode"), "底版已预选诉讼，不应二次绘制");
        template.metadata().path("placements").forEach(placement ->
            assertNotEquals("insuredGender", placement.path("key").asText(),
                "性别选项文字已在底版中，不应重复填写"));

        try (var pdf = PDDocument.load(template.render(sample(), Map.of()))) {
            var selectedOption = new PDFTextStripperByArea();
            selectedOption.addRegion("applicantCertType", new Rectangle2D.Float(137, 336, 36, 20));
            selectedOption.extractRegions(pdf.getPage(0));
            assertTrue(selectedOption.getTextForRegion("applicantCertType").contains("■"),
                "选中项应写入黑块字符，而不是绘制矢量矩形");
            BufferedImage firstPage = new PDFRenderer(pdf).renderImageWithDPI(0, 144);
            assertTrue(blackRatio(firstPage, 293, 688, 6, 6) > 0.8,
                "身份证选项应在原方框内显示实心黑块");
            assertTrue(blackRatio(firstPage, 385, 688, 6, 6) < 0.2,
                "未选中的护照选项应保持空白");
        }
    }

    @Test
    void configuresTemplateSpecificSignatureDateHorizontalOffsets() throws Exception {
        Map<String, Double> expected = Map.of(
            "hh-student-2025", 12D,
            "hh-student-2025-plan2", 16D,
            "hh-student-2025-plan3", 12D);
        byte[] signature = ApplicationFormTemplate.normalizeSignature(signature(false));
        Map<String, byte[]> signatures = Map.of(
            "special", signature, "applicant", signature, "insured", signature);
        for (var item : template.metadata().path("templates")) {
            String code = item.path("code").asText();
            assertEquals(expected.get(code),
                item.path("offsets").path("placementX").path("signatureDate").asDouble());
            Files.write(Path.of("target/signature-date-calibration-" + item.path("premium").asText() + ".pdf"),
                template.render(code, "v1", sample(), signatures));
        }
    }

    private static String compact(String text) {
        return text.replaceAll("\\s+", "");
    }

    private static double blackRatio(BufferedImage image, int x, int y, int width, int height) {
        int black = 0;
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                int rgb = image.getRGB(column, row);
                int red = (rgb >> 16) & 255;
                int green = (rgb >> 8) & 255;
                int blue = rgb & 255;
                if (red + green + blue < 180) black++;
            }
        }
        return black / (double) (width * height);
    }

    @Test
    void ignoresRemovedLegacyDisclosureFieldsWithoutAppendingPages() throws Exception {
        Map<String, Object> values = sample();
        values.put("otherPolicyAnswer", "有");
        values.put("otherPoliciesSummary", "旧保单公司唯一标记");
        values.put("otherPoliciesDetail", "旧保单明细唯一标记");
        values.put("arbitrationCommittee", "旧仲裁委员会唯一标记");
        try (var pdf = PDDocument.load(template.render(values, Map.of()))) {
            assertEquals(18, pdf.getNumberOfPages());
            String text = new PDFTextStripper().getText(pdf);
            assertFalse(text.contains("旧保单公司唯一标记"));
            assertFalse(text.contains("旧保单明细唯一标记"));
            assertFalse(text.contains("旧仲裁委员会唯一标记"));
        }
    }

    @Test
    void rejectsBlankAndInvalidImages() throws Exception {
        assertThrows(ServiceException.class, () -> ApplicationFormTemplate.normalizeSignature(signature(true)));
        assertThrows(ServiceException.class, () -> ApplicationFormTemplate.normalizeSignature(new byte[]{1, 2, 3}));
        assertThrows(ServiceException.class, () -> ApplicationFormTemplate.normalizeSignature(new byte[1024 * 1024 + 1]));
        byte[] normalized = ApplicationFormTemplate.normalizeSignature(signature(false));
        BufferedImage normalizedImage = ImageIO.read(new ByteArrayInputStream(normalized));
        assertNotNull(normalizedImage);
        assertTrue(normalizedImage.getWidth() < 360, "应裁掉签字左右两侧空白");
        assertTrue(normalizedImage.getHeight() < 140, "应裁掉签字上下两侧空白");
    }

    @Test
    void validatesRemainingFieldsAndOmitsRemovedInputsFromMetadata() {
        Map<String, Object> values = sample();
        assertDoesNotThrow(() -> template.validateFields(values));
        var keys = new java.util.HashSet<String>();
        template.metadata().path("fields").forEach(field -> keys.add(field.path("key").asText()));
        assertTrue(java.util.Collections.disjoint(keys, java.util.Set.of(
            "beneficiary", "businessType", "previousPolicyNo",
            "otherPolicyAnswer", "disputeMode", "arbitrationCommittee",
            "insuredSignerRole", "insuredSignerName", "guardianRelation", "guardianCertNo")));
        var fieldTypes = new java.util.HashMap<String, String>();
        template.metadata().path("fields").forEach(field ->
            fieldTypes.put(field.path("key").asText(), field.path("type").asText()));
        assertEquals("digit", fieldTypes.get("applicantPostcode"));
        assertEquals("digit", fieldTypes.get("insuredPostcode"));
        values.put("applicantPostcode", "450A00");
        assertThrows(ServiceException.class, () -> template.validateFields(values));
        values.put("applicantPostcode", "450000");
        values.remove("medicalInsurance");
        assertThrows(ServiceException.class, () -> template.validateFields(values));
        values.put("medicalInsurance", "有");
        assertDoesNotThrow(() -> template.validateFields(values));
    }

    @Test
    void onlyAllowsMatchingCardProxyProductsAndFixedTemplatePremium() {
        InsuranceProductConfig product = new InsuranceProductConfig();
        product.setApplicationFormRequired(true);
        product.setProductMode(1);
        product.setInsureMode(1);
        product.setApplicationTemplateCode(ApplicationFormTemplate.CODE);
        product.setApplicationTemplateVersion(ApplicationFormTemplate.VERSION);
        product.setMinPremium(new BigDecimal("50.00"));
        assertDoesNotThrow(() -> template.validateProduct(product));
        product.setProductMode(2);
        assertThrows(ServiceException.class, () -> template.validateProduct(product));
        product.setProductMode(1);
        product.setInsureMode(0);
        assertThrows(ServiceException.class, () -> template.validateProduct(product));
        product.setInsureMode(1);
        product.setMinPremium(new BigDecimal("200"));
        assertThrows(ServiceException.class, () -> template.validateProduct(product));
        product.setApplicationFormRequired(false);
        assertDoesNotThrow(() -> template.validateProduct(product));
    }
}
