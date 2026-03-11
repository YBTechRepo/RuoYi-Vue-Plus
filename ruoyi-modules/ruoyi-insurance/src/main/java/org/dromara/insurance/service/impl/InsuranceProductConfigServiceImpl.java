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
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.domain.vo.InsuranceProductConfigVo;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.service.IInsuranceProductConfigService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 产品配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProductConfigServiceImpl implements IInsuranceProductConfigService {

    private final InsuranceProductConfigMapper baseMapper;

    /**
     * 查询产品配置
     *
     * @param id 主键
     * @return 产品配置
     */
    @Override
    public InsuranceProductConfigVo queryById(Long id){
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
    public TableDataInfo<InsuranceProductConfigVo> queryPageList(InsuranceProductConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProductConfig> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的产品配置列表
     *
     * @param bo 查询条件
     * @return 产品配置列表
     */
    @Override
    public List<InsuranceProductConfigVo> queryList(InsuranceProductConfigBo bo) {
        LambdaQueryWrapper<InsuranceProductConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProductConfig> buildQueryWrapper(InsuranceProductConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProductConfig> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProductConfig::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceProductConfig::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceProductConfig::getProductName, bo.getProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getCompanyCode()), InsuranceProductConfig::getCompanyCode, bo.getCompanyCode());
        lqw.eq(StringUtils.isNotBlank(bo.getProductType()), InsuranceProductConfig::getProductType, bo.getProductType());
        lqw.eq(bo.getProductMode() != null, InsuranceProductConfig::getProductMode, bo.getProductMode());
        lqw.eq(bo.getMinPremium() != null, InsuranceProductConfig::getMinPremium, bo.getMinPremium());
        lqw.eq(StringUtils.isNotBlank(bo.getImgUrl()), InsuranceProductConfig::getImgUrl, bo.getImgUrl());
        lqw.eq(bo.getStatus() != null, InsuranceProductConfig::getStatus, bo.getStatus());
        lqw.eq(bo.getSort() != null, InsuranceProductConfig::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增产品配置
     *
     * @param bo 产品配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductConfigBo bo) {
        InsuranceProductConfig add = MapstructUtils.convert(bo, InsuranceProductConfig.class);
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
    public Boolean updateByBo(InsuranceProductConfigBo bo) {
        InsuranceProductConfig update = MapstructUtils.convert(bo, InsuranceProductConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProductConfig entity){
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

    @Override
    public InsuranceProductConfig queryByProductCodeAndTenantId(String productCode, String tenantId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<InsuranceProductConfig>()
                .eq(InsuranceProductConfig::getProductCode, productCode)
                .eq(InsuranceProductConfig::getTenantId, tenantId));
    }

}
