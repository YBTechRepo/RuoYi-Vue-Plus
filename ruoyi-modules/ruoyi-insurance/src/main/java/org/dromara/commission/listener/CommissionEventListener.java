package org.dromara.commission.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsurancePolicyMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionEventListener {

    private final IBizCommissionRecordService bizCommissionRecordService;

    private final InsuranceApplyRecordMapper insuranceApplyRecordMapper;

    private final InsurancePolicyMapper insurancePolicyMapper;

    @Async // 🌟 扔进后台线程池
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 🌟 死盯保单 Commit
    public void handlePolicyEvent(PolicyUnderwrittenEvent event) {
        CalcCommission param = event.getCalcParam();
        log.info("【异步算账启动】保单ID: {}", param.getPolicyId());

        // 🌟 防线 1：恢复租户上下文 (MyBatis-Plus 需要它来拼 tenant_id)
        TenantHelper.setDynamic(param.getTenantId());

        try {
            // 🌟 防线 2：开启“上帝视角”，暂时关闭 RuoYi 的数据权限拦截器！
            // 这样底层的查询配置表时，绝不会因为“无登录态”而抛错或漏查数据
            DataPermissionHelper.ignore(() -> {
                // 真正执行算账入库逻辑
                bizCommissionRecordService.calcCommission(param);
                markCommissionSettled(param);
            });

            log.info("【异步算账成功】保单ID: {}", param.getPolicyId());

        } catch (Exception e) {
            log.error("【严重异常：异步算账失败】保单ID: {}", param.getPolicyId(), e);
        } finally {
            // 🌟 防线 3：擦屁股，防止线程池 ThreadLocal 污染
            TenantHelper.clearDynamic();
        }
    }

    private void markCommissionSettled(CalcCommission param) {
        if (Objects.equals(param.getBizSource(), 1)) {
            markApplyRecordCommissionSettled(param);
        } else if (Objects.equals(param.getBizSource(), 2)) {
            markPolicyCommissionSettled(param);
        }
    }

    private void markApplyRecordCommissionSettled(CalcCommission param) {
        int rows = insuranceApplyRecordMapper.update(null, Wrappers.<InsuranceApplyRecord>lambdaUpdate()
            .set(InsuranceApplyRecord::getCommissionStatus, 0)
            .eq(InsuranceApplyRecord::getOrderNo, param.getPolicyNo())
            .eq(InsuranceApplyRecord::getStatus, 0)
            .ne(InsuranceApplyRecord::getCommissionStatus, 0));
        log.info("【投保申请结算状态回写】订单号: {}, 更新行数: {}", param.getPolicyNo(), rows);
    }

    private void markPolicyCommissionSettled(CalcCommission param) {
        int rows = insurancePolicyMapper.update(null, Wrappers.<InsurancePolicy>lambdaUpdate()
            .set(InsurancePolicy::getCommissionStatus, 0)
            .eq(InsurancePolicy::getPolicyNo, param.getPolicyNo())
            .eq(InsurancePolicy::getStatus, 0)
            .ne(InsurancePolicy::getCommissionStatus, 0));
        log.info("【保单结算状态回写】保单号: {}, 更新行数: {}", param.getPolicyNo(), rows);
    }
}
