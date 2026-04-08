package org.dromara.insurance.domain.vo;

import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.insurance.domain.Banner;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 轮播图配置视图对象 biz_banner
 *
 * @author li.xiang
 * @date 2026-03-31
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Banner.class)
public class BannerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    //@ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 轮播图标题
     */
    @ExcelProperty(value = "轮播图标题")
    private String title;

    /**
     * 轮播图内容
     */
    @ExcelProperty(value = "轮播图内容")
    private String content;

    /**
     * 图片地址id
     */
    @ExcelProperty(value = "图片地址id")
    private Long image;

    /**
     * 图片地址idUrl
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "image")
    private String imageUrl;
    /**
     * 跳转类型
     */
    @ExcelProperty(value = "跳转类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "banner_jump_type")
    private Integer jumpType;

    /**
     * 跳转链接
     */
    @ExcelProperty(value = "跳转链接")
    private String jumpUrl;

    /**
     * 排序
     */
    @ExcelProperty(value = "排序")
    private Integer sortOrder;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "banner_image_status")
    private Integer status;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 乐观锁版本
     */
    //@ExcelProperty(value = "乐观锁版本")
    private Integer version;

    /**
     * 租户ID
     */
    @ExcelProperty(value = "租户ID")
    private String tenantId;


}
