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
import org.dromara.insurance.domain.bo.InsuranceOrderApplicantBo;
import org.dromara.insurance.domain.vo.InsuranceOrderApplicantVo;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.service.IInsuranceOrderApplicantService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 投保人信息Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceOrderApplicantServiceImpl implements IInsuranceOrderApplicantService {

    private final InsuranceOrderApplicantMapper baseMapper;

    /**
     * 查询投保人信息
     *
     * @param id 主键
     * @return 投保人信息
     */
    @Override
    public InsuranceOrderApplicantVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询投保人信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保人信息分页列表
     */
    @Override
    public TableDataInfo<InsuranceOrderApplicantVo> queryPageList(InsuranceOrderApplicantBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceOrderApplicant> lqw = buildQueryWrapper(bo);
        Page<InsuranceOrderApplicantVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的投保人信息列表
     *
     * @param bo 查询条件
     * @return 投保人信息列表
     */
    @Override
    public List<InsuranceOrderApplicantVo> queryList(InsuranceOrderApplicantBo bo) {
        LambdaQueryWrapper<InsuranceOrderApplicant> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceOrderApplicant> buildQueryWrapper(InsuranceOrderApplicantBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceOrderApplicant> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceOrderApplicant::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceOrderApplicant::getOrderNo, bo.getOrderNo());
        lqw.like(StringUtils.isNotBlank(bo.getApplicantName()), InsuranceOrderApplicant::getApplicantName, bo.getApplicantName());
        lqw.eq(StringUtils.isNotBlank(bo.getCertStartDate()), InsuranceOrderApplicant::getCertStartDate, bo.getCertStartDate());
        lqw.eq(StringUtils.isNotBlank(bo.getCertEndDate()), InsuranceOrderApplicant::getCertEndDate, bo.getCertEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getApplicantCertNo()), InsuranceOrderApplicant::getApplicantCertNo, bo.getApplicantCertNo());
        lqw.eq(StringUtils.isNotBlank(bo.getApplicantCertType()), InsuranceOrderApplicant::getApplicantCertType, bo.getApplicantCertType());
        lqw.eq(StringUtils.isNotBlank(bo.getApplicantPhone()), InsuranceOrderApplicant::getApplicantPhone, bo.getApplicantPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getApplicantAddress()), InsuranceOrderApplicant::getApplicantAddress, bo.getApplicantAddress());
        return lqw;
    }

    /**
     * 新增投保人信息
     *
     * @param bo 投保人信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceOrderApplicantBo bo) {
        InsuranceOrderApplicant add = MapstructUtils.convert(bo, InsuranceOrderApplicant.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改投保人信息
     *
     * @param bo 投保人信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceOrderApplicantBo bo) {
        InsuranceOrderApplicant update = MapstructUtils.convert(bo, InsuranceOrderApplicant.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceOrderApplicant entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除投保人信息信息
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
