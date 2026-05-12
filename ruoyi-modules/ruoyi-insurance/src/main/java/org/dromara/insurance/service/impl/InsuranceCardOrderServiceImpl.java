package org.dromara.insurance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.InsuranceCardOrder;
import org.dromara.insurance.domain.bo.InsuranceCardOrderBo;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.vo.InsuranceCardOrderVo;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;
import org.dromara.insurance.mapper.InsuranceCardOrderMapper;
import org.dromara.insurance.service.IInsuranceCardOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 卡密订单Service业务层处理
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceCardOrderServiceImpl implements IInsuranceCardOrderService {

    private final InsuranceCardOrderMapper baseMapper;

    private final IBizUserAccountService userAccountService;

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public InsuranceCardOrderVo queryById(Long id) {
        InsuranceCardOrderVo orderVo = baseMapper.selectVoById(id);
        if (orderVo == null) {
            throw new ServiceException("订单不存在或无权查看");
        }
        return orderVo;
    }

    @Override
    public InsuranceCardOrderVo queryAdminById(Long id) {
        return TenantHelper.ignore(() -> baseMapper.selectVoById(id));
    }

    @Override
    public InsuranceCardOrderVo queryByOrderNoForCurrentUser(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new ServiceException("订单号不能为空");
        }
        InsuranceCardOrderVo orderVo = baseMapper.selectVoOne(Wrappers.<InsuranceCardOrder>lambdaQuery()
            .eq(InsuranceCardOrder::getOrderNo, orderNo)
            .eq(InsuranceCardOrder::getAgentUserId, LoginHelper.getUserId())
            .eq(InsuranceCardOrder::getDelFlag, "0"));
        if (orderVo == null) {
            throw new ServiceException("订单不存在或无权查看");
        }
        return orderVo;
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public TableDataInfo<InsuranceCardOrderVo> queryPageList(InsuranceCardOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceCardOrder> lqw = buildQueryWrapper(bo);
        Page<InsuranceCardOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public List<InsuranceCardOrderVo> queryList(InsuranceCardOrderBo bo) {
        LambdaQueryWrapper<InsuranceCardOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public TableDataInfo<InsuranceCardOrderVo> queryAdminPageList(InsuranceCardOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceCardOrder> lqw = buildQueryWrapper(bo);
        Page<InsuranceCardOrderVo> result = TenantHelper.ignore(() -> baseMapper.selectVoPage(pageQuery.build(), lqw));
        return TableDataInfo.build(result);
    }

    @Override
    public List<InsuranceCardOrderVo> queryAdminList(InsuranceCardOrderBo bo) {
        LambdaQueryWrapper<InsuranceCardOrder> lqw = buildQueryWrapper(bo);
        return TenantHelper.ignore(() -> baseMapper.selectVoList(lqw));
    }

    private LambdaQueryWrapper<InsuranceCardOrder> buildQueryWrapper(InsuranceCardOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceCardOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(InsuranceCardOrder::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTenantId()), InsuranceCardOrder::getTenantId, bo.getTenantId());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceCardOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceCardOrder::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceCardOrder::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceCardOrder::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceCardOrder::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceCardOrder::getCustomerMobile, bo.getCustomerMobile());
        lqw.eq(bo.getStatus() != null, InsuranceCardOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getSelectedCompanyCode()), InsuranceCardOrder::getSelectedCompanyCode, bo.getSelectedCompanyCode());
        lqw.eq(StringUtils.isNotBlank(bo.getExpressNo()), InsuranceCardOrder::getExpressNo, bo.getExpressNo());
        return lqw;
    }

    @Override
    public Boolean insertByBo(InsuranceCardOrderBo bo) {
        InsuranceCardOrder add = MapstructUtils.convert(bo, InsuranceCardOrder.class);
        if (add.getStatus() == null) {
            add.setStatus(1);
        }
        if (add.getProductMode() == null) {
            add.setProductMode(3);
        }
        if (add.getPayAmount() == null) {
            add.setPayAmount(calcPayAmount(add));
        }
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(InsuranceCardOrderBo bo) {
        InsuranceCardOrder update = MapstructUtils.convert(bo, InsuranceCardOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(InsuranceCardOrder entity) {
        if (StringUtils.isBlank(entity.getOrderNo())) {
            throw new ServiceException("订单号不能为空");
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return TenantHelper.ignore(() -> baseMapper.deleteByIds(ids)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaveInsureResultVO saveOrderInfo(String orderNo, OrderInsureInfoDTO infoDTO) {
        if (StringUtils.isBlank(orderNo) || infoDTO == null) {
            throw new ServiceException("参数不能为空");
        }
        validateCardOrderInfo(infoDTO);

        InsuranceCardOrder record = baseMapper.selectOne(Wrappers.<InsuranceCardOrder>lambdaQuery()
            .eq(InsuranceCardOrder::getOrderNo, orderNo)
            .eq(InsuranceCardOrder::getAgentUserId, LoginHelper.getUserId())
            .eq(InsuranceCardOrder::getDelFlag, "0"));
        if (record == null) {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (record.getStatus() != null && record.getStatus() == 0) {
            throw new ServiceException("订单已支付，不能修改购买信息");
        }

        BigDecimal payAmount = record.getPayAmount() != null ? record.getPayAmount() : calcPayAmount(record);
        InsuranceCardOrder update = new InsuranceCardOrder();
        update.setId(record.getId());
        update.setCustomerName(infoDTO.getReceiverName());
        update.setCustomerMobile(infoDTO.getReceiverMobile());
        update.setReceiverName(infoDTO.getReceiverName());
        update.setReceiverMobile(infoDTO.getReceiverMobile());
        update.setReceiverAddress(infoDTO.getReceiverAddress());
        update.setSelectedCompanyCode(infoDTO.getSelectedCompanyCode());
        update.setPayAmount(payAmount);
        update.setStatus(3);
        baseMapper.updateById(update);

        SaveInsureResultVO resultVO = new SaveInsureResultVO();
        resultVO.setOrderNo(orderNo);
        resultVO.setPremium(payAmount);
        return resultVO;
    }

    private void validateCardOrderInfo(OrderInsureInfoDTO infoDTO) {
        if (StringUtils.isBlank(infoDTO.getReceiverName())) {
            throw new ServiceException("收货人姓名不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getReceiverMobile())) {
            throw new ServiceException("收货人手机号不能为空");
        }
        if (!infoDTO.getReceiverMobile().matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException("收货人手机号格式不正确");
        }
        if (StringUtils.isBlank(infoDTO.getReceiverAddress())) {
            throw new ServiceException("收货地址不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getSelectedCompanyCode())) {
            throw new ServiceException("请选择保险公司");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payWithBalance(PayWithBalanceReqDTO reqDTO) {
        if (reqDTO == null || StringUtils.isBlank(reqDTO.getOrderNo())) {
            throw new ServiceException("订单号不能为空");
        }
        InsuranceCardOrder record = baseMapper.selectOne(Wrappers.<InsuranceCardOrder>lambdaQuery()
            .eq(InsuranceCardOrder::getOrderNo, reqDTO.getOrderNo())
            .eq(InsuranceCardOrder::getAgentUserId, LoginHelper.getUserId())
            .eq(InsuranceCardOrder::getDelFlag, "0"));
        if (record == null) {
            throw new ServiceException("订单不存在或无权支付");
        }
        if (record.getStatus() != null && record.getStatus() == 0) {
            throw new ServiceException("订单状态异常，请勿重复支付");
        }
        BigDecimal actualAmount = record.getPayAmount() != null ? record.getPayAmount() : calcPayAmount(record);
        if (reqDTO.getPayAmount() != null && reqDTO.getPayAmount().compareTo(actualAmount) != 0) {
            throw new ServiceException("订单金额已发生变化，请重新发起支付");
        }

        userAccountService.deductForOrder(
            record.getAgentUserId(),
            record.getOrderNo(),
            actualAmount,
            "余额支付卡密订单：" + record.getOrderNo() + "_" + record.getProductCode()
        );

        InsuranceCardOrder update = new InsuranceCardOrder();
        update.setId(record.getId());
        update.setStatus(0);
        update.setPayAmount(actualAmount);
        update.setPayTime(new Date());
        baseMapper.updateById(update);
        log.info("卡密订单余额支付成功，单号：{}，不触发佣金计算", record.getOrderNo());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLogistics(InsuranceCardOrderBo bo) {
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("订单ID不能为空");
        }
        if (StringUtils.isBlank(bo.getExpressCompany())) {
            throw new ServiceException("快递公司不能为空");
        }
        if (StringUtils.isBlank(bo.getExpressNo())) {
            throw new ServiceException("快递单号不能为空");
        }
        InsuranceCardOrder update = new InsuranceCardOrder();
        update.setId(bo.getId());
        update.setExpressCompany(bo.getExpressCompany());
        update.setExpressNo(bo.getExpressNo());
        update.setDeliveryTime(new Date());
        update.setStatus(5);
        return TenantHelper.ignore(() -> baseMapper.updateById(update)) > 0;
    }

    private BigDecimal calcPayAmount(InsuranceCardOrder order) {
        BigDecimal goodsAmount = order.getGoodsAmount() == null ? BigDecimal.ZERO : order.getGoodsAmount();
        BigDecimal freightAmount = order.getFreightAmount() == null ? BigDecimal.ZERO : order.getFreightAmount();
        return goodsAmount.add(freightAmount);
    }
}




