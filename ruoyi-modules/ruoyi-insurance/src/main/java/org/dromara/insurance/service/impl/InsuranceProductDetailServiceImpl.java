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
import org.dromara.insurance.domain.bo.InsuranceProductDetailBo;
import org.dromara.insurance.domain.vo.InsuranceProductDetailVo;
import org.dromara.insurance.domain.InsuranceProductDetail;
import org.dromara.insurance.mapper.InsuranceProductDetailMapper;
import org.dromara.insurance.service.IInsuranceProductDetailService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 保险产品-图文详情Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProductDetailServiceImpl implements IInsuranceProductDetailService {

    private final InsuranceProductDetailMapper baseMapper;

    /**
     * 查询保险产品-图文详情
     *
     * @param id 主键
     * @return 保险产品-图文详情
     */
    @Override
    public InsuranceProductDetailVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询保险产品-图文详情列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 保险产品-图文详情分页列表
     */
    @Override
    public TableDataInfo<InsuranceProductDetailVo> queryPageList(InsuranceProductDetailBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProductDetail> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductDetailVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的保险产品-图文详情列表
     *
     * @param bo 查询条件
     * @return 保险产品-图文详情列表
     */
    @Override
    public List<InsuranceProductDetailVo> queryList(InsuranceProductDetailBo bo) {
        LambdaQueryWrapper<InsuranceProductDetail> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProductDetail> buildQueryWrapper(InsuranceProductDetailBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProductDetail> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProductDetail::getId);
        lqw.eq(bo.getProductId() != null, InsuranceProductDetail::getProductId, bo.getProductId());
        return lqw;
    }

    /**
     * 新增保险产品-图文详情
     *
     * @param bo 保险产品-图文详情
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductDetailBo bo) {
        InsuranceProductDetail add = MapstructUtils.convert(bo, InsuranceProductDetail.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改保险产品-图文详情
     *
     * @param bo 保险产品-图文详情
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceProductDetailBo bo) {
        InsuranceProductDetail update = MapstructUtils.convert(bo, InsuranceProductDetail.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProductDetail entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除保险产品-图文详情信息
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
