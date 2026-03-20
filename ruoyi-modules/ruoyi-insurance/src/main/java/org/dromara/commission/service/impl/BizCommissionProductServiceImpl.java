package org.dromara.commission.service.impl;

import cn.hutool.core.collection.CollUtil;
import org.dromara.common.core.utils.DateUtils;
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
import org.dromara.commission.domain.bo.BizCommissionProductBo;
import org.dromara.commission.domain.vo.BizCommissionProductVo;
import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.commission.mapper.BizCommissionProductMapper;
import org.dromara.commission.service.IBizCommissionProductService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特殊产品费率配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizCommissionProductServiceImpl implements IBizCommissionProductService {

    private final BizCommissionProductMapper baseMapper;

    /**
     * 查询特殊产品费率配置
     *
     * @param id 主键
     * @return 特殊产品费率配置
     */
    @Override
    public BizCommissionProductVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询特殊产品费率配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 特殊产品费率配置分页列表
     */
    @Override
    public TableDataInfo<BizCommissionProductVo> queryPageList(BizCommissionProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizCommissionProduct> lqw = buildQueryWrapper(bo);
        Page<BizCommissionProductVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的特殊产品费率配置列表
     *
     * @param bo 查询条件
     * @return 特殊产品费率配置列表
     */
    @Override
    public List<BizCommissionProductVo> queryList(BizCommissionProductBo bo) {
        LambdaQueryWrapper<BizCommissionProduct> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizCommissionProduct> buildQueryWrapper(BizCommissionProductBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizCommissionProduct> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BizCommissionProduct::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), BizCommissionProduct::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), BizCommissionProduct::getProductName, bo.getProductName());
        lqw.eq(bo.getEffectiveStart() != null, BizCommissionProduct::getEffectiveStart, bo.getEffectiveStart());
        lqw.eq(bo.getEffectiveEnd() != null, BizCommissionProduct::getEffectiveEnd, bo.getEffectiveEnd());
        lqw.eq(bo.getStatus() != null, BizCommissionProduct::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增特殊产品费率配置
     *
     * @param bo 特殊产品费率配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizCommissionProductBo bo) {
        BizCommissionProduct add = MapstructUtils.convert(bo, BizCommissionProduct.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改特殊产品费率配置
     *
     * @param bo 特殊产品费率配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizCommissionProductBo bo) {
        BizCommissionProduct update = MapstructUtils.convert(bo, BizCommissionProduct.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizCommissionProduct entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除特殊产品费率配置信息
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
    public BizCommissionProduct queryByProductIdAndTenantId(Long productId, String tenantId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<BizCommissionProduct>()
            .eq(BizCommissionProduct::getProductId, productId)
            .eq(BizCommissionProduct::getTenantId, tenantId));
    }

    @Override
    public Map<Long, BizCommissionProductVo> getBatchSpecialConfigs(List<Long> productIds) {
        // 1. 防御性拦截：如果前端传来的产品列表是空的，直接返回空 Map，防止 MyBatis 报错
        if (CollUtil.isEmpty(productIds)) {
            return new HashMap<>();
        }

        // 2. 发送 1 条 SQL 批量查出所有涉及到的特殊产品费率
        Date now = new Date();
        var lqw = Wrappers.<BizCommissionProduct>lambdaQuery()
            // 相当于 SQL: WHERE product_id IN (1, 2, 3...)
            .in(BizCommissionProduct::getProductId, productIds)
            // 🌟 严谨：只查询状态为 0 (启用) 的特殊配置
            .eq(BizCommissionProduct::getStatus, 0)
            .le(BizCommissionProduct::getEffectiveStart, now)
            .ge(BizCommissionProduct::getEffectiveEnd, now);

        var list = baseMapper.selectList(lqw);

        // 3. 将 List 转换为 Map 返回，方便外层进行 O(1) 的极速匹配
        return list.stream().collect(Collectors.toMap(
            // Map 的 Key：产品 ID
            BizCommissionProduct::getProductId,

            // Map 的 Value：将查出来的 Entity 实体当场转换为外层需要的 VO 对象
            entity -> MapstructUtils.convert(entity, BizCommissionProductVo.class),

            // 兜底合并策略：万一数据库有脏数据，同一个产品查出了两条启用的特殊配置，保留第一条，防止抛出 Duplicate Key 异常
            (v1, v2) -> v1
        ));
    }
}
