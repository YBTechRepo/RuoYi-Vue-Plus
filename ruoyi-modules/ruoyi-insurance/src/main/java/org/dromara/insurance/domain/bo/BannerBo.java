package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.Banner;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 轮播图配置业务对象 biz_banner
 *
 * @author li.xiang
 * @date 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Banner.class, reverseConvertGenerate = false)
public class BannerBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 轮播图内容
     */
    private String content;

    /**
     * 图片地址id
     */
    private Long image;

    /**
     * 跳转类型
     */
    private Integer jumpType;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 乐观锁版本
     */
    private Integer version;


}
