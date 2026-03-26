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
import org.dromara.insurance.domain.bo.InsuranceProductLiabilityBo;
import org.dromara.insurance.domain.vo.InsuranceProductLiabilityVo;
import org.dromara.insurance.domain.InsuranceProductLiability;
import org.dromara.insurance.mapper.InsuranceProductLiabilityMapper;
import org.dromara.insurance.service.IInsuranceProductLiabilityService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 保险产品-保障责任Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProductLiabilityServiceImpl implements IInsuranceProductLiabilityService {

    private final InsuranceProductLiabilityMapper baseMapper;

    /**
     * 查询保险产品-保障责任
     *
     * @param id 主键
     * @return 保险产品-保障责任
     */
    @Override
    public InsuranceProductLiabilityVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询保险产品-保障责任列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 保险产品-保障责任分页列表
     */
    @Override
    public TableDataInfo<InsuranceProductLiabilityVo> queryPageList(InsuranceProductLiabilityBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProductLiability> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductLiabilityVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的保险产品-保障责任列表
     *
     * @param bo 查询条件
     * @return 保险产品-保障责任列表
     */
    @Override
    public List<InsuranceProductLiabilityVo> queryList(InsuranceProductLiabilityBo bo) {
        LambdaQueryWrapper<InsuranceProductLiability> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProductLiability> buildQueryWrapper(InsuranceProductLiabilityBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProductLiability> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProductLiability::getId);
        lqw.eq(bo.getProductId() != null, InsuranceProductLiability::getProductId, bo.getProductId());
        lqw.like(StringUtils.isNotBlank(bo.getLiabilityName()), InsuranceProductLiability::getLiabilityName, bo.getLiabilityName());
        lqw.eq(StringUtils.isNotBlank(bo.getInsuredAmountDesc()), InsuranceProductLiability::getInsuredAmountDesc, bo.getInsuredAmountDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), InsuranceProductLiability::getDescription, bo.getDescription());
        lqw.eq(bo.getSort() != null, InsuranceProductLiability::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增保险产品-保障责任
     *
     * @param bo 保险产品-保障责任
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductLiabilityBo bo) {
        InsuranceProductLiability add = MapstructUtils.convert(bo, InsuranceProductLiability.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改保险产品-保障责任
     *
     * @param bo 保险产品-保障责任
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceProductLiabilityBo bo) {
        InsuranceProductLiability update = MapstructUtils.convert(bo, InsuranceProductLiability.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProductLiability entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除保险产品-保障责任信息
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
