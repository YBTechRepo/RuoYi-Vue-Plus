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
import org.dromara.insurance.domain.bo.InsuranceProductBo;
import org.dromara.insurance.domain.vo.InsuranceProductVo;
import org.dromara.insurance.domain.InsuranceProduct;
import org.dromara.insurance.mapper.InsuranceProductMapper;
import org.dromara.insurance.service.IInsuranceProductService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 产品配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProductServiceImpl implements IInsuranceProductService {

    private final InsuranceProductMapper baseMapper;

    /**
     * 查询产品配置
     *
     * @param id 主键
     * @return 产品配置
     */
    @Override
    public InsuranceProductVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询产品配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品配置分页列表
     */
    @Override
    public TableDataInfo<InsuranceProductVo> queryPageList(InsuranceProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProduct> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的产品配置列表
     *
     * @param bo 查询条件
     * @return 产品配置列表
     */
    @Override
    public List<InsuranceProductVo> queryList(InsuranceProductBo bo) {
        LambdaQueryWrapper<InsuranceProduct> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProduct> buildQueryWrapper(InsuranceProductBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProduct> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProduct::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceProduct::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceProduct::getProductName, bo.getProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getCompanyCode()), InsuranceProduct::getCompanyCode, bo.getCompanyCode());
        lqw.eq(StringUtils.isNotBlank(bo.getProductType()), InsuranceProduct::getProductType, bo.getProductType());
        lqw.eq(bo.getProductMode() != null, InsuranceProduct::getProductMode, bo.getProductMode());
        lqw.eq(bo.getStatus() != null, InsuranceProduct::getStatus, bo.getStatus());
        lqw.eq(bo.getSort() != null, InsuranceProduct::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增产品配置
     *
     * @param bo 产品配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductBo bo) {
        InsuranceProduct add = MapstructUtils.convert(bo, InsuranceProduct.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改产品配置
     *
     * @param bo 产品配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceProductBo bo) {
        InsuranceProduct update = MapstructUtils.convert(bo, InsuranceProduct.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProduct entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除产品配置信息
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
