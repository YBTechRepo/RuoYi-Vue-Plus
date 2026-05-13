package org.dromara.insurance.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.tenant.helper.TenantHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.dromara.insurance.service.IInsuranceProxyOrderService;
import org.dromara.system.domain.vo.SysTenantVo;
import org.dromara.system.service.ISysTenantService;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 代投保订单查询Service业务层处理 (Admin全局视图)
 *
 * @author li.xiang
 * @date 2026-04-14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProxyOrderServiceImpl implements IInsuranceProxyOrderService {

    private final InsuranceApplyRecordMapper baseMapper;
    private final InsuranceOrderApplicantMapper applicantMapper;
    private final InsuranceOrderInsuredMapper insuredMapper;
    private final ISysTenantService sysTenantService;

    /**
     * 查询代投保订单查询
     *
     * @param id 主键
     * @return 代投保订单查询
     */
    @Override
    public InsuranceApplyRecordVo queryById(Long id){
        return TenantHelper.ignore(() -> {
            InsuranceApplyRecordVo vo = baseMapper.selectVoOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getId, id)
                .eq(InsuranceApplyRecord::getInsureMode, 1));
            fillTenantName(vo);
            return vo;
        });
    }

    /**
     * 分页查询代投保订单查询列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 代投保订单查询分页列表
     */
    @Override
    public TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> {
            LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
            Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
            fillTenantNames(result.getRecords());
            return TableDataInfo.build(result);
        });
    }

    /**
     * 查询符合条件的代投保订单查询列表
     *
     * @param bo 查询条件
     * @return 代投保订单查询列表
     */
    @Override
    public List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo) {
        return TenantHelper.ignore(() -> {
            LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
            List<InsuranceApplyRecordVo> list = baseMapper.selectVoList(lqw);
            fillTenantNames(list);
            return list;
        });
    }

    @Override
    public List<InsuranceApplyRecordVo> exportList(InsuranceApplyRecordBo bo) {
        return TenantHelper.ignore(() -> {
            Map<String, Object> params = bo.getParams();
            LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();

            // 🌟 核心强制过滤：仅代投保模式
            lqw.eq(InsuranceApplyRecord::getInsureMode, 1);
            // 🌟 导出场景过滤：仅普通单(0)和批量子单(2)
            lqw.in(InsuranceApplyRecord::getIsBatch, Arrays.asList(0, 2));
            // 🌟 导出场景过滤：状态仅为已支付(0)
            lqw.eq(InsuranceApplyRecord::getStatus, 0);

            lqw.orderByDesc(InsuranceApplyRecord::getId);
            lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
            lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
            lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
            lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
            lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
            lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
            lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());

            if (params != null && params.get("beginTime") != null && params.get("endTime") != null) {
                lqw.between(InsuranceApplyRecord::getCreateTime, params.get("beginTime"), params.get("endTime") + " 23:59:59");
            }

            List<InsuranceApplyRecordVo> list = baseMapper.selectVoList(lqw);
            fillTenantNames(list);

            // 补充投被保人信息
            for (InsuranceApplyRecordVo vo : list) {
                InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                    .eq(InsuranceOrderApplicant::getOrderNo, vo.getOrderNo()));
                if (applicant != null) {
                    vo.setAppName(applicant.getApplicantName());
                    vo.setAppCertType(applicant.getApplicantCertType());
                    vo.setAppCertNo(applicant.getApplicantCertNo());
                    vo.setAppPhone(applicant.getApplicantPhone());
                    vo.setAppAddress(applicant.getApplicantAddress());
                }

                InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                    .eq(InsuranceOrderInsured::getOrderNo, vo.getOrderNo()));
                if (insured != null) {
                    vo.setRelation(insured.getRelation());
                    vo.setInsuredName(insured.getInsuredName());
                    vo.setInsuredCertType(insured.getInsuredCertType());
                    vo.setInsuredCertNo(insured.getInsuredCertNo());
                    vo.setInsuredPhone(insured.getInsuredPhone());
                    vo.setInsuredAddress(insured.getInsuredAddress());
                }
            }

            return list;
        });
    }

    @Override
    public List<Map<String, Object>> querySubOrders(String batchOrderNo) {
        return TenantHelper.ignore(() -> {
            List<Map<String, Object>> result = new ArrayList<>();
            if (StringUtils.isBlank(batchOrderNo)) {
                return result;
            }

            List<InsuranceApplyRecord> subOrders = baseMapper.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
                .eq(InsuranceApplyRecord::getIsBatch, 2)
                .orderByAsc(InsuranceApplyRecord::getId));

            for (InsuranceApplyRecord subOrder : subOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderNo", subOrder.getOrderNo());
                map.put("status", subOrder.getStatus());

                InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                    .eq(InsuranceOrderApplicant::getOrderNo, subOrder.getOrderNo()));
                map.put("appName", applicant != null ? applicant.getApplicantName() : "");

                InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                    .eq(InsuranceOrderInsured::getOrderNo, subOrder.getOrderNo()));
                map.put("insuredName", insured != null ? insured.getInsuredName() : "");

                result.add(map);
            }
            return result;
        });
    }

    @Override
    public Map<String, Object> queryPersonDetail(String orderNo) {
        return TenantHelper.ignore(() -> {
            Map<String, Object> result = new HashMap<>();
            if (StringUtils.isBlank(orderNo)) {
                return result;
            }

            result.put("orderNo", orderNo);

            InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, orderNo));
            if (applicant != null) {
                result.put("appName", applicant.getApplicantName());
                result.put("appPhone", applicant.getApplicantPhone());
                result.put("appCertType", applicant.getApplicantCertType());
                result.put("appCertNo", applicant.getApplicantCertNo());
                result.put("appAddress", applicant.getApplicantAddress());
            }

            InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getOrderNo, orderNo));
            if (insured != null) {
                result.put("insuredName", insured.getInsuredName());
                result.put("insuredPhone", insured.getInsuredPhone());
                result.put("insuredCertType", insured.getInsuredCertType());
                result.put("insuredCertNo", insured.getInsuredCertNo());
                result.put("relation", insured.getRelation());
                result.put("insuredAddress", insured.getInsuredAddress());
            }

            return result;
        });
    }

    private LambdaQueryWrapper<InsuranceApplyRecord> buildQueryWrapper(InsuranceApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();

        // 🌟 核心强制过滤：仅代投保模式
        lqw.eq(InsuranceApplyRecord::getInsureMode, 1);
        // 🌟 核心强制过滤：仅显示普通单和批次主单
        lqw.in(InsuranceApplyRecord::getIsBatch, Arrays.asList(0, 1));

        lqw.orderByDesc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
        //lqw.eq(bo.getStatus() != null, InsuranceApplyRecord::getStatus, bo.getStatus());
        lqw.eq(InsuranceApplyRecord::getStatus,0);
        lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());

        if (params.get("beginTime") != null && params.get("endTime") != null) {
            lqw.between(InsuranceApplyRecord::getCreateTime, params.get("beginTime"), params.get("endTime") + " 23:59:59");
        }

        return lqw;
    }

    /**
     * 补充租户名称
     */
    private void fillTenantName(InsuranceApplyRecordVo vo) {
        if (vo == null || StringUtils.isBlank(vo.getTenantId())) {
            return;
        }
        SysTenantVo tenant = sysTenantService.queryByTenantId(vo.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getCompanyName());
        }
    }

    /**
     * 批量补充租户名称
     */
    private void fillTenantNames(List<InsuranceApplyRecordVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<String, String> tenantNameMap = new HashMap<>();
        for (InsuranceApplyRecordVo vo : list) {
            if (vo == null || StringUtils.isBlank(vo.getTenantId()) || tenantNameMap.containsKey(vo.getTenantId())) {
                continue;
            }
            SysTenantVo tenant = sysTenantService.queryByTenantId(vo.getTenantId());
            tenantNameMap.put(vo.getTenantId(), tenant != null ? tenant.getCompanyName() : "");
        }
        for (InsuranceApplyRecordVo vo : list) {
            if (vo != null && StringUtils.isNotBlank(vo.getTenantId())) {
                vo.setTenantName(tenantNameMap.get(vo.getTenantId()));
            }
        }
    }

    /**
     * 新增代投保订单查询
     *
     * @param bo 代投保订单查询
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord add = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        // 🌟 强制设为代投保模式
        add.setInsureMode(1);
        validEntityBeforeSave(add);
        return TenantHelper.ignore(() -> {
            boolean flag = baseMapper.insert(add) > 0;
            if (flag) {
                bo.setId(add.getId());
            }
            return flag;
        });
    }

    /**
     * 修改代投保订单查询
     *
     * @param bo 代投保订单查询
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord update = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(update);
        // 🌟 仅允许修改代投保模式的订单
        return TenantHelper.ignore(() -> baseMapper.update(update, new LambdaUpdateWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getId, bo.getId())
            .eq(InsuranceApplyRecord::getInsureMode, 1)) > 0);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceApplyRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除代投保订单查询信息
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
        // 🌟 仅允许删除代投保模式的订单
        return TenantHelper.ignore(() -> baseMapper.delete(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .in(InsuranceApplyRecord::getId, ids)
            .eq(InsuranceApplyRecord::getInsureMode, 1)) > 0);
    }
}
