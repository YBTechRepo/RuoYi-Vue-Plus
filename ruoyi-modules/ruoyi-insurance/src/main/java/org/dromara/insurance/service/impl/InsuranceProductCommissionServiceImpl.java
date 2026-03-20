package org.dromara.insurance.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import org.dromara.insurance.domain.bo.InsuranceProductCommissionBo;
import org.dromara.insurance.domain.vo.InsuranceProductCommissionVo;
import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.insurance.mapper.InsuranceProductCommissionMapper;
import org.dromara.insurance.service.IInsuranceProductCommissionService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 佣金配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProductCommissionServiceImpl implements IInsuranceProductCommissionService {

    private final InsuranceProductCommissionMapper baseMapper;

    /**
     * 查询佣金配置
     *
     * @param id 主键
     * @return 佣金配置
     */
    @Override
    public InsuranceProductCommissionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询佣金配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 佣金配置分页列表
     */
    @Override
    public TableDataInfo<InsuranceProductCommissionVo> queryPageList(InsuranceProductCommissionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProductCommission> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductCommissionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的佣金配置列表
     *
     * @param bo 查询条件
     * @return 佣金配置列表
     */
    @Override
    public List<InsuranceProductCommissionVo> queryList(InsuranceProductCommissionBo bo) {
        LambdaQueryWrapper<InsuranceProductCommission> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProductCommission> buildQueryWrapper(InsuranceProductCommissionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProductCommission> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProductCommission::getId);
        lqw.eq(bo.getProductId() != null, InsuranceProductCommission::getProductId, bo.getProductId());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceProductCommission::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceProductCommission::getProductName, bo.getProductName());
        lqw.eq(bo.getStatus() != null, InsuranceProductCommission::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增佣金配置
     *
     * @param bo 佣金配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductCommissionBo bo) {
        InsuranceProductCommission add = MapstructUtils.convert(bo, InsuranceProductCommission.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改佣金配置
     *
     * @param bo 佣金配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceProductCommissionBo bo) {
        InsuranceProductCommission update = MapstructUtils.convert(bo, InsuranceProductCommission.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProductCommission entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除佣金配置信息
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
    public InsuranceProductCommission queryByProductIdAndTenantId(Long productId, String tenantId) {
        Date now = new Date();
        return baseMapper.selectOne(new LambdaQueryWrapper<InsuranceProductCommission>()
            .eq(InsuranceProductCommission::getProductId, productId)
            .eq(InsuranceProductCommission::getTenantId, tenantId)
            .le(InsuranceProductCommission::getEffectiveTime, now)
            .ge(InsuranceProductCommission::getExpirationTime, now));
    }

    @Override
    public Map<Long, BigDecimal> getBatchRateMap(List<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return new HashMap<>();
        }

        // 1. 发送 1 条 SQL，直接查出这批产品对应的唯一佣金配置
        // 💡 魔法：RuoYi 底层的多租户拦截器会自动帮你拼上 AND tenant_id = '当前租户'
        // 所以业务员绝对不会查到别的机构的费率！
        Date now = new Date();
        var lqw = Wrappers.<InsuranceProductCommission>lambdaQuery()
            .in(InsuranceProductCommission::getProductId, productIds)
            .le(InsuranceProductCommission::getEffectiveTime, now)
            .ge(InsuranceProductCommission::getExpirationTime, now);

        var commissionList = baseMapper.selectList(lqw);

        // 2. 直接转成 Map 返回 (Key: 产品ID, Value: 佣金比例)
        return commissionList.stream().collect(Collectors.toMap(
            InsuranceProductCommission::getProductId,
            InsuranceProductCommission::getCommissionRate,
            (v1, v2) -> v1 // 防御性编程：万一有脏数据导致重复，取第一条
        ));
    }
}
