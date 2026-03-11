package org.dromara.insurance.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.vo.InsurancePolicyVo;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.mapper.InsurancePolicyMapper;
import org.dromara.insurance.service.IInsurancePolicyService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 承保保单Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsurancePolicyServiceImpl implements IInsurancePolicyService {

    private final InsurancePolicyMapper baseMapper;

    /**
     * 查询承保保单
     *
     * @param id 主键
     * @return 承保保单
     */
    @Override
    public InsurancePolicyVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询承保保单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 承保保单分页列表
     */
    @Override
    public TableDataInfo<InsurancePolicyVo> queryPageList(InsurancePolicyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsurancePolicy> lqw = buildQueryWrapper(bo);
        Page<InsurancePolicyVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的承保保单列表
     *
     * @param bo 查询条件
     * @return 承保保单列表
     */
    @Override
    public List<InsurancePolicyVo> queryList(InsurancePolicyBo bo) {
        LambdaQueryWrapper<InsurancePolicy> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsurancePolicy> buildQueryWrapper(InsurancePolicyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsurancePolicy> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsurancePolicy::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsurancePolicy::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsurancePolicy::getProductName, bo.getProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getPolicyNo()), InsurancePolicy::getPolicyNo, bo.getPolicyNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsurancePolicy::getOrderNo, bo.getOrderNo());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsurancePolicy::getAgentName, bo.getAgentName());
        lqw.eq(bo.getPremium() != null, InsurancePolicy::getPremium, bo.getPremium());
        lqw.eq(bo.getAmt() != null, InsurancePolicy::getAmt, bo.getAmt());
        lqw.eq(bo.getCommissionStatus() != null, InsurancePolicy::getCommissionStatus, bo.getCommissionStatus());
        lqw.eq(bo.getStatus() != null, InsurancePolicy::getStatus, bo.getStatus());
        lqw.eq(bo.getAppntDate() != null, InsurancePolicy::getAppntDate, bo.getAppntDate());
        lqw.like(StringUtils.isNotBlank(bo.getApplicantName()), InsurancePolicy::getApplicantName, bo.getApplicantName());
        return lqw;
    }

    /**
     * 新增承保保单
     *
     * @param bo 承保保单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsurancePolicyBo bo) {
        InsurancePolicy add = MapstructUtils.convert(bo, InsurancePolicy.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改承保保单
     *
     * @param bo 承保保单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsurancePolicyBo bo) {
        InsurancePolicy update = MapstructUtils.convert(bo, InsurancePolicy.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsurancePolicy entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除承保保单信息
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
    public InsurancePolicy queryByPolicyNoAndTenantId(String policyNo, String tenantId) {
        LambdaQueryWrapper<InsurancePolicy> lqw = Wrappers.lambdaQuery();
        lqw.eq(InsurancePolicy::getPolicyNo, policyNo);
        lqw.eq(InsurancePolicy::getTenantId, tenantId);
        return baseMapper.selectOne(lqw);
    }
}
