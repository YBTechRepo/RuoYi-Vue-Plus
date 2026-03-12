package org.dromara.commission.listener;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.tenant.helper.TenantHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionEventListener {

    private final IBizCommissionRecordService bizCommissionRecordService;

    @Async // 🌟 扔进后台线程池
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 🌟 死盯保单 Commit
    public void handlePolicyEvent(PolicyUnderwrittenEvent event) {
        CalcCommission param = event.getCalcParam();
        log.info("【异步算账启动】保单ID: {}", param.getPolicyId());

        // 🌟 防线 1：只需要恢复租户上下文即可！(MyBatis-Plus 需要它来拼 tenant_id)
        TenantHelper.setDynamic(param.getTenantId());

        try {
            // 🌟 防线 2：直接脱掉 StpUtil 的马甲，让系统作为“上帝视角”直接去算账！
            bizCommissionRecordService.calcCommission(param);
            log.info("【异步算账成功】保单ID: {}", param.getPolicyId());

        } catch (Exception e) {
            log.error("【严重异常：异步算账失败】保单ID: {}", param.getPolicyId(), e);
        } finally {
            // 🌟 防线 3：擦屁股，防止线程池污染
            TenantHelper.clearDynamic();
        }
    }
}
