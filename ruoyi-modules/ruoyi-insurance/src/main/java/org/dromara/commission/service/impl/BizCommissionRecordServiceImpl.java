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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calcCommission(CalcCommission calcCommission) {
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
        log.info("保单号："+calcCommission.getPolicyNo()+",保单保费："+calcCommission.getPolicyPremium()+"，佣金计算基数为："+totalCommission);

        // ================= 2. 尝试匹配特殊产品佣金费率 =================
        BizCommissionProduct specialProduct = bizCommissionProductService.queryByProductIdAndTenantId(productId, tenantId);
        if (specialProduct != null && Objects.equals(0, specialProduct.getStatus())
            && isEffective(now, specialProduct.getEffectiveStart(), specialProduct.getEffectiveEnd())) {
            log.info("保单号："+calcCommission.getPolicyNo()+"匹配特殊产品佣金");
            log.info("业务员佣金比例：{}",specialProduct.getSalesRatio());
            log.info("团队佣金比例：{}",specialProduct.getTeamRatio());
            log.info("机构佣金比例：{}",specialProduct.getProjectRatio());
            // 🌟 传入 paymentMode 和 payerUserId (直接从 calcCommission 取即可)
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
            // 🌟 传入 paymentMode 和 payerUserId
            log.info("保单号："+calcCommission.getPolicyNo()+"匹配机构比例");
            log.info("业务员佣金比例：{}",deptCommission.getSalesRatio());
            log.info("团队佣金比例：{}",deptCommission.getTeamRatio());
            log.info("机构佣金比例：{}",deptCommission.getProjectRatio());
            calculateAndSaveRecord(calcCommission, totalCommission,
                deptCommission.getSalesRatio(), deptCommission.getTeamRatio(), deptCommission.getProjectRatio(), 1);
            return;
        }

        // ================= 4. 兜底处理 =================
        throw new ServiceException("未找到匹配且有效的佣金计算策略");
    }

    private void calculateAndSaveRecord(CalcCommission calcCommission, BigDecimal totalCommission,
                                        BigDecimal sRatio, BigDecimal tRatio, BigDecimal pRatio, Integer strategy) {

        // 🌟 1. 核心计算：全部使用乘法并四舍五入保留2位小数
        BigDecimal salesCommission = totalCommission.multiply(sRatio != null ? sRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal teamCommission = totalCommission.multiply(tRatio != null ? tRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectCommission = totalCommission.multiply(pRatio != null ? pRatio : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        log.info("保单号："+calcCommission.getPolicyNo()+"，业务员佣金："+salesCommission);
        log.info("保单号："+calcCommission.getPolicyNo()+"，团队佣金："+teamCommission);
        log.info("保单号："+calcCommission.getPolicyNo()+"，机构佣金："+projectCommission);

        // 🌟 2. 身份校验与抵扣判定
        // 1: 正常佣金(待发放/待结算)   2: 净费出单已抵扣(不需要发钱，前端不显示)
        boolean isNetPremium = calcCommission.getPaymentMode() != null && calcCommission.getPaymentMode() == 1;
        Long payerUserId = calcCommission.getPayerUserId();

        int salesStatus = (isNetPremium && calcCommission.getSalesUserId() != null && calcCommission.getSalesUserId().equals(payerUserId)) ? 2 : 1;
        int teamStatus = (isNetPremium && calcCommission.getTeamUserId() != null && calcCommission.getTeamUserId().equals(payerUserId)) ? 2 : 1;
        int projectStatus = (isNetPremium && calcCommission.getProjectUserId() != null && calcCommission.getProjectUserId().equals(payerUserId)) ? 2 : 1;

        // 💡 如果您坚持想在数据库里直接存为 0 (做法A)，可以把下面这行代码取消注释：
        // if (salesStatus == 2) salesCommission = BigDecimal.ZERO;

        // 🌟 3. 组装记录对象并落库
        InsertCommission param = new InsertCommission();
        param.setCalcCommission(calcCommission);
        param.setTotalCommission(totalCommission);

        param.setSalesCommission(salesCommission);
        param.setTeamCommission(teamCommission);
        param.setProjectCommission(projectCommission);

        param.setSalesRatio(sRatio);
        param.setTeamRatio(tRatio);
        param.setProjectRatio(pRatio);
        param.setCalcStrategy(strategy);

        // 设置发放状态
        param.setSalesStatus(salesStatus);
        param.setTeamStatus(teamStatus);
        param.setProjectStatus(projectStatus);

        // 落库：只记账，无任何钱包操作！
        insertCommissionRecord(param);

        log.info("单号:{} 佣金账单已生成。净费抵扣状态(1-正常 2-抵扣隐藏): 业务员-{}, 团队长-{}, 总监-{}",
            calcCommission.getPolicyNo(), salesStatus, teamStatus, projectStatus);
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
     * 校验时间是否在有效期内 (包含边界)
     */
    private boolean isEffective(Date now, Date start, Date end) {
        if (now == null) return false;
        boolean afterStart = (start == null) || !now.before(start);
        boolean beforeEnd = (end == null) || !now.after(end);
        return afterStart && beforeEnd;
    }

    private void insertCommissionRecord(InsertCommission param) {
        BizCommissionRecordBo recordBo = new BizCommissionRecordBo();
        CalcCommission cc = param.getCalcCommission();

        Long salesUserId = cc.getSalesUserId();
        Long teamUserId = cc.getTeamUserId();     // 💡 注意：这里可能为 null
        Long projectUserId = cc.getProjectUserId();

        // ... 基础字段赋值 ...
        recordBo.setPolicyId(cc.getPolicyId());
        recordBo.setPolicyNo(cc.getPolicyNo());
        recordBo.setProductId(cc.getProductId());
        recordBo.setProductName(cc.getProductName());
        // 🌟 将实交保费赋给记录的 premium 字段 (如果净费为空则兜底取原价)
        recordBo.setPremium(cc.getNetPremium() != null ? cc.getNetPremium() : cc.getPolicyPremium());
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

        // 🌟🌟🌟 新增：保存净费抵扣状态 (极其重要，1=正常，2=净费已前置抵扣)
        recordBo.setSalesStatus(param.getSalesStatus());
        recordBo.setTeamStatus(param.getTeamStatus());
        recordBo.setProjectStatus(param.getProjectStatus());

        // ================== 🌟 核心重构：资金向上翻滚 (Roll-up) 安全防弹逻辑 ==================
        // 防空指针保护
        BigDecimal finalSales = param.getSalesCommission() != null ? param.getSalesCommission() : BigDecimal.ZERO;
        BigDecimal finalTeam = param.getTeamCommission() != null ? param.getTeamCommission() : BigDecimal.ZERO;
        BigDecimal finalProject = param.getProjectCommission() != null ? param.getProjectCommission() : BigDecimal.ZERO;

        // 翻滚规则 1：如果没有团队长（越级直属），团队长的津贴真空，向上滚给总监
        if (teamUserId == null) {
            finalProject = finalProject.add(finalTeam);
            finalTeam = BigDecimal.ZERO;
        }

        // 翻滚规则 2：如果业务员就是团队长（身份重叠），业务员的钱滚给团队长名下
        if (Objects.equals(salesUserId, teamUserId)) {
            finalTeam = finalTeam.add(finalSales);
            finalSales = BigDecimal.ZERO;
        }

        // 翻滚规则 3：如果团队长就是总监（身份重叠），团队长的钱滚给总监名下
        if (Objects.equals(teamUserId, projectUserId)) {
            finalProject = finalProject.add(finalTeam);
            finalTeam = BigDecimal.ZERO;
        }

        // 翻滚规则 4：防漏网之鱼（如果老总亲自卖单，经过前面的规则，此时 finalSales 可能还有钱，需跨级滚给总监）
        if (Objects.equals(salesUserId, projectUserId)) {
            finalProject = finalProject.add(finalSales);
            finalSales = BigDecimal.ZERO;
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
