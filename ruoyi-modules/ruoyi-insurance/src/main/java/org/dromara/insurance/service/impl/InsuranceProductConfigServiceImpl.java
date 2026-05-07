package org.dromara.insurance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
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
import org.dromara.insurance.domain.InsuranceProductDetail;
import org.dromara.insurance.domain.InsuranceProductLiability;
import org.dromara.insurance.domain.InsuranceTenantProduct;
import org.dromara.insurance.domain.bo.InsuranceProductLiabilityBo;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.domain.bo.ServiceFeeConfig;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.domain.vo.MarketProductVo;
import org.dromara.insurance.mapper.InsuranceProductDetailMapper;
import org.dromara.insurance.mapper.InsuranceProductLiabilityMapper;
import org.dromara.insurance.mapper.InsuranceTenantProductMapper;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.insurance.service.IInsuranceProductLiabilityService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.domain.vo.InsuranceProductConfigVo;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.service.IInsuranceProductConfigService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    private final InsuranceTenantProductMapper insuranceTenantProductMapper;

    private final InsuranceProductDetailMapper detailMapper;

    private final InsuranceProductLiabilityMapper liabilityMapper;

    private final IInsuranceProductCommissionService insuranceProductCommissionService;

    private final IBizCommissionDeptService bizCommissionDeptService;

    private final IBizCommissionProductService bizCommissionProductService;

    private final ISysDeptService sysDeptService;

    private final ISysOssService sysOssService;

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
        
        // category_id tree traversal query
        lqw.and(bo.getCategoryId() != null && bo.getCategoryId() != 0L, 
            w -> w.eq(InsuranceProductConfig::getCategoryId, bo.getCategoryId())
                  .or()
                  .inSql(InsuranceProductConfig::getCategoryId, 
                         "SELECT category_id FROM biz_insurance_product_category WHERE FIND_IN_SET(" + bo.getCategoryId() + ", ancestors)")
        );
        
        // marketing tags query
        if (params != null && params.get("marketingTag") != null && StringUtils.isNotBlank(params.get("marketingTag").toString())) {
            lqw.apply(org.dromara.common.mybatis.helper.DataBaseHelper.findInSet(params.get("marketingTag").toString(), "marketing_tags"));
        }

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

    @Override
    public TableDataInfo<InsuranceSalesProductVo> querySalesPageList(InsuranceProductConfigBo bo, PageQuery pageQuery) {
        //log.error("【DEBUG】querySalesPageList params: bo={}, pageQuery={}", org.dromara.common.json.utils.JsonUtils.toJsonString(bo), org.dromara.common.json.utils.JsonUtils.toJsonString(pageQuery));
        // ================= 0. 如果 bo 带有基础表的查询条件，先去主库过滤出 productId =================
        boolean hasFilter = StringUtils.isNotBlank(bo.getProductName()) ||
                            StringUtils.isNotBlank(bo.getCompanyCode()) ||
                            StringUtils.isNotBlank(bo.getProductType()) ||
                            bo.getProductMode() != null ||
                            (bo.getCategoryId() != null && bo.getCategoryId() != 0L) ||
                            (bo.getParams() != null && bo.getParams().get("marketingTag") != null && StringUtils.isNotBlank(bo.getParams().get("marketingTag").toString()));

        List<Long> filterProductIds = null;
        if (hasFilter) {
            filterProductIds = TenantHelper.dynamic("000000", () -> {
                LambdaQueryWrapper<InsuranceProductConfig> filterLqw = Wrappers.lambdaQuery();
                filterLqw.eq(bo.getProductMode() != null, InsuranceProductConfig::getProductMode, bo.getProductMode());
                filterLqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceProductConfig::getProductName, bo.getProductName());
                filterLqw.eq(StringUtils.isNotBlank(bo.getCompanyCode()), InsuranceProductConfig::getCompanyCode, bo.getCompanyCode());
                filterLqw.eq(StringUtils.isNotBlank(bo.getProductType()), InsuranceProductConfig::getProductType, bo.getProductType());
                
                // category_id tree traversal query
                filterLqw.and(bo.getCategoryId() != null && bo.getCategoryId() != 0L, 
                    w -> w.eq(InsuranceProductConfig::getCategoryId, bo.getCategoryId())
                          .or()
                          .inSql(InsuranceProductConfig::getCategoryId, 
                                 "SELECT category_id FROM biz_insurance_product_category WHERE FIND_IN_SET(" + bo.getCategoryId() + ", ancestors)")
                );
                
                // marketing tags query
                Map<String, Object> params = bo.getParams();
                if (params != null && params.get("marketingTag") != null && StringUtils.isNotBlank(params.get("marketingTag").toString())) {
                    filterLqw.apply(org.dromara.common.mybatis.helper.DataBaseHelper.findInSet(params.get("marketingTag").toString(), "marketing_tags"));
                }

                filterLqw.select(InsuranceProductConfig::getId);

                List<Object> objs = baseMapper.selectObjs(filterLqw);
                return objs.stream().map(obj -> (Long) obj).collect(Collectors.toList());
            });

            //log.error("【DEBUG】hasFilter is true, filterProductIds: {}", filterProductIds);
            // 如果带了条件，但是在平台库中没找到匹配的，直接返回空
            if (CollUtil.isEmpty(filterProductIds)) {
                return TableDataInfo.build(new Page<>());
            }
        }

        // ================= 1. 先分页查询【租户自己的货架】 =================
        // 底层拦截器会自动加上 tenant_id = 当前登录人租户ID
        LambdaQueryWrapper<InsuranceTenantProduct> tenantLqw = new LambdaQueryWrapper<>();
        // 🌟 核心过滤：只查该租户自己设置为“上架(0)”的产品
        tenantLqw.eq(InsuranceTenantProduct::getStatus, "0");

        // 🌟 拼装前置的基础表过滤条件
        if (CollUtil.isNotEmpty(filterProductIds)) {
            tenantLqw.in(InsuranceTenantProduct::getProductId, filterProductIds);
        }

        // 按照租户自定义的排序号和添加时间排序
        tenantLqw.orderByAsc(InsuranceTenantProduct::getSort).orderByDesc(InsuranceTenantProduct::getCreateTime);

        // 发起分页查询
        Page<InsuranceTenantProduct> tenantPage = insuranceTenantProductMapper.selectPage(pageQuery.build(), tenantLqw);
        List<InsuranceTenantProduct> tenantProducts = tenantPage.getRecords();

        //log.error("【DEBUG】tenantProducts size: {}", tenantProducts.size());

        // 如果货架是空的，直接返回空列表
        if (CollUtil.isEmpty(tenantProducts)) {
            return TableDataInfo.build(new Page<>());
        }

        // ================= 2. 提取 ID 跨租户查【平台主库】获取基础信息 =================
        List<Long> productIds = tenantProducts.stream()
            .map(InsuranceTenantProduct::getProductId)
            .toList();
        //log.error("【DEBUG】productIds extracted from tenantProducts: {}", productIds);

        // 开启霸体！去平台租户(000000)获取这些产品的名字、图片等真实信息
        String platformTenantId = "000000";
        List<InsuranceProductConfig> baseProducts = TenantHelper.dynamic(platformTenantId, () -> {
            return baseMapper.selectBatchIds(productIds);
        });
        //log.error("【DEBUG】baseProducts fetched: {}", baseProducts.size());

        // ================= 🌟 新增：独立、统一的 OSS 图片处理逻辑 =================

        // 第一步：提取所有有效的 OSS ID（兼容单图和多图逗号拼接的情况）
        List<Long> ossIdList = baseProducts.stream()
            .map(InsuranceProductConfig::getImgUrl)
            .filter(StringUtils::isNotBlank)
            .flatMap(urls -> Arrays.stream(urls.split(","))) // 拆分可能的逗号
            .filter(StringUtils::isNumeric) // 确保是纯数字ID，过滤掉老脏数据
            .map(Long::valueOf)
            .distinct() // 去重，提升查询效率
            .toList();

        // 第二步：开启霸体，单独批查一次 OSS 表
        Map<String, String> ossUrlMap = new HashMap<>();
        if (CollUtil.isNotEmpty(ossIdList)) {
            // 跨到平台租户查询所有涉及到的图片
            List<SysOssVo> ossList = TenantHelper.dynamic("000000", () -> {
                // 使用 MyBatis-Plus 自带的批量查询方法，只发 1 条 SQL
                return sysOssService.listByIds(ossIdList);
            });

            // 转成 Map (Key: ossId字符串, Value: 真实链接)
            if (CollUtil.isNotEmpty(ossList)) {
                ossList.forEach(oss -> ossUrlMap.put(String.valueOf(oss.getOssId()), oss.getUrl()));
            }
        }

        // 转成 Map 方便精准缝合
        Map<Long, InsuranceProductConfig> baseProductMap = baseProducts.stream()
            .collect(Collectors.toMap(InsuranceProductConfig::getId, p -> p));

        // ================= 3. 数据缝合，转换为销售端 VO =================
        // 第三步：在转换 VO 时，从上面的 Map 里精准取值进行缝合

        // 当前用户id
        String rawLoginId = StpUtil.getLoginIdAsString();
        String idStr = rawLoginId.contains(":") ? rawLoginId.substring(rawLoginId.lastIndexOf(":") + 1) : rawLoginId;
        Long currentUserId = Long.valueOf(idStr);

        List<InsuranceSalesProductVo> salesList = new ArrayList<>();
        for (InsuranceTenantProduct tp : tenantProducts) {
            InsuranceProductConfig baseProduct = baseProductMap.get(tp.getProductId());
            if (baseProduct != null) {
                //替换链接参数
                if(baseProduct.getProductMode() != null && baseProduct.getProductMode() == 2) {
                    String newUrl = baseProduct.getProposalUrl() != null ? baseProduct.getProposalUrl().replace("$agentCode$", "KD" + currentUserId) : null;
                    baseProduct.setProposalUrl(newUrl);
                }

                // 1. 拷贝基础属性
                InsuranceSalesProductVo vo = BeanUtil.copyProperties(baseProduct, InsuranceSalesProductVo.class);
                vo.setId(baseProduct.getId());
                //vo.setTenantProductId(tp.getId());

                // 2. 🌟 手动缝合真实图片链接
                if (StringUtils.isNotBlank(vo.getImgUrl())) {
                    // 如果是多图逗号拼接，这里可以写逻辑拆分后再拼接 URL
                    // 这里以单图为例，直接从 Map 中获取
                    String realUrl = ossUrlMap.get(vo.getImgUrl());
                    if (StringUtils.isNotBlank(realUrl)) {
                        vo.setImgUrlUrl(realUrl);
                    } else {
                        // 兜底：如果没查到，把原值塞进去（防止原值本来就是 http 链接）
                        vo.setImgUrlUrl(vo.getImgUrl());
                    }
                }
                salesList.add(vo);
            } else {
                //log.error("【DEBUG】baseProduct is null for productId: {}", tp.getProductId());
            }
        }

        //log.error("【DEBUG】salesList size after mapping: {}", salesList.size());
        // ================= 4. 执行佣金计算逻辑 (你的原生优秀逻辑) =================
        // 4.1 获取这批产品的【基础佣金费率】字典
        var rateMap = insuranceProductCommissionService.getBatchRateMap(productIds);

        // 4.2 准备两套“抽成比例套餐”
        Long deptId = null;
        if (StpUtil.isLogin()){
            deptId = LoginHelper.getDeptId();
        } else {
            return TableDataInfo.build(new Page<>()); // 未登录防重
        }

        Long topLevelDeptId = deptId;
        SysDeptVo currentDept = sysDeptService.selectDeptById(deptId);
        if (currentDept != null && StringUtils.isNotBlank(currentDept.getAncestors())) {
            String[] ids = currentDept.getAncestors().split(",");
            if (ids.length > 1) {
                topLevelDeptId = Long.valueOf(ids[1]);
            }
        }

        BizCommissionDept bizCommissionDept = bizCommissionDeptService.queryByDeptId(topLevelDeptId);

        // 🛡️ 架构师防御性补丁：防空指针！(万一这个机构没配置佣金比例，给个默认值 0)
        BigDecimal normalBizRatio = bizCommissionDept != null && bizCommissionDept.getSalesRatio() != null ? bizCommissionDept.getSalesRatio() : BigDecimal.ZERO;
        BigDecimal normalTeamRatio = bizCommissionDept != null && bizCommissionDept.getTeamRatio() != null ? bizCommissionDept.getTeamRatio() : BigDecimal.ZERO;
        BigDecimal normalLeaderRatio = bizCommissionDept != null && bizCommissionDept.getProjectRatio() != null ? bizCommissionDept.getProjectRatio() : BigDecimal.ZERO;

        Map<Long, BizCommissionProductVo> specialConfigMap = bizCommissionProductService.getBatchSpecialConfigs(productIds);
        boolean isLeader = StpUtil.hasRole("leader");
        boolean isTeamLeader = StpUtil.hasRole("teamleader");
        boolean isBizMan = StpUtil.hasRole("bizman");

        // 4.3 内存动态拼装与计算
        for (var vo : salesList) {
            var baseRate = rateMap.getOrDefault(vo.getId(), BigDecimal.ZERO);
            var specialConfig = specialConfigMap.get(vo.getId());

            BigDecimal currentBizRatio;
            BigDecimal currentTeamRatio;
            BigDecimal currentLeaderRatio;

            if (specialConfig != null) {
                currentBizRatio = specialConfig.getSalesRatio() != null ? specialConfig.getSalesRatio() : BigDecimal.ZERO;
                currentTeamRatio = specialConfig.getTeamRatio() != null ? specialConfig.getTeamRatio() : BigDecimal.ZERO;
                currentLeaderRatio = specialConfig.getProjectRatio() != null ? specialConfig.getProjectRatio() : BigDecimal.ZERO;
            } else {
                currentBizRatio = normalBizRatio;
                currentTeamRatio = normalTeamRatio;
                currentLeaderRatio = normalLeaderRatio;
            }

            var finalDisplayRate = BigDecimal.ZERO;

            // 核心计算：级差累加公式
            if (isLeader) {
                finalDisplayRate = baseRate.multiply(currentBizRatio)
                    .add(baseRate.multiply(currentTeamRatio))
                    .add(baseRate.multiply(currentLeaderRatio));
            } else if (isTeamLeader) {
                finalDisplayRate = baseRate.multiply(currentBizRatio)
                    .add(baseRate.multiply(currentTeamRatio));
            } else if (isBizMan) {
                finalDisplayRate = baseRate.multiply(currentBizRatio);
            }

            vo.setDisplayCommissionRate(finalDisplayRate);
        }

        // ================= 5. 完美组装返回结果 =================
        // ⚠️ 注意：总页数和总条数必须用 tenantPage 的，这样分页数据才是准确的
        Page<InsuranceSalesProductVo> resultPage = new Page<>(tenantPage.getCurrent(), tenantPage.getSize(), tenantPage.getTotal());
        resultPage.setRecords(salesList);

        return TableDataInfo.build(resultPage);
    }


    @Override
    public InsuranceSalesProductVo querySalesProductById(Long productId) {
        // 1. 先查询【租户自己的货架】确认产品是否上架
        InsuranceTenantProduct tp = insuranceTenantProductMapper.selectOne(new LambdaQueryWrapper<InsuranceTenantProduct>()
            .eq(InsuranceTenantProduct::getProductId, productId)
            .eq(InsuranceTenantProduct::getStatus, "0"));

        if (tp == null) {
            return null;
        }

        // 2. 跨租户查【平台主库】获取基础信息
        String platformTenantId = "000000";
        InsuranceProductConfig baseProduct = TenantHelper.dynamic(platformTenantId, () -> baseMapper.selectById(productId));
        if (baseProduct == null) {
            return null;
        }

        // 3. 数据缝合与图片处理
        InsuranceSalesProductVo vo = BeanUtil.copyProperties(baseProduct, InsuranceSalesProductVo.class);
        vo.setId(baseProduct.getId());

        if (StringUtils.isNotBlank(vo.getImgUrl())) {
            // 支持单图/多图处理逻辑
            String ossIdStr = vo.getImgUrl().split(",")[0];
            if (StringUtils.isNumeric(ossIdStr)) {
                SysOssVo oss = TenantHelper.dynamic(platformTenantId, () -> sysOssService.getById(Long.valueOf(ossIdStr)));
                if (oss != null) {
                    vo.setImgUrlUrl(oss.getUrl());
                } else {
                    vo.setImgUrlUrl(vo.getImgUrl());
                }
            }
        }

        // 4. 执行佣金计算逻辑 (保持与 querySalesPageList 一致)
        List<Long> productIds = List.of(productId);
        var rateMap = insuranceProductCommissionService.getBatchRateMap(productIds);

        Long deptId = LoginHelper.getDeptId();
        Long topLevelDeptId = deptId;
        SysDeptVo currentDept = sysDeptService.selectDeptById(deptId);
        if (currentDept != null && StringUtils.isNotBlank(currentDept.getAncestors())) {
            String[] ids = currentDept.getAncestors().split(",");
            if (ids.length > 1) {
                topLevelDeptId = Long.valueOf(ids[1]);
            }
        }

        BizCommissionDept bizCommissionDept = bizCommissionDeptService.queryByDeptId(topLevelDeptId);
        BigDecimal normalBizRatio = bizCommissionDept != null && bizCommissionDept.getSalesRatio() != null ? bizCommissionDept.getSalesRatio() : BigDecimal.ZERO;
        BigDecimal normalTeamRatio = bizCommissionDept != null && bizCommissionDept.getTeamRatio() != null ? bizCommissionDept.getTeamRatio() : BigDecimal.ZERO;
        BigDecimal normalLeaderRatio = bizCommissionDept != null && bizCommissionDept.getProjectRatio() != null ? bizCommissionDept.getProjectRatio() : BigDecimal.ZERO;

        Map<Long, BizCommissionProductVo> specialConfigMap = bizCommissionProductService.getBatchSpecialConfigs(productIds);
        boolean isLeader = StpUtil.hasRole("leader");
        boolean isTeamLeader = StpUtil.hasRole("teamleader");
        boolean isBizMan = StpUtil.hasRole("bizman");

        var baseRate = rateMap.getOrDefault(productId, BigDecimal.ZERO);
        var specialConfig = specialConfigMap.get(productId);

        BigDecimal currentBizRatio;
        BigDecimal currentTeamRatio;
        BigDecimal currentLeaderRatio;

        if (specialConfig != null) {
            currentBizRatio = specialConfig.getSalesRatio() != null ? specialConfig.getSalesRatio() : BigDecimal.ZERO;
            currentTeamRatio = specialConfig.getTeamRatio() != null ? specialConfig.getTeamRatio() : BigDecimal.ZERO;
            currentLeaderRatio = specialConfig.getProjectRatio() != null ? specialConfig.getProjectRatio() : BigDecimal.ZERO;
        } else {
            currentBizRatio = normalBizRatio;
            currentTeamRatio = normalTeamRatio;
            currentLeaderRatio = normalLeaderRatio;
        }

        var finalDisplayRate = BigDecimal.ZERO;
        if (isLeader) {
            finalDisplayRate = baseRate.multiply(currentBizRatio)
                .add(baseRate.multiply(currentTeamRatio))
                .add(baseRate.multiply(currentLeaderRatio));
        } else if (isTeamLeader) {
            finalDisplayRate = baseRate.multiply(currentBizRatio)
                .add(baseRate.multiply(currentTeamRatio));
        } else if (isBizMan) {
            finalDisplayRate = baseRate.multiply(currentBizRatio);
        }

        vo.setDisplayCommissionRate(finalDisplayRate);
        return vo;
    }

    @Override
    public TableDataInfo<MarketProductVo> queryMarketPageList(InsuranceProductConfigBo bo, PageQuery pageQuery) {
        // ================= 1. 查出当前租户已经“进货”的产品 ID =================
        List<Long> addedProductIds = insuranceTenantProductMapper.selectObjs(
            new LambdaQueryWrapper<InsuranceTenantProduct>()
                .select(InsuranceTenantProduct::getProductId)
        );

        // ================= 2. 开启霸体，跨租户查平台总库 =================
        bo.setStatus(0); // 0 代表上架
        String platformTenantId = "000000"; // 替换为真实的平台默认租户ID

        Page<InsuranceProductConfigVo> platformPage = TenantHelper.dynamic(platformTenantId, () -> {
            LambdaQueryWrapper<InsuranceProductConfig> lqw = buildQueryWrapper(bo);
            // 执行底层的分页查询
            Page<InsuranceProductConfigVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);

            // 🌟 核心修复：在霸体状态下，挨个把 OSS ID 手动翻译成真实的 URL
            for (InsuranceProductConfigVo vo : page.getRecords()) {
                if (StringUtils.isNotBlank(vo.getImgUrl())) {
                    try {
                        Long ossId = Long.valueOf(vo.getImgUrl());
                        SysOssVo oss = sysOssService.getById(ossId);
                        if (oss != null && StringUtils.isNotBlank(oss.getUrl())) {
                            vo.setImgUrlUrl(oss.getUrl()); // 手动把真实链接塞进去
                        }
                    } catch (NumberFormatException e) {
                        // 兼容旧的脏数据（本身存的就是http链接）
                        vo.setImgUrlUrl(vo.getImgUrl());
                    }
                }
            }
            return page;
        });

        // ================= 3. 数据缝合 =================
        List<MarketProductVo> marketList = platformPage.getRecords().stream().map(vo -> {
            MarketProductVo marketVo = BeanUtil.copyProperties(vo, MarketProductVo.class);

            // 状态缝合
            boolean isAdded = addedProductIds.contains(vo.getId());
            marketVo.setHasAdded(isAdded);

            return marketVo;
        }).collect(Collectors.toList());

        // ================= 4. 组装并返回分页结果 =================
        Page<MarketProductVo> resultPage = new Page<>(platformPage.getCurrent(), platformPage.getSize(), platformPage.getTotal());
        resultPage.setRecords(marketList);

        return TableDataInfo.build(resultPage);
    }

    @Override
    public void saveFullProduct(InsuranceProductSaveBo formBo) {
        // 🌟 变动点 1：从嵌套的 product 对象中获取主表数据
        InsuranceProductConfigBo productBo = formBo.getProduct();
        if (productBo == null) {
            throw new ServiceException("产品基础信息不能为空");
        }

        // ================= 🌟 新增：服务费配置时间重叠校验 =================
        if (StringUtils.isNotBlank(productBo.getServiceFeeConfig())) {
            List<org.dromara.insurance.domain.bo.ServiceFeeConfig> configs = org.dromara.common.json.utils.JsonUtils.parseArray(productBo.getServiceFeeConfig(), org.dromara.insurance.domain.bo.ServiceFeeConfig.class);
            if (CollUtil.isNotEmpty(configs)) {
                // 过滤掉未设置生效时间的无效数据，并按生效时间升序排序
                List<org.dromara.insurance.domain.bo.ServiceFeeConfig> sortedConfigs = configs.stream()
                    .filter(c -> c.getEffectiveStartTime() != null)
                    .sorted(Comparator.comparing(org.dromara.insurance.domain.bo.ServiceFeeConfig::getEffectiveStartTime))
                    .collect(Collectors.toList());

                // 校验时间段是否有交集
                for (int i = 0; i < sortedConfigs.size() - 1; i++) {
                    org.dromara.insurance.domain.bo.ServiceFeeConfig current = sortedConfigs.get(i);
                    org.dromara.insurance.domain.bo.ServiceFeeConfig next = sortedConfigs.get(i + 1);

                    // 如果当前配置没有失效时间(表示永久)，而后面还有新的生效配置，说明重叠了
                    if (current.getEffectiveEndTime() == null) {
                        throw new ServiceException("服务费配置的时间段存在重叠：配置了永久有效后，不能再添加后续时间段的配置");
                    }

                    // 正常的重叠：当前失效时间 > 后一个的生效时间 (允许等于，即 00:00:00 结束，下一个 00:00:00 开始)
                    if (current.getEffectiveEndTime().after(next.getEffectiveStartTime())) {
                        throw new ServiceException("服务费配置的时间段存在重叠，请检查配置项！");
                    }
                }

                // 校验自身时间逻辑：生效时间不能晚于失效时间
                for (org.dromara.insurance.domain.bo.ServiceFeeConfig config : sortedConfigs) {
                    if (config.getEffectiveEndTime() != null && config.getEffectiveStartTime().after(config.getEffectiveEndTime())) {
                        throw new ServiceException("服务费配置有误：生效时间不能晚于失效时间！");
                    }
                }
            }
        }
        // ===============================================================

        InsuranceProductConfig mainProduct = BeanUtil.copyProperties(productBo, InsuranceProductConfig.class);

        if (mainProduct.getId() == null) {
            baseMapper.insert(mainProduct);
        } else {
            baseMapper.updateById(mainProduct);
        }

        // 拿到最新生成的主键 ID
        Long productId = mainProduct.getId();

        // 2. 处理保障责任表 (1对N) - 策略：先删后插 (最简单稳妥)
        liabilityMapper.delete(new LambdaQueryWrapper<InsuranceProductLiability>()
            .eq(InsuranceProductLiability::getProductId, productId));

        if (CollUtil.isNotEmpty(formBo.getLiabilityList())) {
            List<InsuranceProductLiability> liabilities = formBo.getLiabilityList().stream().map(bo -> {
                InsuranceProductLiability entity = BeanUtil.copyProperties(bo, InsuranceProductLiability.class);
                entity.setProductId(productId); // 强行绑定主键
                entity.setId(null); // 清空前端可能传过来的脏 ID
                return entity;
            }).collect(Collectors.toList());

            liabilityMapper.insertBatch(liabilities);
        }

        // 3. 处理详情表 (1对1) - 包含所有 JSON 富媒体
        InsuranceProductDetail detail = BeanUtil.copyProperties(formBo, InsuranceProductDetail.class);
        detail.setProductId(productId);

        // 判断详情表是新增还是更新
        InsuranceProductDetail existDetail = detailMapper.selectOne(
            new LambdaQueryWrapper<InsuranceProductDetail>().eq(InsuranceProductDetail::getProductId, productId)
        );
        if (existDetail == null) {
            detailMapper.insert(detail);
        } else {
            detail.setId(existDetail.getId());
            detailMapper.updateById(detail);
        }
    }

    @Override
    public InsuranceProductSaveBo getProductFull(Long id) {
        // 🌟 核心改造：使用 TenantHelper.dynamic 临时将当前线程的租户身份切换为 "000000"
        return TenantHelper.dynamic("000000", () -> {

            InsuranceProductSaveBo resultBo = new InsuranceProductSaveBo();

            // 1. 查主表 (此时底层 SQL 会自动拼接 WHERE tenant_id = '000000')
            InsuranceProductConfig mainProduct = baseMapper.selectById(id);
            if (mainProduct == null) {
                throw new ServiceException("产品不存在");
            }
            InsuranceProductConfigBo configBo = BeanUtil.copyProperties(mainProduct, InsuranceProductConfigBo.class);
            resultBo.setProduct(configBo);

            // 2. 查责任表
            List<InsuranceProductLiability> liabilities = liabilityMapper.selectList(
                new LambdaQueryWrapper<InsuranceProductLiability>()
                    .eq(InsuranceProductLiability::getProductId, id)
                    .orderByAsc(InsuranceProductLiability::getSort)
            );
            resultBo.setLiabilityList(BeanUtil.copyToList(liabilities, InsuranceProductLiabilityBo.class));

            // 3. 查详情表
            InsuranceProductDetail detail = detailMapper.selectOne(
                new LambdaQueryWrapper<InsuranceProductDetail>().eq(InsuranceProductDetail::getProductId, id)
            );

            if (detail != null) {
                // 将 detail 里的字段拷贝到大 BO 里 (包括那些 JSON List)
                BeanUtil.copyProperties(detail, resultBo);

                // 4. 手动翻译 OSS 图片 ID 为真实 URL
                // (注: 这个方法内部咱们之前用了 TenantHelper.ignore()，在 RuoYi 中嵌套使用 Helper 是完全兼容且安全的)
                translateOssIdsToUrls(resultBo);
            }

            return resultBo;

        }); // 结束 dynamic 代码块，自动恢复为当前登录用户的真实租户身份
    }

    @Override
    public String getServiceFeeConfig(Long productId) {
        return TenantHelper.ignore(() -> {
            InsuranceProductConfig config = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceProductConfig>()
                .select(InsuranceProductConfig::getServiceFeeConfig)
                .eq(InsuranceProductConfig::getId, productId)
                // 2. 拦截器被屏蔽后，我们手动指定要去查 000000 租户的数据
                .eq(InsuranceProductConfig::getTenantId, "000000"));
            return config != null ? config.getServiceFeeConfig() : null;
        });
    }

    /**
     * 手动翻译 OSS 图片/文件 ID 为真实 URL
     */
    private void translateOssIdsToUrls(InsuranceProductSaveBo bo) {
        List<Long> ossIdList = new ArrayList<>();

        // 🌟 0. 新增：收集主产品基础信息中的 头图/入口图 ID
        if (bo.getProduct() != null) {
            String mainImgUrl = bo.getProduct().getImgUrl();
            if (StringUtils.isNotBlank(mainImgUrl) && mainImgUrl.matches("\\d+")) {
                ossIdList.add(Long.valueOf(mainImgUrl));
            }
        }

        // 🌟 1. 收集产品特点图 ID (增加防御：仅纯数字才去翻译)
        if (CollUtil.isNotEmpty(bo.getFeatureImages())) {
            bo.getFeatureImages().forEach(idStr -> {
                if (StringUtils.isNotBlank(idStr) && idStr.matches("\\d+")) {
                    ossIdList.add(Long.valueOf(idStr));
                }
            });
        }

        // 🌟 2. 收集理赔流程图 ID (增加防御：仅纯数字才去翻译)
        if (CollUtil.isNotEmpty(bo.getClaimImages())) {
            bo.getClaimImages().forEach(idStr -> {
                if (StringUtils.isNotBlank(idStr) && idStr.matches("\\d+")) {
                    ossIdList.add(Long.valueOf(idStr));
                }
            });
        }

        // 3. 收集条款文件的 ID (之前已经写好防御了)
        if (CollUtil.isNotEmpty(bo.getClauseFiles())) {
            bo.getClauseFiles().forEach(clause -> {
                if (StringUtils.isNotBlank(clause.getFileUrl()) && clause.getFileUrl().matches("\\d+")) {
                    ossIdList.add(Long.valueOf(clause.getFileUrl()));
                }
            });
        }

        if (CollUtil.isNotEmpty(ossIdList)) {
            // 开启上帝视角查 OSS
            Map<String, String> urlMap = TenantHelper.ignore(() -> {
                List<SysOssVo> ossList = sysOssService.listByIds(ossIdList);
                return ossList.stream().collect(Collectors.toMap(
                    oss -> String.valueOf(oss.getOssId()), SysOssVo::getUrl
                ));
            });

            // 🌟 4. 新增：回写主产品头图的真实 URL
            if (bo.getProduct() != null) {
                String mainImgUrl = bo.getProduct().getImgUrl();
                if (StringUtils.isNotBlank(mainImgUrl)) {
                    bo.setImgUrl(urlMap.getOrDefault(mainImgUrl, mainImgUrl));
                }
            }

            // 3. 回写图片 URL
            if (CollUtil.isNotEmpty(bo.getFeatureImages())) {
                bo.setFeatureImages(bo.getFeatureImages().stream().map(id -> urlMap.getOrDefault(id, id)).collect(Collectors.toList()));
            }
            if (CollUtil.isNotEmpty(bo.getClaimImages())) {
                bo.setClaimImages(bo.getClaimImages().stream().map(id -> urlMap.getOrDefault(id, id)).collect(Collectors.toList()));
            }

            // 🌟 4. 新增：回写条款文件的真实 URL
            if (CollUtil.isNotEmpty(bo.getClauseFiles())) {
                bo.getClauseFiles().forEach(clause -> {
                    if (StringUtils.isNotBlank(clause.getFileUrl())) {
                        clause.setFileUrl(urlMap.getOrDefault(clause.getFileUrl(), clause.getFileUrl()));
                    }
                });
            }
        }
    }
}
