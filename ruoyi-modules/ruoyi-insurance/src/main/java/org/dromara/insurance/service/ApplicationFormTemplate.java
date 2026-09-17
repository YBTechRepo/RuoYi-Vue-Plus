package org.dromara.insurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

/** 预置模板只使用校准过的版本；页码及位置以 PDF 顶部为原点。 */
@Component
public class ApplicationFormTemplate {
    public static final String CODE = "hh-student-2025";
    public static final String VERSION = "v1";
    private static final String ROOT = "application-forms/hh-student-2025-v1/";
    private static final String FONT_RESOURCE = "SimSun.ttc";
    private static final String FONT_NAME = "SimSun";
    private static final float CHECK_BOX_X_OFFSET = 7.5f;
    private static final float CHECK_BOX_Y_OFFSET = 1f;
    private static final float CHECK_BOX_REPLACEMENT_INSET = 0.75f;
    private static final float CHECK_BOX_REPLACEMENT_SIZE = 7.5f;
    private static final float CHECK_BOX_MASK_PADDING = 0.5f;
    private final ObjectMapper json = new ObjectMapper();
    private final JsonNode manifest;
    private final byte[] fontBytes;
    private final Map<String, TemplateAsset> templates = new LinkedHashMap<>();

    private record TemplateAsset(JsonNode metadata, byte[] base, String rendererHash) {}

