package org.dromara.insurance.service.impl;

import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.service.IInsuranceApplyRecordService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Collection;


/**
 * 投保记录Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceApplyRecordServiceImpl implements IInsuranceApplyRecordService {

    private final InsuranceApplyRecordMapper baseMapper;

    private final IBizCommissionRecordService bizCommissionRecordService;
    private final ISysUserService sysUserService;
    private final ISysDeptService sysDeptService;
    private final ApplicationContext applicationContext;

    /**
     * 查询投保记录
     *
     * @param id 主键
     * @return 投保记录
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public InsuranceApplyRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询投保记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保记录分页列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的投保记录列表
     *
     * @param bo 查询条件
     * @return 投保记录列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceApplyRecord> buildQueryWrapper(InsuranceApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
        lqw.eq(bo.getStatus() != null, InsuranceApplyRecord::getStatus, bo.getStatus());
        lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());
        return lqw;
    }

    /**
     * 新增投保记录
     *
     * @param bo 投保记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord add = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改投保记录
     *
     * @param bo 投保记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord update = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceApplyRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除投保记录信息
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
    public Boolean handleOrderPaySuccess(Long orderId) {
        // 1. 根据订单号查出当前订单
        InsuranceApplyRecordVo order = baseMapper.selectVoById(orderId);
        if(order == null){
            throw new ServiceException("订单不存在");
        }
        // 2. 幂等性校验
        if(order.getStatus() == 0){
            return true;
        }

        // 3. 🌟 核心防御：CAS 状态机更新 (绝对防止按钮连点、双重并发)
        // 相当于 SQL: UPDATE table SET status = 2 WHERE id = ? AND status = ?
        int rows = baseMapper.update(null, Wrappers.<InsuranceApplyRecord>lambdaUpdate()
            .set(InsuranceApplyRecord::getStatus, 0)
            .eq(InsuranceApplyRecord::getId, order.getId())
            .eq(InsuranceApplyRecord::getStatus, order.getStatus()) // 要求旧状态必须一致
        );

        // 如果 rows == 0，说明被别的连点请求抢先更新了，直接当作成功返回，阻断后续算佣金
        if (rows == 0){
            log.warn("并发拦截：订单 {} 已被处理", order.getOrderNo());
            return true;
        }

        // 4. 构建佣金计算参数
        CalcCommission calcParam = buildCalcParam(order);

        // 5. 触发佣金计算逻辑
        applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));

        return true;
    }

    /**
     * 构建佣金计算参数
     */
    private CalcCommission buildCalcParam(InsuranceApplyRecordVo order) {
        Long agentUserId = order.getAgentUserId();
        Long agentDeptId = order.getAgentDeptId();
        SysUserVo salesUser = sysUserService.selectUserById(agentUserId);
        if (salesUser == null) {
            throw new ServiceException("业务员信息不存在");
        }

        CalcCommission calcParam = new CalcCommission();
        calcParam.setPolicyId(order.getId());
        calcParam.setPolicyNo(order.getOrderNo());
        calcParam.setProductId(order.getProductId());
        calcParam.setPolicyPremium(order.getPremium());
        calcParam.setTenantId(salesUser.getTenantId());
        calcParam.setCreateById(agentUserId);
        calcParam.setCreateDeptId(agentDeptId);

        // 🌟 角色寻址逻辑 (同步自 OpenPolicyFacadeServiceImpl)
        List<SysRoleVo> roles = salesUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new ServiceException("该业务员未配置角色，无法计算佣金");
        }
        String roleKey = roles.get(0).getRoleKey();
        SysDeptVo currentDept = sysDeptService.selectDeptById(agentDeptId);
        if (currentDept == null) {
            throw new ServiceException("业务员所属机构不存在");
        }
        String deptCategory = currentDept.getDeptCategory();

        if ("bizman".equals(roleKey)) { // 业务员
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());

            Long directLeaderId = currentDept.getLeader();
            SysUserVo directLeader = directLeaderId != null ? sysUserService.selectUserById(directLeaderId) : null;
            String directLeaderName = directLeader != null ? directLeader.getNickName() : "";

            if ("2".equals(deptCategory)) { // 挂在团队下
                calcParam.setTeamUserId(directLeaderId);
                calcParam.setTeamUserName(directLeaderName);

                SysDeptVo parentDept = sysDeptService.selectDeptById(currentDept.getParentId());
                if (parentDept != null) {
                    Long projectLeaderId = parentDept.getLeader();
                    SysUserVo projectLeader = projectLeaderId != null ? sysUserService.selectUserById(projectLeaderId) : null;
                    calcParam.setProjectUserId(projectLeaderId);
                    calcParam.setProjectUserName(projectLeader != null ? projectLeader.getNickName() : "");
                }
            } else if ("1".equals(deptCategory)) { // 越级直挂项目组
                calcParam.setTeamUserId(null);
                calcParam.setTeamUserName("");
                calcParam.setProjectUserId(directLeaderId);
                calcParam.setProjectUserName(directLeaderName);
            }
        } else if ("teamleader".equals(roleKey)) { // 团队负责人自己出单
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());
            calcParam.setTeamUserId(agentUserId);
            calcParam.setTeamUserName(salesUser.getNickName());

            SysDeptVo parentDept = sysDeptService.selectDeptById(currentDept.getParentId());
            if (parentDept != null) {
                Long projectLeaderId = parentDept.getLeader();
                SysUserVo projectLeader = projectLeaderId != null ? sysUserService.selectUserById(projectLeaderId) : null;
                calcParam.setProjectUserId(projectLeaderId);
                calcParam.setProjectUserName(projectLeader != null ? projectLeader.getNickName() : "");
            }
        } else { // 项目负责人（大老板）亲自出单
            calcParam.setSalesUserId(agentUserId);
            calcParam.setSalesUserName(salesUser.getNickName());
            calcParam.setTeamUserId(agentUserId);
            calcParam.setTeamUserName(salesUser.getNickName());
            calcParam.setProjectUserId(agentUserId);
            calcParam.setProjectUserName(salesUser.getNickName());
        }
        return calcParam;
    }
}
