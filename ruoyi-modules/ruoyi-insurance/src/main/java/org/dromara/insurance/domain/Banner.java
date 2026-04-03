package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 轮播图配置对象 biz_banner
 *
 * @author li.xiang
 * @date 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_banner")
public class Banner extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
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
     * 删除标志
     */
    @TableLogic
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @Version
    private Integer version;


}
