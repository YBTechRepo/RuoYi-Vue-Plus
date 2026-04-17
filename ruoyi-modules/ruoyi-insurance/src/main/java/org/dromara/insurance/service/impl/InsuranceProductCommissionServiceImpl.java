package org.dromara.insurance.service.impl;

import cn.hutool.core.collection.CollUtil;
import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.insurance.domain.bo.ProductCommissionConfig;
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
        if (StringUtils.isNotBlank(entity.getCommissionConfig())) {
            List<ProductCommissionConfig> configs = JsonUtils.parseArray(entity.getCommissionConfig(), ProductCommissionConfig.class);
            if (CollUtil.isNotEmpty(configs)) {
                // 过滤掉所有未设置生效时间的无效数据，并按生效时间升序排序
                List<ProductCommissionConfig> sortedConfigs = configs.stream()
                    .filter(c -> c.getEffectiveTime() != null)
                    .sorted(Comparator.comparing(ProductCommissionConfig::getEffectiveTime))
                    .collect(Collectors.toList());

                // 校验时间段是否有交集
                for (int i = 0; i < sortedConfigs.size() - 1; i++) {
                    ProductCommissionConfig current = sortedConfigs.get(i);
                    ProductCommissionConfig next = sortedConfigs.get(i + 1);

                    // 如果当前配置没有失效时间(表示永久)，而后面还有新的生效配置，说明重叠了
                    if (current.getExpirationTime() == null) {
                        throw new org.dromara.common.core.exception.ServiceException("佣金配置的时间段存在重叠：配置了永久有效后，不能再添加后续时间段的配置");
                    }

                    // 正常的重叠：当前失效时间 > 后一个的生效时间 (允许等于，即 00:00:00 结束，下一个 00:00:00 开始)
                    if (current.getExpirationTime().after(next.getEffectiveTime())) {
                        throw new org.dromara.common.core.exception.ServiceException("佣金配置的时间段存在重叠，请检查配置项！");
                    }
                }
                
                // 校验自身时间逻辑：生效时间不能晚于失效时间
                for (ProductCommissionConfig config : sortedConfigs) {
                    if (config.getExpirationTime() != null && config.getEffectiveTime().after(config.getExpirationTime())) {
                        throw new org.dromara.common.core.exception.ServiceException("佣金配置有误：生效时间不能晚于失效时间！");
                    }
                }
            }
        }
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
        InsuranceProductCommission commission = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceProductCommission>()
            .eq(InsuranceProductCommission::getProductId, productId)
            .eq(InsuranceProductCommission::getTenantId, tenantId));

        if (commission != null && StringUtils.isNotBlank(commission.getCommissionConfig())) {
            List<ProductCommissionConfig> configs = JsonUtils.parseArray(commission.getCommissionConfig(), ProductCommissionConfig.class);
            Date now = new Date();
            ProductCommissionConfig activeConfig = configs.stream()
                .filter(c -> (c.getEffectiveTime() == null || now.after(c.getEffectiveTime()))
                          && (c.getExpirationTime() == null || now.before(c.getExpirationTime())))
                .findFirst()
                .orElse(null);

            if (activeConfig != null) {
                commission.setCommissionRate(activeConfig.getCommissionRate());
                commission.setEffectiveTime(activeConfig.getEffectiveTime());
                commission.setExpirationTime(activeConfig.getExpirationTime());
            } else {
                // 如果没有生效的配置，清空费率，防止误用旧数据
                commission.setCommissionRate(null);
            }
        }
        return commission;
    }

    @Override
    public Map<Long, BigDecimal> getBatchRateMap(List<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return new HashMap<>();
        }

        // 1. 发送 1 条 SQL，查出这批产品对应的佣金配置
        Date now = new Date();
        var lqw = Wrappers.<InsuranceProductCommission>lambdaQuery()
            .in(InsuranceProductCommission::getProductId, productIds)
            .eq(InsuranceProductCommission::getStatus, 0);

        var commissionList = baseMapper.selectList(lqw);
        Map<Long, BigDecimal> rateMap = new HashMap<>();

        for (InsuranceProductCommission commission : commissionList) {
            BigDecimal activeRate = null;
            if (StringUtils.isNotBlank(commission.getCommissionConfig())) {
                List<ProductCommissionConfig> configs = JsonUtils.parseArray(commission.getCommissionConfig(), ProductCommissionConfig.class);
                activeRate = configs.stream()
                    .filter(c -> (c.getEffectiveTime() == null || now.after(c.getEffectiveTime()))
                              && (c.getExpirationTime() == null || now.before(c.getExpirationTime())))
                    .findFirst()
                    .map(ProductCommissionConfig::getCommissionRate)
                    .orElse(null);
            }

            // 如果 JSON 中没有找到生效的，且数据库字段中有值，可以考虑是否作为兜底，但按需求应优先/只看 JSON
            if (activeRate != null) {
                rateMap.put(commission.getProductId(), activeRate);
            } else if (commission.getCommissionRate() != null && isEffective(now, commission.getEffectiveTime(), commission.getExpirationTime())) {
                // 这里保留对旧字段的兼容性（可选）
                rateMap.put(commission.getProductId(), commission.getCommissionRate());
            }
        }

        return rateMap;
    }

    private boolean isEffective(Date now, Date start, Date end) {
        if (now == null) return false;
        boolean afterStart = (start == null) || !now.before(start);
        boolean beforeEnd = (end == null) || !now.after(end);
        return afterStart && beforeEnd;
    }
}
