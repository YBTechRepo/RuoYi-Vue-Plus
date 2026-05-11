package org.dromara.insurance.service.impl;

import org.dromara.common.tenant.helper.TenantHelper;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.commission.mapper.BizCommissionProductMapper;
import org.dromara.commission.service.IBizCommissionDeptService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.*;
import org.dromara.insurance.domain.bo.ProductCommissionConfig;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.dromara.insurance.domain.dto.BatchInsuredImportDto;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.dto.OrderInsuredItemDTO;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;
import org.dromara.insurance.mapper.*;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.service.IInsuranceApplyRecordService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 投保记录Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceApplyRecordServiceImpl implements IInsuranceApplyRecordService {

    private final InsuranceApplyRecordMapper baseMapper;

    private final InsuranceOrderApplicantMapper insuranceOrderApplicantMapper;

    private final InsuranceOrderInsuredMapper insuranceOrderInsuredMapper;

    private final InsuranceProductCommissionMapper insuranceProductCommissionMapper;

    private final IBizUserAccountService userAccountService;

    private final BizCommissionProductMapper bizCommissionProductMapper;

    private final IBizCommissionDeptService bizCommissionDeptService;

    private final ISysUserService sysUserService;

    private final ISysDeptService sysDeptService;

    private final ApplicationContext applicationContext;

    private final IInsuranceProductConfigService productConfigService;

    /**
     * 查询投保记录
     * @param id 主键
     * @return 投保记录
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public InsuranceApplyRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询投保记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保记录分页列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的投保记录列表
     *
     * @param bo 查询条件
     * @return 投保记录列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public TableDataInfo<InsuranceApplyRecordVo> querySubPageList(String orderNo, PageQuery pageQuery) {
        if (StringUtils.isBlank(orderNo)) {
            return TableDataInfo.build(new Page<>());
        }
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceApplyRecord::getId);
        lqw.eq(InsuranceApplyRecord::getIsBatch, 2);
        lqw.eq(InsuranceApplyRecord::getBatchOrderNo, orderNo);
        Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<Map<String, Object>> querySubOrders(String batchOrderNo) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(batchOrderNo)) {
            return result;
        }

        TenantHelper.ignore(() -> {
            List<InsuranceApplyRecord> subOrders = baseMapper.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
                .eq(InsuranceApplyRecord::getIsBatch, 2)
                .orderByAsc(InsuranceApplyRecord::getId));

            for (InsuranceApplyRecord subOrder : subOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderNo", subOrder.getOrderNo());
                map.put("status", subOrder.getStatus());

                InsuranceOrderApplicant applicant = insuranceOrderApplicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                    .eq(InsuranceOrderApplicant::getOrderNo, subOrder.getOrderNo()));
                map.put("appName", applicant != null ? applicant.getApplicantName() : "");

                InsuranceOrderInsured insured = insuranceOrderInsuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                    .eq(InsuranceOrderInsured::getOrderNo, subOrder.getOrderNo()));
                map.put("insuredName", insured != null ? insured.getInsuredName() : "");

                result.add(map);
            }
        });

        return result;
    }

    @Override
    public Map<String, Object> queryPersonDetail(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(orderNo)) {
            return result;
        }

        result.put("orderNo", orderNo);

        TenantHelper.ignore(() -> {
            InsuranceOrderApplicant applicant = insuranceOrderApplicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, orderNo));
            if (applicant != null) {
                result.put("appName", applicant.getApplicantName());
                result.put("appPhone", applicant.getApplicantPhone());
                result.put("appCertType", applicant.getApplicantCertType());
                result.put("appCertNo", applicant.getApplicantCertNo());
                result.put("appAddress", applicant.getApplicantAddress());
            }

            InsuranceOrderInsured insured = insuranceOrderInsuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getOrderNo, orderNo));
            if (insured != null) {
                result.put("insuredName", insured.getInsuredName());
                result.put("insuredPhone", insured.getInsuredPhone());
                result.put("insuredCertType", insured.getInsuredCertType());
                result.put("insuredCertNo", insured.getInsuredCertNo());
                result.put("relation", insured.getRelation());
                result.put("insuredAddress", insured.getInsuredAddress());
            }
        });

        return result;
    }

    private LambdaQueryWrapper<InsuranceApplyRecord> buildQueryWrapper(InsuranceApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
        lqw.eq(bo.getStatus() != null, InsuranceApplyRecord::getStatus, bo.getStatus());
        lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());

        // 🌟 仅查询普通单 (0) 和 批次主单 (1)，过滤掉批量子单 (2)
        lqw.in(InsuranceApplyRecord::getIsBatch, Arrays.asList(0, 1));

        return lqw;
    }

    /**
     * 新增投保记录
     *
     * @param bo 投保记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord add = MapstructUtils.convert(bo, InsuranceApplyRecord.class);

        //投保模式 0-自投保 1-代投保
        //支付模式 0-常规支付 1-余额代扣
        if(Objects.equals(bo.getInsureMode(), 1) && Objects.equals(bo.getPaymentMode(), 1)){
            add.setStatus(2);
        }

        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改投保记录
     *
     * @param bo 投保记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord update = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceApplyRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除投保记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleOrderPaySuccess(Long orderId) {
        // 1. 根据订单号查出当前订单
        InsuranceApplyRecordVo order = baseMapper.selectVoById(orderId);
        if(order == null){
            throw new ServiceException("订单不存在");
        }
        // 2. 幂等性校验
        if(order.getStatus() == 0){
            return true;
        }

        // 3. 🌟 核心防御：CAS 状态机更新 (绝对防止按钮连点、双重并发)
        // 相当于 SQL: UPDATE table SET status = 2 WHERE id = ? AND status = ?
        int rows = baseMapper.update(null, Wrappers.<InsuranceApplyRecord>lambdaUpdate()
            .set(InsuranceApplyRecord::getStatus, 0)
            .eq(InsuranceApplyRecord::getId, order.getId())
            .eq(InsuranceApplyRecord::getStatus, order.getStatus()) // 要求旧状态必须一致
        );

        // 如果 rows == 0，说明被别的连点请求抢先更新了，直接当作成功返回，阻断后续算佣金
        if (rows == 0){
            log.warn("并发拦截：订单 {} 已被处理", order.getOrderNo());
            return true;
        }

        // 4. 构建佣金计算参数
        CalcCommission calcParam = buildCalcParam(order);

        // 5. 触发佣金计算逻辑
        applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaveInsureResultVO saveInsureInfo(String orderNo, OrderInsureInfoDTO infoDTO) {
        if (StringUtils.isBlank(orderNo) || infoDTO == null) {
            throw new ServiceException("参数不能为空");
        }

        // ==========================================
        // 0. 查验主订单；卡密订单会走专属收货信息保存，不落投被保人表
        // ==========================================
        InsuranceApplyRecord record = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo, orderNo)
            .eq(InsuranceApplyRecord::getDelFlag, "0"));

        if (record == null) {
            throw new ServiceException("无效的订单记录");
        }

        if (Objects.equals(record.getProductMode(), 3) && Objects.equals(record.getInsureMode(), 2)) {
            return saveCardSecretOrderInfo(orderNo, infoDTO, record);
        }

        validateRegularInsureInfo(infoDTO);

        // ==========================================
        // 1. 幂等性防线：先清理旧数据，防止重复提交产生冗余
        // ==========================================
        insuranceOrderApplicantMapper.delete(new LambdaQueryWrapper<InsuranceOrderApplicant>()
            .eq(InsuranceOrderApplicant::getOrderNo, orderNo));
        insuranceOrderInsuredMapper.delete(new LambdaQueryWrapper<InsuranceOrderInsured>()
            .eq(InsuranceOrderInsured::getOrderNo, orderNo));

        // ==========================================
        // 2. 保存投被保人信息 (优化为批量操作)
        // ==========================================
        // 2.1 保存投保人
        InsuranceOrderApplicant applicant = new InsuranceOrderApplicant();
        BeanUtils.copyProperties(infoDTO, applicant); // 建议用 BeanUtils 偷懒，前提是字段名一致
        applicant.setOrderNo(orderNo);
        insuranceOrderApplicantMapper.insert(applicant);

        // 2.2 批量保存被保人 (必须用 saveBatch 提升性能)
        List<OrderInsuredItemDTO> insuredDtoList = infoDTO.getInsuredList();
        if (CollUtil.isNotEmpty(insuredDtoList)) {
            List<InsuranceOrderInsured> insuredEntityList = new ArrayList<>();
            for (OrderInsuredItemDTO item : insuredDtoList) {
                InsuranceOrderInsured insured = new InsuranceOrderInsured();
                BeanUtils.copyProperties(item, insured);
                insured.setOrderNo(orderNo);
                insuredEntityList.add(insured);
            }
            insuranceOrderInsuredMapper.insertBatch(insuredEntityList);
        }

        // 准备通用的返回对象
        SaveInsureResultVO saveInsureResultVO = new SaveInsureResultVO();
        saveInsureResultVO.setOrderNo(orderNo);

        // ==========================================
        // 4. 净费逻辑判断（如果不是代投保或不是净费，直接返回原价）
        // ==========================================
        if (record.getInsureMode() != 1 || record.getPaymentMode() != 1) {
            saveInsureResultVO.setPremium(record.getPremium());
            return saveInsureResultVO; // 🚀 提前返回！
        }

        // ==========================================
        // 5. 净费计算核心逻辑
        // ==========================================
        BigDecimal finalRate = calculateFinalCommissionRate(record);

        // 如果未查到佣金配置，按原价返回（或者根据业务抛出异常）
        if (finalRate.compareTo(BigDecimal.ZERO) == 0) {
            saveInsureResultVO.setPremium(record.getPremium());
            //变更订单状态 - 待支付
            record.setNetPremium(record.getPremium());
            record.setStatus(3);
            baseMapper.updateById(record);
            return saveInsureResultVO;
        }

        // 计算净费：金额必须限制两位小数，四舍五入！
        BigDecimal netPremium = record.getPremium()
            .multiply(BigDecimal.ONE.subtract(finalRate))
            .setScale(2, RoundingMode.HALF_UP);

        // 落库更新
        record.setNetPremium(netPremium);

        //待支付
        record.setStatus(3);

        baseMapper.updateById(record);

        // 返回净费
        saveInsureResultVO.setPremium(netPremium);
        return saveInsureResultVO;
    }

    private SaveInsureResultVO saveCardSecretOrderInfo(String orderNo, OrderInsureInfoDTO infoDTO, InsuranceApplyRecord record) {
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

        InsuranceApplyRecord update = new InsuranceApplyRecord();
        update.setId(record.getId());
        update.setCustomerName(infoDTO.getReceiverName());
        update.setCustomerMobile(infoDTO.getReceiverMobile());
        update.setReceiverName(infoDTO.getReceiverName());
        update.setReceiverMobile(infoDTO.getReceiverMobile());
        update.setReceiverAddress(infoDTO.getReceiverAddress());
        update.setSelectedCompanyCode(infoDTO.getSelectedCompanyCode());
        update.setNetPremium(record.getPremium());
        update.setStatus(3);
        baseMapper.updateById(update);

        SaveInsureResultVO resultVO = new SaveInsureResultVO();
        resultVO.setOrderNo(orderNo);
        resultVO.setPremium(record.getPremium());
        return resultVO;
    }

    private void validateRegularInsureInfo(OrderInsureInfoDTO infoDTO) {
        if (StringUtils.isBlank(infoDTO.getApplicantName())) {
            throw new ServiceException("投保人姓名不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getApplicantCertType())) {
            throw new ServiceException("请选择投保人证件类型");
        }
        if (StringUtils.isBlank(infoDTO.getApplicantCertNo())) {
            throw new ServiceException("投保人证件号码不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getCertStartDate())) {
            throw new ServiceException("投保人证件生效起期不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getCertEndDate())) {
            throw new ServiceException("投保人证件生效止期不能为空");
        }
        if (StringUtils.isBlank(infoDTO.getApplicantPhone())) {
            throw new ServiceException("投保人手机号不能为空");
        }
        if (!infoDTO.getApplicantPhone().matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException("投保人手机号格式不正确");
        }
        if (StringUtils.isBlank(infoDTO.getApplicantAddress())) {
            throw new ServiceException("投保人地址不能为空");
        }
        if (CollUtil.isEmpty(infoDTO.getInsuredList())) {
            throw new ServiceException("至少需要填写一名被保人信息");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payWithBalance(PayWithBalanceReqDTO reqDTO) {
        String orderNo = reqDTO.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            throw new ServiceException("订单号不能为空");
        }
        // ==========================================
        // 1. 安全防线：查数据库，锁定真实金额与状态
        // ==========================================
        InsuranceApplyRecord record = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo, orderNo)
            .eq(InsuranceApplyRecord::getDelFlag, "0"));

        if (record == null) {
            throw new ServiceException("订单不存在");
        }

        // 防重付拦截
        if (record.getStatus() == 0) {
            throw new ServiceException("订单状态异常，请勿重复支付");
        }

        // 防越权：确保当前登录人只能支付自己的订单
        if (!record.getAgentUserId().equals(LoginHelper.getUserId())) {
            throw new ServiceException("非法操作：无权支付此订单");
        }

        // 🌟 【安全校验可选】核对前端金额与数据库金额是否一致（防前端显示错误导致客诉）
        // 注意：这里使用的是 record.getNetPremium() 或 getPremium()，即上一步算好的真实保费
        BigDecimal actualAmount = record.getNetPremium() != null ? record.getNetPremium() : record.getPremium();
        if (reqDTO.getPayAmount() != null && reqDTO.getPayAmount().compareTo(actualAmount) != 0) {
            throw new ServiceException("订单金额已发生变化，请重新发起支付");
        }

        // ==========================================
        // 2. 扣费（必须使用数据库里的 actualAmount）
        // ==========================================
        // 调用之前写好的神级账户扣款方法 (内含乐观锁和资金流水记录)
        userAccountService.deductForOrder(
            record.getAgentUserId(),
            orderNo,
            actualAmount, // 🛑 绝对不能用 reqDTO.getPayAmount() !
            "余额支付订单：" + orderNo + "_" + record.getProductCode()
        );

        // ==========================================
        // 3. 变更订单支付状态
        // ==========================================
        InsuranceApplyRecord updateRecord = new InsuranceApplyRecord();
        updateRecord.setId(record.getId());
        updateRecord.setStatus(0);
        updateRecord.setPayTime(new Date());
        baseMapper.updateById(updateRecord);

        // ==========================================
        // 4. 触发佣金计算逻辑 (捕获异常，防止影响扣款事务)
        // ==========================================
        try {
            InsuranceApplyRecordVo recordVo = MapstructUtils.convert(record, InsuranceApplyRecordVo.class);
            CalcCommission calcParam = buildCalcParam(recordVo);

            // 🌟 将支付方式、真实的付款人以及保单保费传给下游！
            calcParam.setPaymentMode(record.getPaymentMode());
            calcParam.setPayerUserId(LoginHelper.getUserId());
            calcParam.setPolicyPremium(record.getPremium());
            calcParam.setNetPremium(record.getNetPremium());

            log.info("佣金计算参数：{}",JsonUtils.toJsonString(calcParam));

            applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));
            log.info("余额支付成功，已异步抛出佣金计算事件，单号：{}", orderNo);
        } catch (Exception e) {
            log.error("余额支付成功，但触发佣金计算事件失败，单号：{}，原因：{}", orderNo, e.getMessage(), e);
        }

        return true;
    }

    /**
     * 核心私有方法：计算该订单最终的佣金抵扣比例
     *
     * @param record 投保申请记录
     * @return 最终抵扣比例 (如 0.35)，如果为 0 则代表按原价出单
     */
    private BigDecimal calculateFinalCommissionRate(InsuranceApplyRecord record) {

        // ==========================================
        // 1. 查询产品基准费率配置 (Base Rate)
        // ==========================================
        InsuranceProductCommission productCommission = insuranceProductCommissionMapper.selectOne(
            new LambdaQueryWrapper<InsuranceProductCommission>()
                .eq(InsuranceProductCommission::getProductId, record.getProductId())
                .eq(InsuranceProductCommission::getProductCode, record.getProductCode())
                .eq(InsuranceProductCommission::getDelFlag, "0")
        );

        if (productCommission == null || StringUtils.isBlank(productCommission.getCommissionConfig())) {
            return BigDecimal.ZERO;
        }

        // 解析 JSON 配置，并根据当前时间匹配生效的费率
        Date now = new Date();
        List<ProductCommissionConfig> configs = JsonUtils.parseArray(productCommission.getCommissionConfig(), ProductCommissionConfig.class);
        BigDecimal baseRate = configs.stream()
            .filter(c -> (c.getEffectiveTime() == null || now.after(c.getEffectiveTime()))
                && (c.getExpirationTime() == null || now.before(c.getExpirationTime())))
            .findFirst()
            .map(ProductCommissionConfig::getCommissionRate)
            .orElse(BigDecimal.ZERO);

        // 如果基准费率为0，直接返回，避免后续无效计算
        if (baseRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // ==========================================
        // 2. 获取当前用户的机构（部门）通用比例
        // ==========================================
        Long deptId = LoginHelper.getDeptId(); // 假设上层已有 @SaCheckLogin 拦截
        Long topLevelDeptId = deptId;
        SysDeptVo currentDept = sysDeptService.selectDeptById(deptId);

        // 寻找顶级部门 ID
        if (currentDept != null && StringUtils.isNotBlank(currentDept.getAncestors())) {
            String[] ids = currentDept.getAncestors().split(",");
            if (ids.length > 1) {
                topLevelDeptId = Long.valueOf(ids[1]);
            }
        }

        BizCommissionDept bizCommissionDept = bizCommissionDeptService.queryByDeptId(topLevelDeptId);
        // 🛡️ 防空指针：取不到则默认为 0
        BigDecimal normalBizRatio = bizCommissionDept != null && bizCommissionDept.getSalesRatio() != null ? bizCommissionDept.getSalesRatio() : BigDecimal.ZERO;
        BigDecimal normalTeamRatio = bizCommissionDept != null && bizCommissionDept.getTeamRatio() != null ? bizCommissionDept.getTeamRatio() : BigDecimal.ZERO;
        BigDecimal normalLeaderRatio = bizCommissionDept != null && bizCommissionDept.getProjectRatio() != null ? bizCommissionDept.getProjectRatio() : BigDecimal.ZERO;

        // ==========================================
        // 3. 获取产品的特殊定制比例 (优先级高于机构比例)
        // ==========================================
        BizCommissionProduct bizCommissionProduct = bizCommissionProductMapper.selectOne(
            new LambdaQueryWrapper<BizCommissionProduct>()
                .eq(BizCommissionProduct::getProductId, record.getProductId())
                .eq(BizCommissionProduct::getProductCode, record.getProductCode())
                .eq(BizCommissionProduct::getStatus, 0)
                .eq(BizCommissionProduct::getDelFlag, "0")
        );

        // 💡 优雅的三元表达式覆盖：如果产品配了专属比例，就用产品的；否则沿用机构的通用比例
        BigDecimal currentBizRatio = bizCommissionProduct != null && bizCommissionProduct.getSalesRatio() != null ? bizCommissionProduct.getSalesRatio() : normalBizRatio;
        BigDecimal currentTeamRatio = bizCommissionProduct != null && bizCommissionProduct.getTeamRatio() != null ? bizCommissionProduct.getTeamRatio() : normalTeamRatio;
        BigDecimal currentLeaderRatio = bizCommissionProduct != null && bizCommissionProduct.getProjectRatio() != null ? bizCommissionProduct.getProjectRatio() : normalLeaderRatio;

        // ==========================================
        // 4. 根据当前用户的角色进行比例累加
        // ==========================================
        BigDecimal finalRate = BigDecimal.ZERO;

        boolean isLeader = StpUtil.hasRole("leader");         // 顶级项目总监
        boolean isTeamLeader = StpUtil.hasRole("teamleader"); // 团队长
        boolean isBizMan = StpUtil.hasRole("bizman");         // 基层业务员

        if (isLeader) {
            // 总监：拿自己作为业务员的钱 + 团队长的钱 + 总监的钱
            finalRate = baseRate.multiply(currentBizRatio)
                .add(baseRate.multiply(currentTeamRatio))
                .add(baseRate.multiply(currentLeaderRatio));
        } else if (isTeamLeader) {
            // 团队长：拿自己作为业务员的钱 + 团队长的钱
            finalRate = baseRate.multiply(currentBizRatio)
                .add(baseRate.multiply(currentTeamRatio));
        } else if (isBizMan) {
            // 基层业务员：只拿业务员的钱
            finalRate = baseRate.multiply(currentBizRatio);
        }

        return finalRate;
    }

    /**
     * 构建佣金计算参数
     */
    private CalcCommission buildCalcParam(InsuranceApplyRecordVo order) {
        Long agentUserId = order.getAgentUserId();
        Long agentDeptId = order.getAgentDeptId();
        SysUserVo salesUser = sysUserService.selectUserById(agentUserId);
        if (salesUser == null) {
            throw new ServiceException("业务员信息不存在");
        }

        CalcCommission calcParam = new CalcCommission();
        calcParam.setPolicyId(order.getId());
        calcParam.setPolicyNo(order.getOrderNo());
        calcParam.setProductId(order.getProductId());
        calcParam.setProductName(order.getProductName());
        calcParam.setTenantId(salesUser.getTenantId());
        calcParam.setCreateById(agentUserId);
        calcParam.setCreateDeptId(agentDeptId);
        calcParam.setPolicyPremium(order.getPremium());
        // 🌟 补充实交保费：优先取订单里的净费(并且必须大于0)，没有则取原价
        BigDecimal actualPaid = (order.getNetPremium() != null && order.getNetPremium().compareTo(BigDecimal.ZERO) > 0) ? order.getNetPremium() : order.getPremium();
        calcParam.setNetPremium(actualPaid);

        // 🌟 角色寻址逻辑 (同步自 OpenPolicyFacadeServiceImpl)
        List<SysRoleVo> roles = salesUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new ServiceException("该业务员未配置角色，无法计算佣金");
        }
        String roleKey = roles.get(0).getRoleKey();
        SysDeptVo currentDept = sysDeptService.selectDeptById(agentDeptId);
        if (currentDept == null) {
            throw new ServiceException("业务员所属机构不存在");
        }
        String deptCategory = currentDept.getDeptCategory();

        if ("bizman".equals(roleKey)) { // 业务员
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());

            Long directLeaderId = currentDept.getLeader();
            SysUserVo directLeader = directLeaderId != null ? sysUserService.selectUserById(directLeaderId) : null;
            String directLeaderName = directLeader != null ? directLeader.getNickName() : "";

            if ("2".equals(deptCategory)) { // 挂在团队下
                calcParam.setTeamUserId(directLeaderId);
                calcParam.setTeamUserName(directLeaderName);

                SysDeptVo parentDept = sysDeptService.selectDeptById(currentDept.getParentId());
                if (parentDept != null) {
                    Long projectLeaderId = parentDept.getLeader();
                    SysUserVo projectLeader = projectLeaderId != null ? sysUserService.selectUserById(projectLeaderId) : null;
                    calcParam.setProjectUserId(projectLeaderId);
                    calcParam.setProjectUserName(projectLeader != null ? projectLeader.getNickName() : "");
                }
            } else if ("1".equals(deptCategory)) { // 越级直挂项目组
                calcParam.setTeamUserId(null);
                calcParam.setTeamUserName("");
                calcParam.setProjectUserId(directLeaderId);
                calcParam.setProjectUserName(directLeaderName);
            }
        } else if ("teamleader".equals(roleKey)) { // 团队负责人自己出单
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());
            calcParam.setTeamUserId(agentUserId);
            calcParam.setTeamUserName(salesUser.getNickName());

            SysDeptVo parentDept = sysDeptService.selectDeptById(currentDept.getParentId());
            if (parentDept != null) {
                Long projectLeaderId = parentDept.getLeader();
                SysUserVo projectLeader = projectLeaderId != null ? sysUserService.selectUserById(projectLeaderId) : null;
                calcParam.setProjectUserId(projectLeaderId);
                calcParam.setProjectUserName(projectLeader != null ? projectLeader.getNickName() : "");
            }
        } else { // 项目负责人（大老板）亲自出单
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());
            calcParam.setTeamUserId(agentUserId);
            calcParam.setTeamUserName(salesUser.getNickName());
            calcParam.setProjectUserId(agentUserId);
            calcParam.setProjectUserName(salesUser.getNickName());
        }
        return calcParam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitBatch(BatchSubmitDTO submitDTO) {
        if (submitDTO == null || CollUtil.isEmpty(submitDTO.getAuditList())) {
            throw new ServiceException("投保人员名单不能为空");
        }

        // 1. 获取产品信息并计算金额 (逻辑同 preview)
        InsuranceSalesProductVo productVo = productConfigService.querySalesProductById(submitDTO.getProductId());
        if (productVo == null) {
            throw new ServiceException("产品不存在或已下架");
        }

        BigDecimal grossPremium = productVo.getMinPremium() != null ? productVo.getMinPremium() : BigDecimal.ZERO;
        BigDecimal commissionRate = productVo.getDisplayCommissionRate() != null ? productVo.getDisplayCommissionRate() : BigDecimal.ZERO;
        // 计算单人净保费：gross * (1 - rate)
        BigDecimal netPremium = grossPremium.multiply(BigDecimal.ONE.subtract(commissionRate)).setScale(2, RoundingMode.HALF_UP);

        int validCount = submitDTO.getAuditList().size();
        BigDecimal totalNetPremium = netPremium.multiply(new BigDecimal(validCount)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalGrossPremium = grossPremium.multiply(new BigDecimal(validCount)).setScale(2, RoundingMode.HALF_UP);

        // 2. 扣款
        Long currentUserId = LoginHelper.getUserId();
        String batchOrderNo = "BH" + cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMddHHmmss") + cn.hutool.core.util.RandomUtil.randomNumbers(4);
        userAccountService.deductForOrder(
            currentUserId,
            batchOrderNo,
            totalNetPremium,
            "批量投保扣款：" + batchOrderNo + "_" + productVo.getProductName()
        );

        // 3. 生成批次主单 (status=0 已支付)
        InsuranceApplyRecord mainOrder = new InsuranceApplyRecord();
        mainOrder.setOrderNo(batchOrderNo);
        mainOrder.setProductId(productVo.getId());
        mainOrder.setProductCode(productVo.getProductCode());
        mainOrder.setProductName(productVo.getProductName());
        mainOrder.setAgentUserId(currentUserId);
        mainOrder.setAgentName(LoginHelper.getUsername());
        mainOrder.setAgentDeptId(LoginHelper.getDeptId());
        mainOrder.setPremium(totalGrossPremium);
        mainOrder.setNetPremium(totalNetPremium);

        // 🌟 增加客户信息赋值
        if (CollUtil.isNotEmpty(submitDTO.getAuditList())) {
            BatchInsuredImportDto firstPerson = submitDTO.getAuditList().get(0);
            mainOrder.setCustomerName(firstPerson.getName() + "等" + validCount + "人");
            mainOrder.setCustomerMobile(firstPerson.getPhone());
        }

        mainOrder.setStatus(0);
        // 主单
        mainOrder.setIsBatch(1);
        //代投保
        mainOrder.setInsureMode(1);
        // 余额支付
        mainOrder.setPaymentMode(1);
        mainOrder.setPayTime(new Date());
        baseMapper.insert(mainOrder);

        // 4. 循环生成子单及详细信息
        int subIndex = 1;
        for (BatchInsuredImportDto dto : submitDTO.getAuditList()) {
            // 子单号：主单号 + 4位自增序号 (例如: BATCH202604131234561234-0001)
            String subOrderNo = batchOrderNo + "-" + String.format("%04d", subIndex++);

            // 子单记录
            InsuranceApplyRecord subOrder = new InsuranceApplyRecord();
            BeanUtils.copyProperties(mainOrder, subOrder);
            subOrder.setId(null);
            subOrder.setOrderNo(subOrderNo);
            subOrder.setBatchOrderNo(batchOrderNo);
            subOrder.setIsBatch(2); // 强制覆盖为主单拷贝过来的属性
            subOrder.setInsureMode(1);
            subOrder.setPaymentMode(1);
            subOrder.setPremium(grossPremium);
            subOrder.setNetPremium(netPremium);
            subOrder.setCustomerName(dto.getName());
            subOrder.setCustomerMobile(dto.getPhone());
            baseMapper.insert(subOrder);

            // 投保人记录
            InsuranceOrderApplicant applicant = new InsuranceOrderApplicant();
            applicant.setOrderNo(subOrderNo);
            applicant.setApplicantName(dto.getAppName());
            applicant.setApplicantCertType(dto.getAppCertType());
            applicant.setApplicantCertNo(dto.getAppCertNo());
            applicant.setApplicantPhone(dto.getAppPhone());
            applicant.setApplicantAddress(dto.getAppAddress());
            insuranceOrderApplicantMapper.insert(applicant);

            // 被保人记录
            InsuranceOrderInsured insured = new InsuranceOrderInsured();
            insured.setOrderNo(subOrderNo);
            insured.setRelation(dto.getRelation());
            insured.setInsuredName(dto.getName());
            insured.setInsuredCertType(dto.getCertType());
            insured.setInsuredCertNo(dto.getCertNo());
            insured.setInsuredPhone(dto.getPhone());
            insured.setInsuredAddress(dto.getAddress());
            insuranceOrderInsuredMapper.insert(insured);
        }

        // 5. 触发佣金计算 (仅为主单触发一次，计算总额分润)
        try {
            InsuranceApplyRecordVo mainOrderVo = MapstructUtils.convert(mainOrder, InsuranceApplyRecordVo.class);
            CalcCommission calcParam = buildCalcParam(mainOrderVo);

            // 🌟 补充主单特有信息 (按照 payWithBalance 方式)
            calcParam.setPaymentMode(mainOrder.getPaymentMode());
            calcParam.setPayerUserId(currentUserId);
            calcParam.setPolicyPremium(totalGrossPremium); // 使用总保单保费计算
            calcParam.setNetPremium(totalNetPremium); // 🌟 补充实交保费为批次总净费

            applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));
            log.info("批次主单 {} 佣金计算事件已触发", batchOrderNo);
        } catch (Exception e) {
            log.error("批次主单 {} 佣金计算事件触发失败", batchOrderNo, e);
        }

        return batchOrderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(InsuranceApplyRecordBo bo) {
        if (bo == null || bo.getId() == null) {
            throw new ServiceException("参数错误，无法取消订单");
        }

        InsuranceApplyRecord record = baseMapper.selectById(bo.getId());
        if (record == null) {
            throw new ServiceException("订单不存在");
        }

        // 防越权：确保当前登录人只能取消自己的订单
        if (!record.getAgentUserId().equals(LoginHelper.getUserId())) {
            throw new ServiceException("非法操作：无权取消此订单");
        }

        InsuranceApplyRecord updateOrder = new InsuranceApplyRecord();
        updateOrder.setId(bo.getId());
        updateOrder.setStatus(4); // 4为取消状态

        return baseMapper.updateById(updateOrder) > 0;
    }
}