    public ApplicationFormTemplate() throws IOException {
        try (var in = new ClassPathResource(ROOT + "manifest.json").getInputStream()) { manifest = json.readTree(in); }
        try (var font = new ClassPathResource(ROOT + FONT_RESOURCE).getInputStream()) {
            fontBytes = font.readAllBytes();
        }
        for (JsonNode metadata : manifest.path("templates")) {
            byte[] base;
            try (var in = new ClassPathResource(ROOT + metadata.path("baseResource").asText()).getInputStream()) {
                base = in.readAllBytes();
            }
            if (!hash(base).equals(metadata.path("templateHash").asText())) {
                throw new IOException("投保单模板摘要不匹配: " + metadata.path("name").asText());
            }
            String rendererHash = hash((hash(base) + manifest.toString() + hash(fontBytes))
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            templates.put(key(metadata.path("code").asText(), metadata.path("version").asText()),
                new TemplateAsset(metadata, base, rendererHash));
        }
        if (templates.isEmpty()) throw new IOException("未配置投保单模板");
    }
    public JsonNode metadata() { return manifest; }
    public JsonNode metadata(String code, String version) {
        TemplateAsset asset=resolve(code,version);
        JsonNode copy=manifest.deepCopy();
        String allowance=asset.metadata().path("allowanceDaily").asText();
        for(JsonNode statement:copy.path("statements")) {
            if(!"special".equals(statement.path("key").asText()))continue;
            ArrayNode content=(ArrayNode)statement.path("content");
            for(int i=0;i<content.size();i++)content.set(i,TextNode.valueOf(content.get(i).asText()
                .replace("给付标准为60元/人/天","给付标准为"+allowance+"元/人/天")));
        }
        return copy;
    }
    public String templateHash() { return templateHash(CODE, VERSION); }
    public String templateHash(String code, String version) { return resolve(code, version).rendererHash(); }
    public int insuranceMonths(String code, String version) { return resolve(code, version).metadata().path("insuranceMonths").asInt(); }
    public BigDecimal premium(String code, String version) { return resolve(code, version).metadata().path("premium").decimalValue(); }
    public void assertVersion(String code, String version) {
        resolve(code, version);
    }
    public void validateProduct(InsuranceProductConfig product) {
        if (!Boolean.TRUE.equals(product.getApplicationFormRequired())) return;
        if (!Objects.equals(product.getProductMode(), 1) || !Objects.equals(product.getInsureMode(), 1))
            throw new ServiceException("签字投保单仅支持卡单产品的系统内代投保");
        BigDecimal templatePremium = premium(product.getApplicationTemplateCode(), product.getApplicationTemplateVersion());
        if (product.getMinPremium() == null || templatePremium.compareTo(product.getMinPremium()) != 0)
            throw new ServiceException("产品最低保费必须与所选投保单固定保费一致");
    }
    public void validateFields(Map<String, Object> values) {
        for (JsonNode field : manifest.path("fields")) {
            String value = text(values, field.path("key").asText());
            if (field.path("required").asBoolean() && value.isBlank())
                throw new ServiceException("请填写" + field.path("label").asText());
            if (value.length() > 500) throw new ServiceException(field.path("label").asText() + "过长");
            if (!value.isBlank() && "digit".equals(field.path("type").asText()) && !value.matches("\\d+"))
                throw new ServiceException(field.path("label").asText() + "只能填写数字");
            if (!value.isBlank() && field.has("options")) {
                boolean valid = false;
                for (JsonNode option : field.path("options")) if (option.path("value").asText().equals(value)) valid = true;
                if (!valid) throw new ServiceException(field.path("label").asText() + "选项无效");
            }
        }
    }
    private void required(Map<String, Object> values, String key, String label) {
        if (text(values, key).isBlank()) throw new ServiceException("请填写" + label);
    }
    public static String text(Map<String, ?> values, String key) { return Objects.toString(values.get(key), "").trim(); }
    public static String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    /** 在解码之前检查像素数量；归一化后裁掉空白边缘并只保留 PNG 像素内容。 */
    public static byte[] normalizeSignature(byte[] data) throws IOException {
        if (data.length == 0 || data.length > 1024 * 1024) throw new ServiceException("签字图片须小于1MB");
        BufferedImage image;
        try (var in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) throw new ServiceException("签字图片格式无效");
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                if (!"png".equalsIgnoreCase(reader.getFormatName())) throw new ServiceException("签字图片必须为PNG");
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w < 20 || h < 20 || w > 4096 || h > 2048 || (long) w * h > 4000000)
                    throw new ServiceException("签字图片尺寸无效");
                image = reader.read(0);
            } finally { reader.dispose(); }
        }
        int marks = 0, minX=image.getWidth(), minY=image.getHeight(), maxX=0, maxY=0;
        for (int y=0;y<image.getHeight();y++) for (int x=0;x<image.getWidth();x++) {
            int c=image.getRGB(x,y);
            if ((c >>> 24) > 80 && ((c >> 16) & 255) + ((c >> 8) & 255) + (c & 255) < 600) {
                marks++; minX=Math.min(minX,x); maxX=Math.max(maxX,x); minY=Math.min(minY,y); maxY=Math.max(maxY,y);
            }
        }
        if (marks < 80 || maxX-minX < 15 || maxY-minY < 8) throw new ServiceException("签字内容为空或过于简单，请重新签字");
        int inkWidth=maxX-minX+1, inkHeight=maxY-minY+1;
        int paddingX=Math.max(8,Math.round(inkWidth*0.06f));
        int paddingY=Math.max(8,Math.round(inkHeight*0.10f));
        int left=Math.max(0,minX-paddingX), top=Math.max(0,minY-paddingY);
        int right=Math.min(image.getWidth()-1,maxX+paddingX), bottom=Math.min(image.getHeight()-1,maxY+paddingY);
        BufferedImage cropped=image.getSubimage(left,top,right-left+1,bottom-top+1);
        var out=new ByteArrayOutputStream(); ImageIO.write(cropped,"png",out); return out.toByteArray();
    }
    public byte[] render(Map<String, Object> values, Map<String, byte[]> signatures) throws IOException {
        return render(CODE, VERSION, values, signatures);
    }
    public byte[] render(String code, String version, Map<String, Object> values, Map<String, byte[]> signatures) throws IOException {
        TemplateAsset asset=resolve(code,version);
        try (PDDocument doc=PDDocument.load(asset.base()); var out=new ByteArrayOutputStream();
             var fontIn=new ByteArrayInputStream(fontBytes);
             var fonts=new TrueTypeCollection(fontIn)) {
            TrueTypeFont trueTypeFont=fonts.getFontByName(FONT_NAME);
            if (trueTypeFont == null) throw new IOException("投保单模板字体缺少" + FONT_NAME);
            PDFont font=PDType0Font.load(doc,trueTypeFont,true);
            for (JsonNode f:manifest.path("placements")) {
                String key=f.path("key").asText();
                String value=text(values,key);
                if (value.isBlank()) continue;
                PDPage page=doc.getPage(f.path("page").asInt());
                try(var cs=new PDPageContentStream(doc,page,PDPageContentStream.AppendMode.APPEND,true,true)) {
                    float x=(float)f.path("x").asDouble()+offset(asset.metadata(),"placementX",key);
                    float y=(float)f.path("y").asDouble()+offset(asset.metadata(),"placements",key);
                    float w=(float)f.path("width").asDouble(), h=(float)f.path("height").asDouble();
                    if (f.path("mask").asBoolean(false)) {
                        float inset = 1f;
                        cs.setNonStrokingColor(java.awt.Color.WHITE);
                        cs.addRect(x + inset, page.getMediaBox().getHeight() - y - h + inset,
                            Math.max(0, w - inset * 2), Math.max(0, h - inset * 2));
                        cs.fill();
                    }
                    try {
                        drawText(cs,page,font,value,x,y,w,h,(float)f.path("fontSize").asDouble());
                    } catch (ServiceException e) {
                        throw new ServiceException("模板字段[" + key + "]容纳不下填写内容，请缩短后重新核对");
                    }
                }
            }
            for (JsonNode c:manifest.path("checks")) {
                if (!c.path("value").asText().equals(text(values,c.path("key").asText()))) continue;
                PDPage page=doc.getPage(c.path("page").asInt());
                try(var cs=new PDPageContentStream(doc,page,PDPageContentStream.AppendMode.APPEND,true,true)) {
                    float left=(float)c.path("x").asDouble()
                        +(float)c.path("xOffset").asDouble(CHECK_BOX_X_OFFSET)-CHECK_BOX_REPLACEMENT_INSET
                        +offset(asset.metadata(),"checkX",c.path("key").asText());
                    float top=(float)c.path("y").asDouble()
                        +(float)c.path("yOffset").asDouble(CHECK_BOX_Y_OFFSET)-CHECK_BOX_REPLACEMENT_INSET
                        +offset(asset.metadata(),"checks",c.path("key").asText());
                    float maskSize=CHECK_BOX_REPLACEMENT_SIZE+CHECK_BOX_MASK_PADDING*2;
                    float maskY=page.getMediaBox().getHeight()-(top-CHECK_BOX_MASK_PADDING)-maskSize;
                    cs.setNonStrokingColor(java.awt.Color.WHITE);
                    cs.addRect(left-CHECK_BOX_MASK_PADDING,maskY,maskSize,maskSize);
                    cs.fill();
                    float ascent=font.getFontDescriptor().getAscent()/1000*CHECK_BOX_REPLACEMENT_SIZE;
                    cs.setNonStrokingColor(java.awt.Color.BLACK);cs.beginText();
                    cs.setFont(font,CHECK_BOX_REPLACEMENT_SIZE);
                    cs.newLineAtOffset(left,page.getMediaBox().getHeight()-top-ascent);
                    cs.showText("■");cs.endText();
                }
            }
            for(JsonNode f:manifest.path("signatures")) {
                byte[] data=signatures.get(f.path("key").asText()); if(data==null)continue;
                PDPage page=doc.getPage(f.path("page").asInt());
                BufferedImage img=ImageIO.read(new ByteArrayInputStream(data));
                float maxW=(float)f.path("width").asDouble(), maxH=(float)f.path("height").asDouble();
                float scale=Math.min(maxW/img.getWidth(),maxH/img.getHeight());
                try(var cs=new PDPageContentStream(doc,page,PDPageContentStream.AppendMode.APPEND,true,true)) {
                    cs.drawImage(LosslessFactory.createFromImage(doc,img),(float)f.path("x").asDouble(),
                        page.getMediaBox().getHeight()-(float)f.path("y").asDouble()
                            -offset(asset.metadata(),"signatures",f.path("key").asText())-img.getHeight()*scale,
                        img.getWidth()*scale,img.getHeight()*scale);
                }
            }
            doc.save(out);return out.toByteArray();
        }
    }
    private void drawText(PDPageContentStream cs,PDPage page,PDFont font,String text,float x,float top,float width,float height,float size) throws IOException {
        String value=text.replace('\r',' ').replace('\n',' ').trim();
        float horizontalPadding=Math.min(2f,width*0.05f);
        float availableWidth=width-horizontalPadding*2;
        float availableHeight=height-2;
        float textWidth=font.getStringWidth(value)/1000*size;
        if(textWidth>availableWidth||size>availableHeight)
            throw new ServiceException("模板栏位容纳不下填写内容，请缩短后重新核对");
        float baseline=page.getMediaBox().getHeight()-top-(height+size)/2+1;
        cs.setNonStrokingColor(java.awt.Color.BLACK);cs.beginText();cs.setFont(font,size);
        cs.newLineAtOffset(x+horizontalPadding,baseline);cs.showText(value);cs.endText();
    }
    private TemplateAsset resolve(String code,String version) {
        TemplateAsset asset=templates.get(key(code,version));
        if(asset==null)throw new ServiceException("不支持的投保单模板版本");
        return asset;
    }
    private String key(String code,String version){return Objects.toString(code,"")+"@"+Objects.toString(version,"");}
    private float offset(JsonNode metadata,String group,String field){
        return (float)metadata.path("offsets").path(group).path(field).asDouble(0D);
    }
}
