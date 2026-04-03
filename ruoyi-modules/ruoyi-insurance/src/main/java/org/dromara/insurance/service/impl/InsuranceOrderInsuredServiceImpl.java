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
import org.dromara.insurance.domain.bo.InsuranceOrderInsuredBo;
import org.dromara.insurance.domain.vo.InsuranceOrderInsuredVo;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.dromara.insurance.service.IInsuranceOrderInsuredService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 被保人明细Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceOrderInsuredServiceImpl implements IInsuranceOrderInsuredService {

    private final InsuranceOrderInsuredMapper baseMapper;

    /**
     * 查询被保人明细
     *
     * @param id 主键
     * @return 被保人明细
     */
    @Override
    public InsuranceOrderInsuredVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询被保人明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 被保人明细分页列表
     */
    @Override
    public TableDataInfo<InsuranceOrderInsuredVo> queryPageList(InsuranceOrderInsuredBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceOrderInsured> lqw = buildQueryWrapper(bo);
        Page<InsuranceOrderInsuredVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的被保人明细列表
     *
     * @param bo 查询条件
     * @return 被保人明细列表
     */
    @Override
    public List<InsuranceOrderInsuredVo> queryList(InsuranceOrderInsuredBo bo) {
        LambdaQueryWrapper<InsuranceOrderInsured> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceOrderInsured> buildQueryWrapper(InsuranceOrderInsuredBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceOrderInsured> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceOrderInsured::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceOrderInsured::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getRelation()), InsuranceOrderInsured::getRelation, bo.getRelation());
        lqw.like(StringUtils.isNotBlank(bo.getInsuredName()), InsuranceOrderInsured::getInsuredName, bo.getInsuredName());
        lqw.eq(StringUtils.isNotBlank(bo.getCertStartDate()), InsuranceOrderInsured::getCertStartDate, bo.getCertStartDate());
        lqw.eq(StringUtils.isNotBlank(bo.getCertEndDate()), InsuranceOrderInsured::getCertEndDate, bo.getCertEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getInsuredCertNo()), InsuranceOrderInsured::getInsuredCertNo, bo.getInsuredCertNo());
        lqw.eq(StringUtils.isNotBlank(bo.getInsuredCertType()), InsuranceOrderInsured::getInsuredCertType, bo.getInsuredCertType());
        lqw.eq(StringUtils.isNotBlank(bo.getInsuredPhone()), InsuranceOrderInsured::getInsuredPhone, bo.getInsuredPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getInsuredAddress()), InsuranceOrderInsured::getInsuredAddress, bo.getInsuredAddress());
        return lqw;
    }

    /**
     * 新增被保人明细
     *
     * @param bo 被保人明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceOrderInsuredBo bo) {
        InsuranceOrderInsured add = MapstructUtils.convert(bo, InsuranceOrderInsured.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改被保人明细
     *
     * @param bo 被保人明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceOrderInsuredBo bo) {
        InsuranceOrderInsured update = MapstructUtils.convert(bo, InsuranceOrderInsured.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceOrderInsured entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除被保人明细信息
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
}
