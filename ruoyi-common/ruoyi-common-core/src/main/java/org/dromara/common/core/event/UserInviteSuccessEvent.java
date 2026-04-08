package org.dromara.common.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册/新增成功事件
 */
@Getter
public class UserInviteSuccessEvent extends ApplicationEvent {

    private final Long userId;
    private final String userName;
    private final String nickName;
    private final String tenantId;

    public UserInviteSuccessEvent(Object source, Long userId, String userName, String nickName, String tenantId) {
        super(source);
        this.userId = userId;
        this.userName = userName;
        this.nickName = nickName;
        this.tenantId = tenantId;
    }
}
