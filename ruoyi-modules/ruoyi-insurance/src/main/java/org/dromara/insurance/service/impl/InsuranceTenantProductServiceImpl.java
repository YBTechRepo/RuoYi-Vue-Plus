package org.dromara.insurance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.domain.vo.BizCommissionProductVo;
import org.dromara.commission.service.IBizCommissionDeptService;
import org.dromara.commission.service.IBizCommissionProductService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysOssService;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.insurance.domain.bo.ServiceFeeConfig;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceTenantProductBo;
import org.dromara.insurance.domain.vo.InsuranceTenantProductVo;
import org.dromara.insurance.domain.InsuranceTenantProduct;
import org.dromara.insurance.mapper.InsuranceTenantProductMapper;
import org.dromara.insurance.service.IInsuranceTenantProductService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品库Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceTenantProductServiceImpl implements IInsuranceTenantProductService {

    private final InsuranceTenantProductMapper baseMapper;

    private final InsuranceProductConfigMapper insuranceProductConfigMapper;

    private final IInsuranceProductCommissionService insuranceProductCommissionService;

    private final IBizCommissionDeptService bizCommissionDeptService;

    private final IBizCommissionProductService bizCommissionProductService;

    private final ISysDeptService sysDeptService;

    private final ISysOssService sysOssService;

    /**
     * 查询产品库
     *
     * @param id 主键
     * @return 产品库
     */
    @Override
    public InsuranceTenantProductVo queryById(Long id) {
        // 1. 先查出本租户的产品关联记录
        InsuranceTenantProduct tp = baseMapper.selectById(id);
        if (tp == null) {
            return null;
        }

        // 2. 转换为 VO
        InsuranceTenantProductVo vo = MapstructUtils.convert(tp, InsuranceTenantProductVo.class);

        // 3. 跨租户查询平台主库并缝合数据
        String platformTenantId = "000000"; // 替换为真实的平台默认租户ID
        TenantHelper.dynamic(platformTenantId, () -> {
            InsuranceProductConfig baseInfo = insuranceProductConfigMapper.selectById(tp.getProductId());
            if (baseInfo != null) {
                // 缝合基础字段
                vo.setCompanyCode(baseInfo.getCompanyCode());
                vo.setProductCode(baseInfo.getProductCode());
                vo.setProductName(baseInfo.getProductName());
                vo.setProductType(baseInfo.getProductType());
                vo.setProductMode(baseInfo.getProductMode());
                vo.setInsureMode(baseInfo.getInsureMode());
                vo.setPaymentMode(baseInfo.getPaymentMode());
                vo.setMinPremium(baseInfo.getMinPremium());
                vo.setDescription(baseInfo.getDescription());
                vo.setProposalUrl(buildProposalUrl(baseInfo.getProposalUrl()));
                vo.setCategoryId(baseInfo.getCategoryId());
                vo.setCategoryName(baseInfo.getCategoryName());
                vo.setMarketingTags(baseInfo.getMarketingTags());

                // 翻译图片链接 (在平台租户环境下)
                if (StringUtils.isNotBlank(baseInfo.getImgUrl())) {
                    try {
                        Long ossId = Long.valueOf(baseInfo.getImgUrl());
                        SysOssVo oss = sysOssService.getById(ossId);
                        if (oss != null && StringUtils.isNotBlank(oss.getUrl())) {
                            vo.setImgUrl(oss.getUrl());
                        }
                    } catch (NumberFormatException e) {
                        vo.setImgUrl(baseInfo.getImgUrl());
                    }
                }

                // 计算服务费和净费
                if (StringUtils.isNotBlank(baseInfo.getServiceFeeConfig())) {
                    List<ServiceFeeConfig> feeConfigs = JsonUtils.parseArray(baseInfo.getServiceFeeConfig(), ServiceFeeConfig.class);
                    Date now = new Date();
                    ServiceFeeConfig currentConfig = feeConfigs.stream()
                        .filter(c -> (c.getEffectiveStartTime() == null || now.after(c.getEffectiveStartTime()))
                                  && (c.getEffectiveEndTime() == null || now.before(c.getEffectiveEndTime())))
                        .findFirst()
                        .orElse(null);

                    if (currentConfig != null && currentConfig.getFeeRatio() != null) {
                        BigDecimal feeRatio = currentConfig.getFeeRatio();
                        vo.setServiceFee(feeRatio);
                        if (vo.getMinPremium() != null) {
                            if (vo.getProductMode() != null && vo.getProductMode() == 2) {
                                vo.setNetPremium(null);
                            } else {
                                BigDecimal netPremium = vo.getMinPremium().multiply(BigDecimal.ONE.subtract(feeRatio));
                                vo.setNetPremium(netPremium.setScale(2, RoundingMode.HALF_UP));
                            }
                        }
                    }
                }
            }
        });

        vo.setDisplayCommissionRate(getDisplayCommissionRateMap(List.of(tp.getProductId()))
            .getOrDefault(tp.getProductId(), BigDecimal.ZERO));

        return vo;
    }

    /**
     * 分页查询产品库列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品库分页列表
     */
    @Override
    public TableDataInfo<InsuranceTenantProductVo> queryPageList(InsuranceTenantProductBo bo, PageQuery pageQuery) {
        // ================= 1. 组装本租户的查询条件 =================
        LambdaQueryWrapper<InsuranceTenantProduct> lqw = new LambdaQueryWrapper<>();
        // 仅保留状态筛选 (前端筛选: 只看上架的 或 只看下架的)
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), InsuranceTenantProduct::getStatus, bo.getStatus());
        applyProductModeFilter(bo, lqw);

        // 分类树形查询
        lqw.and(bo.getCategoryId() != null && bo.getCategoryId() != 0L,
            w -> w.eq(InsuranceTenantProduct::getCategoryId, bo.getCategoryId())
                  .or()
                  .inSql(InsuranceTenantProduct::getCategoryId,
                         "SELECT category_id FROM biz_insurance_product_category WHERE FIND_IN_SET(" + bo.getCategoryId() + ", ancestors)")
        );

        // 营销标签查询
        Map<String, Object> params = bo.getParams();
        if (params != null && params.get("marketingTag") != null && StringUtils.isNotBlank(params.get("marketingTag").toString())) {
            lqw.apply(org.dromara.common.mybatis.helper.DataBaseHelper.findInSet(params.get("marketingTag").toString(), "marketing_tags"));
        }

        // 默认按排序号升序、创建时间降序
        lqw.orderByAsc(InsuranceTenantProduct::getSort).orderByDesc(InsuranceTenantProduct::getCreateTime);

        // ================= 2. 执行本租户的分页查询 =================
        Page<InsuranceTenantProduct> page = baseMapper.selectPage(pageQuery.build(), lqw);
        if (CollUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(new Page<>());
        }

        // ================= 3. 提取当前页的产品 ID =================
        List<Long> productIds = page.getRecords().stream()
            .map(InsuranceTenantProduct::getProductId)
            .collect(Collectors.toList());

        // ================= 4. 开启霸体：跨租户查主库 & 稳妥翻译图片 =================
        String platformTenantId = "000000"; // 替换为真实的平台默认租户ID

        // 外部缓存 Map，专门用来接住真实图片链接，不污染原有实体
        Map<String, String> realOssUrlCache = new HashMap<>();

        Map<Long, InsuranceProductConfig> baseMap = TenantHelper.dynamic(platformTenantId, () -> {
            // 4.1 查出平台产品实体列表
            List<InsuranceProductConfig> baseProducts = insuranceProductConfigMapper.selectBatchIds(productIds);

            // 4.2 遍历转换成 Map，并在这个“霸体”环境内用 getById 把真实 URL 查出来
            Map<Long, InsuranceProductConfig> map = new HashMap<>();
            for (InsuranceProductConfig config : baseProducts) {
                if (StringUtils.isNotBlank(config.getImgUrl())) {
                    try {
                        // 🌟 核心替换：使用最底层、绝对存在的 getById 方法
                        Long ossId = Long.valueOf(config.getImgUrl());
                        SysOssVo oss = sysOssService.getById(ossId); // 如果你的版本返回的不是 SysOssVo 而是 SysOss，请改一下类型

                        if (oss != null && StringUtils.isNotBlank(oss.getUrl())) {
                            realOssUrlCache.put(config.getImgUrl(), oss.getUrl());
                        }
                    } catch (NumberFormatException e) {
                        // 防御性编程：如果历史脏数据直接存了 "http://..."，而非纯数字 ID，直接兜底原样放入
                        realOssUrlCache.put(config.getImgUrl(), config.getImgUrl());
                    }
                }
                map.put(config.getId(), config);
            }
            return map;
        });

        Map<Long, BigDecimal> displayCommissionRateMap = getDisplayCommissionRateMap(productIds);

        // ================= 5. 数据完美缝合 =================
        List<InsuranceTenantProductVo> voList = page.getRecords().stream().map(tp -> {
            // 先转换本表自带的字段 (id, productId, status, sort 等)
            InsuranceTenantProductVo vo = MapstructUtils.convert(tp, InsuranceTenantProductVo.class);

            // 缝合平台库的字段
            InsuranceProductConfig baseInfo = baseMap.get(tp.getProductId());
            if (baseInfo != null) {
                vo.setProductId(baseInfo.getId());
                vo.setCompanyCode(baseInfo.getCompanyCode());
                vo.setProductCode(baseInfo.getProductCode());
                vo.setProductName(baseInfo.getProductName()); // 缝合名字
                vo.setProductType(baseInfo.getProductType());
                vo.setProductMode(baseInfo.getProductMode());
                vo.setInsureMode(baseInfo.getInsureMode());
                vo.setPaymentMode(baseInfo.getPaymentMode());
                vo.setMinPremium(baseInfo.getMinPremium());
                vo.setDescription(baseInfo.getDescription());
                vo.setProposalUrl(buildProposalUrl(baseInfo.getProposalUrl()));
                vo.setStatus(tp.getStatus());
                vo.setSort(tp.getSort());
                vo.setCategoryId(baseInfo.getCategoryId());
                vo.setCategoryName(baseInfo.getCategoryName());
                vo.setMarketingTags(baseInfo.getMarketingTags());
                vo.setDisplayCommissionRate(displayCommissionRateMap.getOrDefault(baseInfo.getId(), BigDecimal.ZERO));

                // 🌟 核心：从刚才缓存的 Map 中拿出真实链接，手动赋给 VO
                vo.setImgUrl(realOssUrlCache.get(baseInfo.getImgUrl()));

                // 🌟 计算服务费和净费
                if (StringUtils.isNotBlank(baseInfo.getServiceFeeConfig())) {
                    List<ServiceFeeConfig> feeConfigs = JsonUtils.parseArray(baseInfo.getServiceFeeConfig(), ServiceFeeConfig.class);
                    Date now = new Date();
                    ServiceFeeConfig currentConfig = feeConfigs.stream()
                        .filter(c -> (c.getEffectiveStartTime() == null || now.after(c.getEffectiveStartTime()))
                                  && (c.getEffectiveEndTime() == null || now.before(c.getEffectiveEndTime())))
                        .findFirst()
                        .orElse(null);

                    if (currentConfig != null && currentConfig.getFeeRatio() != null) {
                        BigDecimal feeRatio = currentConfig.getFeeRatio();
                        vo.setServiceFee(feeRatio);
                        if (vo.getMinPremium() != null) {
                            if (vo.getProductMode() != null && vo.getProductMode() == 2) {
                                vo.setNetPremium(null);
                            } else {
                                BigDecimal netPremium = vo.getMinPremium().multiply(BigDecimal.ONE.subtract(feeRatio));
                                vo.setNetPremium(netPremium.setScale(2, RoundingMode.HALF_UP));
                            }
                        }
                    }
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // ================= 6. 组装分页结果并返回 =================
        Page<InsuranceTenantProductVo> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(voList);

        return TableDataInfo.build(resultPage);
    }

    /**
     * 按当前登录用户角色计算产品展示佣金，规则与移动端销售产品列表保持一致。
     */
    private Map<Long, BigDecimal> getDisplayCommissionRateMap(List<Long> productIds) {
        Map<Long, BigDecimal> baseRateMap = insuranceProductCommissionService.getBatchRateMap(productIds);

        Long deptId = LoginHelper.getDeptId();
        Long topLevelDeptId = deptId;
        SysDeptVo currentDept = sysDeptService.selectDeptById(deptId);
        if (currentDept != null && StringUtils.isNotBlank(currentDept.getAncestors())) {
            String[] ids = currentDept.getAncestors().split(",");
            if (ids.length > 1) {
                topLevelDeptId = Long.valueOf(ids[1]);
            }
        }

        BizCommissionDept commissionDept = bizCommissionDeptService.queryByDeptId(topLevelDeptId);
        BigDecimal normalBizRatio = commissionDept != null && commissionDept.getSalesRatio() != null
            ? commissionDept.getSalesRatio() : BigDecimal.ZERO;
        BigDecimal normalTeamRatio = commissionDept != null && commissionDept.getTeamRatio() != null
            ? commissionDept.getTeamRatio() : BigDecimal.ZERO;
        BigDecimal normalLeaderRatio = commissionDept != null && commissionDept.getProjectRatio() != null
            ? commissionDept.getProjectRatio() : BigDecimal.ZERO;

        Map<Long, BizCommissionProductVo> specialConfigMap = bizCommissionProductService.getBatchSpecialConfigs(productIds);
        boolean isLeader = StpUtil.hasRole("leader");
        boolean isTeamLeader = StpUtil.hasRole("teamleader");
        boolean isBizMan = StpUtil.hasRole("bizman");

        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long productId : productIds) {
            BigDecimal baseRate = baseRateMap.getOrDefault(productId, BigDecimal.ZERO);
            BizCommissionProductVo specialConfig = specialConfigMap.get(productId);
            BigDecimal bizRatio;
            BigDecimal teamRatio;
            BigDecimal leaderRatio;
            if (specialConfig != null) {
                bizRatio = specialConfig.getSalesRatio() != null ? specialConfig.getSalesRatio() : BigDecimal.ZERO;
                teamRatio = specialConfig.getTeamRatio() != null ? specialConfig.getTeamRatio() : BigDecimal.ZERO;
                leaderRatio = specialConfig.getProjectRatio() != null ? specialConfig.getProjectRatio() : BigDecimal.ZERO;
            } else {
                bizRatio = normalBizRatio;
                teamRatio = normalTeamRatio;
                leaderRatio = normalLeaderRatio;
            }

            BigDecimal displayRate = BigDecimal.ZERO;
            if (isLeader) {
                displayRate = baseRate.multiply(bizRatio)
                    .add(baseRate.multiply(teamRatio))
                    .add(baseRate.multiply(leaderRatio));
            } else if (isTeamLeader) {
                displayRate = baseRate.multiply(bizRatio)
                    .add(baseRate.multiply(teamRatio));
            } else if (isBizMan) {
                displayRate = baseRate.multiply(bizRatio);
            }
            result.put(productId, displayRate);
        }
        return result;
    }

    /**
     * 投保链接占位符替换：后台租户产品列表与销售端保持一致，agentCode 使用 KD + 当前用户ID。
     */
    private String buildProposalUrl(String proposalUrl) {
        if (StringUtils.isBlank(proposalUrl)) {
            return proposalUrl;
        }
        Long currentUserId = LoginHelper.getUserId();
        String agentUserId = currentUserId == null ? "" : String.valueOf(currentUserId);
        String userName = LoginHelper.getUsername();
        String agentCode = StringUtils.isBlank(agentUserId) ? (userName == null ? "" : userName) : "KD" + agentUserId;
        return proposalUrl
            .replace("$agentCode$", agentCode)
            .replace("${agentCode}", agentCode)
            .replace("{agentCode}", agentCode)
            .replace("$agentUserId$", agentUserId)
            .replace("${agentUserId}", agentUserId)
            .replace("{agentUserId}", agentUserId);
    }

    /**
     * 产品模式来自平台产品表，先跨租户查询符合模式的产品ID，再约束本租户货架数据。
     */
    private void applyProductModeFilter(InsuranceTenantProductBo bo, LambdaQueryWrapper<InsuranceTenantProduct> lqw) {
        if (bo.getProductMode() == null) {
            return;
        }
        List<Long> productIds = TenantHelper.dynamic("000000", () -> insuranceProductConfigMapper.selectList(
                new LambdaQueryWrapper<InsuranceProductConfig>()
                    .select(InsuranceProductConfig::getId)
                    .eq(InsuranceProductConfig::getProductMode, bo.getProductMode())
            ).stream()
            .map(InsuranceProductConfig::getId)
            .collect(Collectors.toList())
        );
        if (CollUtil.isEmpty(productIds)) {
            lqw.eq(InsuranceTenantProduct::getProductId, -1L);
            return;
        }
        lqw.in(InsuranceTenantProduct::getProductId, productIds);
    }
    /**
     * 查询符合条件的产品库列表
     *
     * @param bo 查询条件
     * @return 产品库列表
     */
    @Override
    public List<InsuranceTenantProductVo> queryList(InsuranceTenantProductBo bo) {
        LambdaQueryWrapper<InsuranceTenantProduct> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceTenantProduct> buildQueryWrapper(InsuranceTenantProductBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceTenantProduct> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceTenantProduct::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), InsuranceTenantProduct::getStatus, bo.getStatus());
        applyProductModeFilter(bo, lqw);

        // 分类树形查询
        lqw.and(bo.getCategoryId() != null && bo.getCategoryId() != 0L,
            w -> w.eq(InsuranceTenantProduct::getCategoryId, bo.getCategoryId())
                  .or()
                  .inSql(InsuranceTenantProduct::getCategoryId,
                         "SELECT category_id FROM biz_insurance_product_category WHERE FIND_IN_SET(" + bo.getCategoryId() + ", ancestors)")
        );

        // 营销标签查询
        if (params != null && params.get("marketingTag") != null && StringUtils.isNotBlank(params.get("marketingTag").toString())) {
            lqw.apply(org.dromara.common.mybatis.helper.DataBaseHelper.findInSet(params.get("marketingTag").toString(), "marketing_tags"));
        }

        return lqw;
    }

    /**
     * 新增产品库
     *
     * @param bo 产品库
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceTenantProductBo bo) {
        InsuranceTenantProduct add = MapstructUtils.convert(bo, InsuranceTenantProduct.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改产品库
     *
     * @param bo 产品库
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceTenantProductBo bo) {
        InsuranceTenantProduct update = MapstructUtils.convert(bo, InsuranceTenantProduct.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceTenantProduct entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除产品库信息
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddProducts(List<Long> productIds) {
        // ================= 1. 防呆拦截：过滤掉已经存在的产品 =================
        List<Long> existProductIds = baseMapper.selectObjs(
            new LambdaQueryWrapper<InsuranceTenantProduct>()
                .select(InsuranceTenantProduct::getProductId)
                .in(InsuranceTenantProduct::getProductId, productIds)
        );

        // 剔除掉已经存在的，只保留真正需要新增的 ID
        List<Long> validIds = productIds.stream()
            .filter(id -> !existProductIds.contains(id))
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(validIds)) {
            throw new ServiceException("所选产品均已在您的货架中，请勿重复添加！");
        }

        // ================= 2. 构建实体列表 =================
        // =============== 1.5 赨租户淥主帓获取囮贏和营鐀标嬾信曯 ================
        Map<Long, InsuranceProductConfig> baseProductMap = TenantHelper.dynamic("000000", () -> {
             return insuranceProductConfigMapper.selectBatchIds(validIds).stream()
                 .collect(Collectors.toMap(InsuranceProductConfig::getId, p -> p));
        });

        List<InsuranceTenantProduct> insertList = new ArrayList<>();
        for (Long productId : validIds) {
            InsuranceTenantProduct tp = new InsuranceTenantProduct();
            tp.setProductId(productId);
            tp.setStatus("0"); // Ĭ��״̬Ϊ����

            InsuranceProductConfig baseInfo = baseProductMap.get(productId);
            if (baseInfo != null) {
                tp.setCategoryId(baseInfo.getCategoryId());
                tp.setCategoryName(baseInfo.getCategoryName());
                tp.setMarketingTags(baseInfo.getMarketingTags());
            }

            insertList.add(tp);
        }

        // ================= 3. 执行批量插入 (RuoYi-Vue-Plus 核心魔法) =================
        // 使用 BaseMapperPlus 提供的 insertBatch 方法，底层会打包成一条批量 SQL
        return baseMapper.insertBatch(insertList);
    }
}
