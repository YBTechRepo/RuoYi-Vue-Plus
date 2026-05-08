package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 通知公告已读记录 sys_notice_read
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_read")
public class SysNoticeRead extends TenantEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 已读时间
     */
    private Date readTime;
}
