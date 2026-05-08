package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.Date;

/**
 * 通知公告表 sys_notice
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends TenantEntity {

    /**
     * 公告ID
     */
    @TableId(value = "notice_id")
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 是否登录弹窗（0否 1是）
     */
    private String popupFlag;

    /**
     * 弹窗开始时间
     */
    private Date popupStartTime;

    /**
     * 弹窗结束时间
     */
    private Date popupEndTime;

    /**
     * 备注
     */
    private String remark;

}
