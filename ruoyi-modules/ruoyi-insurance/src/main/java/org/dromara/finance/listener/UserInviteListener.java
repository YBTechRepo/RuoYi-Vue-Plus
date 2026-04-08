package org.dromara.finance.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.event.UserInviteSuccessEvent;
import org.dromara.finance.service.IBizUserAccountService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserInviteListener {

    private final IBizUserAccountService bizUserAccountService;

    /**
     * 监听用户邀请成功事件
     *
     * @param event 事件对象
     */


    // 确保只有在 SysUserInviteServiceImpl 中的事务成功提交后，才会触发这里的业务处理
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserInviteSuccess(UserInviteSuccessEvent event) {

        try {
            // 调用业务层的服务
            bizUserAccountService.initAccount(event.getUserId(),event.getUserName(), event.getNickName(), event.getTenantId());
        } catch (Exception e) {
            log.error("处理用户邀请成功事件(账户初始化)失败", e);
            // 注意：由于是 AFTER_COMMIT，这里的异常不会导致系统层的注册逻辑回滚。
            // 如果希望两者具有强一致性，请改用普通的 @EventListener 并将它们包在同一个事务中。
        }
    }
}
