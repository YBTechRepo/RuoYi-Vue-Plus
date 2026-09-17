package org.dromara.insurance.service;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.service.impl.InsuranceApplyRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("dev")
class ApplicationFormPaymentGuardTest {
    private ApplicationFormGuard guard;
    private InsuranceApplyRecordMapper orders;
    private IBizUserAccountService accounts;
    private ApplicationContext events;
    private InsuranceApplyRecordServiceImpl service;
    private InsuranceApplyRecord order;

    @BeforeEach
    void setup() {
        guard = mock(ApplicationFormGuard.class);
        orders = mock(InsuranceApplyRecordMapper.class);
        accounts = mock(IBizUserAccountService.class);
        events = mock(ApplicationContext.class);
        service = new InsuranceApplyRecordServiceImpl(guard, orders, null, null, null, null,
            accounts, null, null, null, null, null, events, null);
        order = new InsuranceApplyRecord();
        order.setOrderNo("UNSIGNED-001");
        when(orders.selectOne(any())).thenReturn(order);
        when(orders.selectById(1L)).thenReturn(order);
        when(guard.lock(order.getOrderNo())).thenReturn(order);
        doThrow(new ServiceException("请先签署")).when(guard).assertReady(order);
    }

    @Test
    void balancePaymentCannotDeductOrPublishCommissionBeforeReady() {
        var request = new PayWithBalanceReqDTO();
        request.setOrderNo(order.getOrderNo());
        assertThrows(ServiceException.class, () -> service.payWithBalance(request));
        verify(guard).assertReady(order);
        verifyNoInteractions(accounts, events);
    }

    @Test
    void manualConfirmationCannotMarkUnsignedOrderPaid() {
        assertThrows(ServiceException.class, () -> service.handleOrderPaySuccess(1L));
        verify(guard).assertReady(order);
        verify(orders, never()).selectVoById(any());
        verifyNoInteractions(accounts, events);
    }

    @Test
    void batchServiceChecksProductBeforeProcessingRows() {
        var request = new BatchSubmitDTO();
        request.setProductId(1L);
        doThrow(new ServiceException("不能批量投保")).when(guard).assertBatchAllowed(1L);
        assertThrows(ServiceException.class, () -> service.submitBatch(request));
        verify(guard).assertBatchAllowed(1L);
        verifyNoInteractions(orders, accounts, events);
    }
}
