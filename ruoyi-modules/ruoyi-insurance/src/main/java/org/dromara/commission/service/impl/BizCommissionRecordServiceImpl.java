package org.dromara.commission.service.impl;

import org.dromara.commission.domain.*;
import org.dromara.commission.domain.bo.AppCommissionQueryBo;
import org.dromara.commission.domain.vo.AppCommissionItemVo;
import org.dromara.commission.domain.vo.CommissionSummaryVo;
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
import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.service.ISysDeptService;
import org.springframework.stereotype.Service;
import org.dromara.commission.domain.bo.BizCommissionRecordBo;
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.mapper.BizCommissionRecordMapper;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 佣金分配明细Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizCommissionRecordServiceImpl implements IBizCommissionRecordService {

    private final BizCommissionRecordMapper baseMapper;

    private final IInsuranceProductCommissionService insuranceProductCommissionService;

    private final IBizCommissionProductService bizCommissionProductService;

    private final IBizCommissionDeptService bizCommissionDeptService;

    private final ISysDeptService sysDeptService;

    /**
     * 查询佣金分配明细
     *
     * @param id 主键
     * @return 佣金分配明细
     */
    @Override
    public BizCommissionRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询佣金分配明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 佣金分配明细分页列表
     */
    @Override
    public TableDataInfo<BizCommissionRecordVo> queryPageList(BizCommissionRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizCommissionRecord> lqw = buildQueryWrapper(bo);
        Page<BizCommissionRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的佣金分配明细列表
     *
     * @param bo 查询条件
     * @return 佣金分配明细列表
     */
    @Override
    public List<BizCommissionRecordVo> queryList(BizCommissionRecordBo bo) {
        LambdaQueryWrapper<BizCommissionRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizCommissionRecord> buildQueryWrapper(BizCommissionRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizCommissionRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BizCommissionRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getPolicyNo()), BizCommissionRecord::getPolicyNo, bo.getPolicyNo());
        lqw.eq(bo.getCreateTime() != null, BizCommissionRecord::getCreateTime, bo.getCreateTime());
        return lqw;
    }

    /**
     * 新增佣金分配明细
     *
     * @param bo 佣金分配明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizCommissionRecordBo bo) {
        BizCommissionRecord add = MapstructUtils.convert(bo, BizCommissionRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改佣金分配明细
     *
     * @param bo 佣金分配明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizCommissionRecordBo bo) {
        BizCommissionRecord update = MapstructUtils.convert(bo, BizCommissionRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizCommissionRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除佣金分配明细信息
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

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void calcCommission(CalcCommission calcCommission) {
//        // 1. 查询并校验产品基础佣金费率
//        Long productId = calcCommission.getProductId();
//        InsuranceProductCommission productBase = insuranceProductCommissionService.queryByProductIdAndTenantId(productId, calcCommission.getTenantId());
//
//        // 💡 优化：使用 Objects.equals 防止空指针
//        if (productBase == null || !Objects.equals(0, productBase.getStatus())) {
//            throw new ServiceException("产品基础佣金费率不存在或未启用");
//        }
//
//        Date now = new Date();
//        if (!isEffective(now, productBase.getEffectiveTime(), productBase.getExpirationTime())) {
//            throw new ServiceException("产品基础佣金费率不在有效期内");
//        }
//
//        // 计算当前保单总佣金基数
//        BigDecimal totalCommission = calcCommission.getPolicyPremium().multiply(productBase.getCommissionRate());
//
//        // 2. 尝试匹配特殊产品佣金费率 (策略0)
//        BizCommissionProduct specialProduct = bizCommissionProductService.queryByProductIdAndTenantId(productId, calcCommission.getTenantId());
//        if (specialProduct != null && Objects.equals(0, specialProduct.getStatus())
//            && isEffective(now, specialProduct.getEffectiveStart(), specialProduct.getEffectiveEnd())) {
//            calculateAndSaveRecord(calcCommission, totalCommission,
//                specialProduct.getSalesRatio(), specialProduct.getTeamRatio(), specialProduct.getProjectRatio(), 0);
//            return;
//        }
//
//        // ================= 3. 尝试匹配机构佣金费率 (策略1) =================
//
//        // 🌟 修复：不能直接用底层 createDeptId 查，必须向上找到该租户的顶层机构
//        Long topLevelDeptId = calcCommission.getCreateDeptId();
//        SysDeptVo currentSalesDept = sysDeptService.selectDeptById(topLevelDeptId);
//
//        if (currentSalesDept != null && StringUtils.isNotBlank(currentSalesDept.getAncestors())) {
//            String[] ids = currentSalesDept.getAncestors().split(",");
//            // RuoYi 规则：索引 0 是 '0'，索引 1 通常是该租户的根节点（顶层机构）
//            if (ids.length > 1) {
//                topLevelDeptId = Long.valueOf(ids[1]);
//            }
//        }
//
//        // 拿顶层机构 ID 去查机构佣金配置
//        BizCommissionDept deptCommission = bizCommissionDeptService.queryByDeptIdAndTenantId(topLevelDeptId, calcCommission.getTenantId());
//
//        if (deptCommission != null && Objects.equals(0, deptCommission.getStatus())
//            && isEffective(now, deptCommission.getEffectiveStart(), deptCommission.getEffectiveEnd())) {
//            calculateAndSaveRecord(calcCommission, totalCommission,
//                deptCommission.getSalesRatio(), deptCommission.getTeamRatio(), deptCommission.getProjectRatio(), 1);
//            return;
//        }
//
//        // 4. 兜底处理
//        throw new ServiceException("未找到匹配且有效的佣金计算策略（特殊产品或机构费率）");
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calcCommission(CalcCommission calcCommission) {
        // 🌟 这里的 ID 现在是 100% 纯正的系统产品 ID
        Long productId = calcCommission.getProductId();
        String tenantId = calcCommission.getTenantId();
        Date now = new Date();

        // ================= 1. 查询并校验产品基础佣金费率 =================
        InsuranceProductCommission productBase = insuranceProductCommissionService.queryByProductIdAndTenantId(productId, tenantId);

        if (productBase == null || !Objects.equals(0, productBase.getStatus())) {
            throw new ServiceException("产品基础佣金费率不存在或未启用");
        }
        if (!isEffective(now, productBase.getEffectiveTime(), productBase.getExpirationTime())) {
            throw new ServiceException("产品基础佣金费率不在有效期内");
        }
        BigDecimal totalCommission = calcCommission.getPolicyPremium().multiply(productBase.getCommissionRate());

        // ================= 2. 尝试匹配特殊产品佣金费率 =================
        BizCommissionProduct specialProduct = bizCommissionProductService.queryByProductIdAndTenantId(productId, tenantId);
        if (specialProduct != null && Objects.equals(0, specialProduct.getStatus())
            && isEffective(now, specialProduct.getEffectiveStart(), specialProduct.getEffectiveEnd())) {
            calculateAndSaveRecord(calcCommission, totalCommission,
                specialProduct.getSalesRatio(), specialProduct.getTeamRatio(), specialProduct.getProjectRatio(), 0);
            return;
        }

        // ================= 3. 尝试匹配机构佣金费率 (向上寻根逻辑) =================
        Long topLevelDeptId = calcCommission.getCreateDeptId();
        SysDeptVo currentSalesDept = sysDeptService.selectDeptById(topLevelDeptId);

        if (currentSalesDept != null && StringUtils.isNotBlank(currentSalesDept.getAncestors())) {
            String[] ids = currentSalesDept.getAncestors().split(",");
            if (ids.length > 1) {
                topLevelDeptId = Long.valueOf(ids[1]);
            }
        }

        BizCommissionDept deptCommission = bizCommissionDeptService.queryByDeptIdAndTenantId(topLevelDeptId, tenantId);
        if (deptCommission != null && Objects.equals(0, deptCommission.getStatus())
            && isEffective(now, deptCommission.getEffectiveStart(), deptCommission.getEffectiveEnd())) {
            calculateAndSaveRecord(calcCommission, totalCommission,
                deptCommission.getSalesRatio(), deptCommission.getTeamRatio(), deptCommission.getProjectRatio(), 1);
            return;
        }

        // ================= 4. 兜底处理 =================
        throw new ServiceException("未找到匹配且有效的佣金计算策略");
    }

    @Override
    public CommissionSummaryVo getAppCommissionSummary(Long userId, String queryMonth) {
        // 直接调用 Mapper 里的聚合 SQL
        CommissionSummaryVo summary = baseMapper.getCommissionSummary(userId, queryMonth);

        // 防空指针兜底 (如果该业务员一单都没开，数据库 SUM 会返回 null)
        if (summary == null) {
            summary = new CommissionSummaryVo();
            summary.setTotalAmount(BigDecimal.ZERO);
            summary.setMonthAmount(BigDecimal.ZERO);
        } else {
            if (summary.getTotalAmount() == null) summary.setTotalAmount(BigDecimal.ZERO);
            if (summary.getMonthAmount() == null) summary.setMonthAmount(BigDecimal.ZERO);
        }
        return summary;
    }

    @Override
    public TableDataInfo<AppCommissionItemVo> queryAppCommissionPageList(AppCommissionQueryBo bo, PageQuery pageQuery, Long userId) {
        // 利用 MyBatis-Plus 的分页插件 + 手写 XML SQL
        Page<AppCommissionItemVo> page = baseMapper.queryAppCommissionList(pageQuery.build(), bo, userId);
        return TableDataInfo.build(page);
    }

    /**
     * 计算并保存记录 (修复财务计算 Bug)
     */
    private void calculateAndSaveRecord(CalcCommission calcCommission, BigDecimal totalCommission,
                                        BigDecimal sRatio, BigDecimal tRatio, BigDecimal pRatio, Integer strategy) {

        // 🌟 修复：必须全部使用乘法，并保留 2 位小数，防止精度丢失和截留平台利润
        BigDecimal salesCommission = totalCommission.multiply(sRatio != null ? sRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal teamCommission = totalCommission.multiply(tRatio != null ? tRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectCommission = totalCommission.multiply(pRatio != null ? pRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        InsertCommission param = new InsertCommission();
        param.setCalcCommission(calcCommission);
        param.setTotalCommission(totalCommission);
        param.setSalesCommission(salesCommission);
        param.setTeamCommission(teamCommission);
        param.setProjectCommission(projectCommission);
        // ... 保存比例等基础信息
        param.setSalesRatio(sRatio);
        param.setTeamRatio(tRatio);
        param.setProjectRatio(pRatio);
        param.setCalcStrategy(strategy);

        insertCommissionRecord(param);
    }

    /**
     * 校验时间是否在有效期内 (包含边界)
     */
    private boolean isEffective(Date now, Date start, Date end) {
        if (now == null) return false;
        boolean afterStart = (start == null) || !now.before(start);
        boolean beforeEnd = (end == null) || !now.after(end);
        return afterStart && beforeEnd;
    }

    /**
     * 插入佣金记录 (彻底解决身份重叠与越级的终极方案)
     */
    private void insertCommissionRecord(InsertCommission param) {
        BizCommissionRecordBo recordBo = new BizCommissionRecordBo();
        CalcCommission cc = param.getCalcCommission();

        Long salesUserId = cc.getSalesUserId();
        Long teamUserId = cc.getTeamUserId();     // 💡 注意：这里可能为 null
        Long projectUserId = cc.getProjectUserId();

        // ... 省略基础字段的 set 赋值 (PolicyId, ProductId, 名字等) ...
        recordBo.setPolicyId(cc.getPolicyId());
        recordBo.setPolicyNo(cc.getPolicyNo());
        recordBo.setProductId(cc.getProductId());
        recordBo.setCommissionBase(param.getTotalCommission());
        recordBo.setSalesUserId(salesUserId);
        recordBo.setTeamUserId(teamUserId);
        recordBo.setProjectUserId(projectUserId);
        recordBo.setCalcStrategy(param.getCalcStrategy());
        recordBo.setSalesRatio(param.getSalesRatio());
        recordBo.setTeamRatio(param.getTeamRatio());
        recordBo.setProjectRatio(param.getProjectRatio());
        recordBo.setSalesUserName(cc.getSalesUserName());
        recordBo.setTeamUserName(cc.getTeamUserName());
        recordBo.setProjectUserName(cc.getProjectUserName());

        // ================== 🌟 核心重构：资金向上翻滚 (Roll-up) 逻辑 ==================
        // 初始应发金额
        BigDecimal finalSales = param.getSalesCommission();
        BigDecimal finalTeam = param.getTeamCommission();
        BigDecimal finalProject = param.getProjectCommission();

        // 规则 1：如果业务员就是团队负责人，【团队负责人】吃掉【业务员】的那份
        if (Objects.equals(salesUserId, teamUserId)) {
            finalTeam = finalTeam.add(finalSales);
            finalSales = BigDecimal.ZERO;
        }

        // 规则 2：如果【团队】为空（即越级直属），或【团队】等于【项目】，【项目负责人】吃掉【团队】那份
        if (teamUserId == null || Objects.equals(teamUserId, projectUserId)) {
            finalProject = finalProject.add(finalTeam);
            finalTeam = BigDecimal.ZERO;
        }

        // 规则 3：极致重叠防御（如果老总亲自卖单，此时钱可能堆在 finalSales 或 finalTeam 里，全部回滚给 Project）
        if (Objects.equals(salesUserId, projectUserId)) {
            finalProject = finalProject.add(finalSales).add(finalTeam);
            finalSales = BigDecimal.ZERO;
            finalTeam = BigDecimal.ZERO;
        }

        // 最终赋值
        recordBo.setSalesAmount(finalSales);
        recordBo.setTeamAmount(finalTeam);
        recordBo.setProjectAmount(finalProject);
        // =========================================================================

        recordBo.setCreateBy(cc.getCreateById());
        recordBo.setUpdateBy(cc.getCreateById());
        recordBo.setCreateDept(cc.getCreateDeptId());

        Boolean flag = insertByBo(recordBo);
        if (!flag) {
            throw new ServiceException("插入佣金记录失败");
        }
    }
}
