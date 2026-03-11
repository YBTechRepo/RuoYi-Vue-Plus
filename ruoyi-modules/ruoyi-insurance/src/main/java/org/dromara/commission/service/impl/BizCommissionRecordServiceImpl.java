package org.dromara.commission.service.impl;

import org.dromara.commission.domain.*;
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
import org.springframework.stereotype.Service;
import org.dromara.commission.domain.bo.BizCommissionRecordBo;
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.mapper.BizCommissionRecordMapper;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

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
        lqw.orderByAsc(BizCommissionRecord::getId);
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
        // 查询产品基础佣金费率
        Long productId = calcCommission.getProductId();
        InsuranceProductCommission insuranceProductCommission = insuranceProductCommissionService.queryByProductIdAndTenantId(productId, calcCommission.getTenantId());

        if (insuranceProductCommission == null || !insuranceProductCommission.getStatus().equals(0)) {
            throw new ServiceException("产品基础佣金费率不存在或未启用");
        }

        //判断是否在有效期限内
        Date now = new Date();
        if (now.before(insuranceProductCommission.getEffectiveTime()) || now.after(insuranceProductCommission.getExpirationTime())) {
            throw new ServiceException("产品基础佣金费率不在有效期内");
        }

        //基础费率 - 百分比
        BigDecimal commissionRate = insuranceProductCommission.getCommissionRate();
        //当前保单总佣金
        BigDecimal totalCommission = calcCommission.getPolicyPremium().multiply(commissionRate);
        // 业务员佣金
        BigDecimal salesCommission = BigDecimal.ZERO;
        // 团队佣金
        BigDecimal teamCommission = BigDecimal.ZERO;
        // 负责人佣金
        BigDecimal projectCommission = BigDecimal.ZERO;

        // 查询是否有特殊佣金费率
        BizCommissionProduct bizCommissionProduct = bizCommissionProductService.queryByProductIdAndTenantId(productId, calcCommission.getTenantId());
        boolean useSpecialCommission = false;

        if (bizCommissionProduct != null) {
            if(bizCommissionProduct.getStatus() == 0){
                // 检查特殊佣金是否在有效期内
                if(now.after(bizCommissionProduct.getEffectiveStart()) &&
                    now.before(bizCommissionProduct.getEffectiveEnd())){
                    // 有特殊佣金费率且在有效期内，使用特殊佣金费率
                    useSpecialCommission = true;
                    // 业务员佣金
                    salesCommission = bizCommissionProduct.getSalesRatio().multiply(totalCommission);
                    // 团队佣金
                    teamCommission = bizCommissionProduct.getTeamRatio().multiply(totalCommission);
                    // 负责人佣金
                    projectCommission = totalCommission.subtract(salesCommission).subtract(teamCommission);

                    InsertCommission insertCommissionParam = new InsertCommission();
                    insertCommissionParam.setCalcCommission(calcCommission);
                    insertCommissionParam.setProjectCommission(projectCommission);
                    insertCommissionParam.setSalesCommission(salesCommission);
                    insertCommissionParam.setTeamCommission(teamCommission);
                    insertCommissionParam.setTotalCommission(totalCommission);
                    insertCommissionParam.setSalesRatio(bizCommissionProduct.getSalesRatio());
                    insertCommissionParam.setTeamRatio(bizCommissionProduct.getTeamRatio());
                    insertCommissionParam.setProjectRatio(bizCommissionProduct.getProjectRatio());
                    insertCommissionParam.setCalcStrategy(0);
                    insertCommissionRecord(insertCommissionParam);
                }
            }
        }

        if (!useSpecialCommission) {
            // 查询机构佣金表
            // deptId 可以暂时取值 createDeptId
            BizCommissionDept bizCommissionDept = bizCommissionDeptService.queryByDeptIdAndTenantId(calcCommission.getCreateDeptId(), calcCommission.getTenantId());
            // 业务员佣金
            salesCommission = bizCommissionDept.getSalesRatio().multiply(totalCommission);
            // 团队佣金
            teamCommission = bizCommissionDept.getTeamRatio().multiply(totalCommission);
            // 负责人佣金
            projectCommission = totalCommission.subtract(salesCommission).subtract(teamCommission);

            InsertCommission insertCommissionParam = new InsertCommission();
            insertCommissionParam.setCalcCommission(calcCommission);
            insertCommissionParam.setProjectCommission(projectCommission);
            insertCommissionParam.setSalesCommission(salesCommission);
            insertCommissionParam.setTeamCommission(teamCommission);
            insertCommissionParam.setTotalCommission(totalCommission);
            insertCommissionParam.setSalesRatio(bizCommissionDept.getSalesRatio());
            insertCommissionParam.setTeamRatio(bizCommissionDept.getTeamRatio());
            insertCommissionParam.setProjectRatio(bizCommissionDept.getProjectRatio());
            insertCommissionParam.setCalcStrategy(1);
            insertCommissionRecord(insertCommissionParam);
        }
    }

    private void insertCommissionRecord(InsertCommission insertCommissionParam){
        BizCommissionRecordBo bizCommissionRecordBo = new BizCommissionRecordBo();

        Long salesUserId = insertCommissionParam.getCalcCommission().getSalesUserId();
        Long teamUserId = insertCommissionParam.getCalcCommission().getTeamUserId();
        Long projectUserId = insertCommissionParam.getCalcCommission().getProjectUserId();

        bizCommissionRecordBo.setPolicyId(insertCommissionParam.getCalcCommission().getPolicyId());
        bizCommissionRecordBo.setPolicyNo(insertCommissionParam.getCalcCommission().getPolicyNo());
        bizCommissionRecordBo.setProductId(insertCommissionParam.getCalcCommission().getProductId());
        bizCommissionRecordBo.setCommissionBase(insertCommissionParam.getTotalCommission());
        bizCommissionRecordBo.setSalesUserId(salesUserId);
        bizCommissionRecordBo.setTeamUserId(teamUserId);
        bizCommissionRecordBo.setProjectUserId(projectUserId);
        bizCommissionRecordBo.setCalcStrategy(insertCommissionParam.getCalcStrategy());
        bizCommissionRecordBo.setSalesRatio(insertCommissionParam.getSalesRatio());
        bizCommissionRecordBo.setTeamRatio(insertCommissionParam.getTeamRatio());
        bizCommissionRecordBo.setProjectRatio(insertCommissionParam.getProjectRatio());
        bizCommissionRecordBo.setSalesUserName(insertCommissionParam.getCalcCommission().getSalesUserName());
        bizCommissionRecordBo.setTeamUserName(insertCommissionParam.getCalcCommission().getTeamUserName());
        bizCommissionRecordBo.setProjectUserName(insertCommissionParam.getCalcCommission().getProjectUserName());

        BigDecimal sAmount = insertCommissionParam.getSalesCommission();
        BigDecimal tAmount = insertCommissionParam.getTeamCommission();
        BigDecimal pAmount = insertCommissionParam.getProjectCommission();

        if (salesUserId.equals(projectUserId) && salesUserId.equals(teamUserId)) {
            // 总负责人出单
            bizCommissionRecordBo.setSalesAmount(BigDecimal.ZERO);
            bizCommissionRecordBo.setTeamAmount(BigDecimal.ZERO);
            bizCommissionRecordBo.setProjectAmount(pAmount.add(sAmount).add(tAmount));

        } else if (salesUserId.equals(teamUserId)) {
            // 团队负责人出单
            bizCommissionRecordBo.setSalesAmount(BigDecimal.ZERO);
            bizCommissionRecordBo.setTeamAmount(tAmount.add(sAmount));
            bizCommissionRecordBo.setProjectAmount(pAmount); // 总监的钱不动

        } else {
            // 普通业务员出单
            bizCommissionRecordBo.setSalesAmount(sAmount);
            bizCommissionRecordBo.setTeamAmount(tAmount);
            bizCommissionRecordBo.setProjectAmount(pAmount);
        }

        Boolean flag = insertByBo(bizCommissionRecordBo);
        if(!flag){
            throw new ServiceException("插入佣金记录失败");
        }
    }
}
