package org.dromara.insurance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.commission.service.IBizCommissionDeptService;
import org.dromara.commission.service.IBizCommissionProductService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.ResultModel;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.insurance.domain.dto.PolicyDto;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.insurance.service.IOpenPolicyFacadeService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenPolicyFacadeServiceImpl implements IOpenPolicyFacadeService {

    private final IInsurancePolicyService insurancePolicyService;

    private final IInsuranceProductConfigService insuranceProductConfigService;

    private final ISysUserService sysUserService;

    private final ISysDeptService sysDeptService;

    private final ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel processCallback(PolicyCallbackDto policyCallbackDto) {
        if (policyCallbackDto == null || policyCallbackDto.getPolicyDto() == null) {
            log.error("保单数据为空，无法处理回调");
            throw new ServiceException("保单数据为空，无法处理回调");
        }
        PolicyDto policyDto = policyCallbackDto.getPolicyDto();
        // 查询业务人员信息 并获取tenantId
        Long userId = Long.valueOf(policyDto.getAgentCode());
        SysUserVo sysUserVo = sysUserService.selectUserById(userId);
        if(sysUserVo == null){
            log.error("agent_id对应的用户不存在，agentId={}", policyDto.getAgentCode());
            throw new ServiceException("agent_id对应的用户不存在");
        }
        // 该用户的租户id
        String tenantId = sysUserVo.getTenantId();
        // 查询产品详情
        String productPlanCode = policyDto.getProductPlanCode();
        InsuranceProductConfig product = insuranceProductConfigService.queryByProductCodeAndTenantId(productPlanCode, tenantId);
        if (product == null) {
            log.error("产品不存在，productCode={}", productPlanCode);
            throw new ServiceException("产品不存在");
        }
        // 新增保单
        // 查询该保单号是否已存在
        String policyNo = policyDto.getPolicyNo();
        InsurancePolicy existingPolicy = insurancePolicyService.queryByPolicyNoAndTenantId(policyNo, tenantId);
        if (existingPolicy != null) {
            log.info("保单号已存在，policyNo={}", policyNo);
            return ResultModel.success("该保单号对应数据已存在");
        }

        Long createById = null;
        Long createDeptId = null;
        CalcCommission calcCommissionParam = new CalcCommission();

        // 查询团队信息
        // 当前user的部门id
        Long deptId = sysUserVo.getDeptId();

        // 先确认当前agent_id对应的用户身份
        // 职级代码
        String roleKey = sysUserVo.getRoles().get(0).getRoleKey();

        if(roleKey.equals("bizman")){// 业务员
            calcCommissionParam.setSalesUserId(sysUserVo.getUserId());
            calcCommissionParam.setSalesUserName(sysUserVo.getUserName());

            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(deptId);
            // leader 为团队负责人id
            Long teamLeaderId = sysDeptVo.getLeader();
            SysUserVo teamLeader = sysUserService.selectUserById(teamLeaderId);
            String teamLeaderName = teamLeader.getNickName();

            calcCommissionParam.setTeamUserId(teamLeaderId);
            calcCommissionParam.setTeamUserName(teamLeaderName);

            if(sysDeptVo.getDeptCategory().equals("2")){
                //获取父id再次查询
                SysDeptVo parentDept = sysDeptService.selectDeptById(sysDeptVo.getParentId());
                // leader 为团队负责人 id
                Long projectLeaderId = parentDept.getLeader();
                SysUserVo projectLeader = sysUserService.selectUserById(projectLeaderId);
                String projectLeaderName = projectLeader.getNickName();

                calcCommissionParam.setProjectUserId(projectLeaderId);
                calcCommissionParam.setProjectUserName(projectLeaderName);
                createById = projectLeaderId;
                createDeptId = parentDept.getDeptId();
            }
        }else if(roleKey.equals("teamleader")){// 团队负责人
            calcCommissionParam.setSalesUserName(sysUserVo.getUserName());
            calcCommissionParam.setSalesUserId(sysUserVo.getUserId());
            calcCommissionParam.setTeamUserId(sysUserVo.getUserId());
            calcCommissionParam.setTeamUserName(sysUserVo.getUserName());

            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(deptId);
            Long projectLeaderId = sysDeptVo.getLeader();
            SysUserVo projectLeader = sysUserService.selectUserById(projectLeaderId);
            String projectLeaderName = projectLeader.getNickName();

            calcCommissionParam.setProjectUserId(projectLeaderId);
            calcCommissionParam.setProjectUserName(projectLeaderName);
            createById = projectLeaderId;
            createDeptId = sysDeptVo.getDeptId();
        }else{ // 项目负责人
            calcCommissionParam.setSalesUserName(sysUserVo.getUserName());
            calcCommissionParam.setSalesUserId(sysUserVo.getUserId());
            calcCommissionParam.setTeamUserName(sysUserVo.getUserName());
            calcCommissionParam.setTeamUserId(sysUserVo.getUserId());
            calcCommissionParam.setProjectUserId(sysUserVo.getUserId());
            calcCommissionParam.setProjectUserName(sysUserVo.getUserName());

            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(deptId);
            createById = sysUserVo.getUserId();
            createDeptId = sysDeptVo.getDeptId();
        }

        // 如果上面推导了一圈，createById 还是 null，就兜底用当前业务员的 ID
        Long finalCreateById = (createById != null) ? createById : sysUserVo.getUserId();
        Long finalCreateDeptId = (createDeptId != null) ? createDeptId : sysUserVo.getDeptId();

        // 保存保单
        Long[] policyIdHolder = new Long[1]; // 用于在 Lambda 外接收 ID
        StpUtil.switchTo(finalCreateById, () -> {
            // 只有真正入库的动作才需要穿马甲
            policyIdHolder[0] = createPolicy(policyCallbackDto, product, sysUserVo, tenantId, finalCreateDeptId);
        });

        calcCommissionParam.setPolicyId(policyIdHolder[0]);
        calcCommissionParam.setPolicyNo(policyNo);
        calcCommissionParam.setProductId(product.getId());
        calcCommissionParam.setCreateById(finalCreateById);     // 传入兜底后的安全 ID
        calcCommissionParam.setCreateDeptId(finalCreateDeptId); // 传入兜底后的安全部门 ID
        calcCommissionParam.setTenantId(tenantId);
        calcCommissionParam.setPolicyPremium(new BigDecimal(policyDto.getPrem()));

        //调用佣金计算
        applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcCommissionParam));

        return ResultModel.success(calcCommissionParam);
    }

    @Override
    public SysUserVo getUserByUserId(Long userId) {
        return sysUserService.selectUserById(userId);
    }

    /**
     * 新增保单
     */
    private Long createPolicy(PolicyCallbackDto policyCallbackDto,InsuranceProductConfig product,SysUserVo sysUser,String tenantId,Long createDeptId) {
        InsurancePolicyBo insurancePolicyBo = new InsurancePolicyBo();
        insurancePolicyBo.setProductId(product.getId());
        insurancePolicyBo.setProductCode(product.getProductCode());
        insurancePolicyBo.setProductName(product.getProductName());
        insurancePolicyBo.setPolicyNo(policyCallbackDto.getPolicyDto().getPolicyNo());
        insurancePolicyBo.setOrderNo(policyCallbackDto.getPolicyDto().getOrderNo());
        insurancePolicyBo.setAgentName(sysUser.getNickName());
        insurancePolicyBo.setAgentUserId(sysUser.getUserId());
        insurancePolicyBo.setAgentDeptId(sysUser.getDeptId());
        insurancePolicyBo.setPremium(new BigDecimal(policyCallbackDto.getPolicyDto().getPrem()));
        insurancePolicyBo.setAmt(new BigDecimal(policyCallbackDto.getPolicyDto().getAmt()));
        // 未结算
        insurancePolicyBo.setCommissionStatus(1);
        // 已生效
        insurancePolicyBo.setStatus(0);
        insurancePolicyBo.setAppntDate(DateUtil.parse(policyCallbackDto.getPolicyDto().getAppntDate()));
        insurancePolicyBo.setAccecptDate(DateUtil.parse(policyCallbackDto.getPolicyDto().getAcceptDate()));
        insurancePolicyBo.setPolicyStartDate(DateUtil.parse(policyCallbackDto.getPolicyDto().getPolicyStartDate()));
        insurancePolicyBo.setPolicyEndDate(DateUtil.parse(policyCallbackDto.getPolicyDto().getPolicyEndDate()));
        insurancePolicyBo.setApplicantName(policyCallbackDto.getAppntDto().getName());
        insurancePolicyBo.setApplicantSex(policyCallbackDto.getAppntDto().getSex());
        insurancePolicyBo.setApplicantIdNo(policyCallbackDto.getAppntDto().getIdNo());
        // 固定为身份证
        insurancePolicyBo.setApplicantIdType("0");
        insurancePolicyBo.setApplicantMobile(policyCallbackDto.getAppntDto().getMobile());
        // 暂时固定为其他
        insurancePolicyBo.setRelationshipToInsured("4");
        insurancePolicyBo.setInsuredName(policyCallbackDto.getInsuredDtoList().get(0).getName());
        insurancePolicyBo.setInsuredSex(policyCallbackDto.getInsuredDtoList().get(0).getSex());
        insurancePolicyBo.setInsuredIdNo(policyCallbackDto.getInsuredDtoList().get(0).getIdNo());
        // 固定为身份证
        insurancePolicyBo.setInsuredIdType("0");
        insurancePolicyBo.setInsuredMobile(policyCallbackDto.getInsuredDtoList().get(0).getMobile());

        Boolean flag = insurancePolicyService.insertByBo(insurancePolicyBo);
        if (!flag) {
            throw new ServiceException("新增保单失败");
        }

        return insurancePolicyBo.getId();
    }


}
