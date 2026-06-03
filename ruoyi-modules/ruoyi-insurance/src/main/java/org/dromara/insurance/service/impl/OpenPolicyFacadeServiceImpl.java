package org.dromara.insurance.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.InsuranceTenantProduct;
import org.dromara.insurance.domain.ResultModel;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.insurance.domain.dto.PolicyDto;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.mapper.InsuranceTenantProductMapper;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.insurance.service.IOpenPolicyFacadeService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenPolicyFacadeServiceImpl implements IOpenPolicyFacadeService {

    private final IInsurancePolicyService insurancePolicyService;

    private final IInsuranceProductConfigService insuranceProductConfigService;

    private final InsuranceProductConfigMapper insuranceProductConfigMapper;

    private final InsuranceTenantProductMapper insuranceTenantProductMapper;

    private final ISysUserService sysUserService;

    private final ISysDeptService sysDeptService;

    private final ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel processCallback(PolicyCallbackDto policyCallbackDto) {
        if (policyCallbackDto == null || policyCallbackDto.getPolicy() == null) {
            log.error("保单数据为空，无法处理回调");
            throw new ServiceException("保单数据为空，无法处理回调");
        }
        PolicyDto policyDto = policyCallbackDto.getPolicy();

        // ================= 1. 锚定真实的“出单业务员”身份 =================
        String agentCode = policyDto.getAgentCode();
        Long agentId = Long.valueOf(agentCode.substring(2));
        SysUserVo salesUser = sysUserService.selectUserById(agentId);
        if (salesUser == null) {
            log.error("agent_id对应的出单用户不存在，agentId={}", policyDto.getAgentCode());
            throw new ServiceException("agent_id对应的用户不存在");
        }

        // 🌟 核心提取：这三个值是数据隔离的“定海神针”
        String tenantId = salesUser.getTenantId();
        Long salesUserId = salesUser.getUserId();
        Long salesDeptId = salesUser.getDeptId();

        // 开启多租户霸体（包裹整个保单处理生命周期）
        try {
            TenantHelper.setDynamic(tenantId);

            // ================= 2. 查产品详情与防重 =================
            String productPlanCode = policyDto.getProductPlanCode();

//            InsuranceProductConfig product = insuranceProductConfigService.queryByProductCodeAndTenantId(productPlanCode, tenantId);
//            if (product == null) {
//                throw new ServiceException("产品不存在");
//            }
            // 🌟 去平台总库（000000）查出真实的系统产品
            // 🌟 完美替换 1：用 Mapper 直接查平台总库
            InsuranceProductConfig product = TenantHelper.dynamic("000000", () -> {
                return insuranceProductConfigMapper.selectOne(
                    new LambdaQueryWrapper<InsuranceProductConfig>()
                        .eq(InsuranceProductConfig::getProductCode, productPlanCode)
                        .last("LIMIT 1") // 防御性限制，只取第一条
                );
            });
            if (product == null) {
                log.error("平台总库中不存在该产品代码，productCode={}", productPlanCode);
                throw new ServiceException("系统未配置该产品");
            }

            // 防止上面的 dynamic 方法在底层清空了上下文
            TenantHelper.setDynamic(tenantId);

            // 🌟 校验这件商品，是否真的在当前租户的“货架”上！
            // 🌟 完美替换 2：用 Mapper 校验这件商品是否在当前租户货架上
            InsuranceTenantProduct tenantProduct = insuranceTenantProductMapper.selectOne(
                new LambdaQueryWrapper<InsuranceTenantProduct>()
                    .eq(InsuranceTenantProduct::getProductId, product.getId())
                    // 底层依然会自动拼接 tenant_id = 当前回调业务员的租户ID
                    .last("LIMIT 1")
            );

            if (tenantProduct == null || !"0".equals(tenantProduct.getStatus())) {
                // 记录严重越权警告（可视业务情况决定是否 throw 阻断）
                log.warn("【越权出单警告】业务员卖出了未在机构货架上架的产品！tenantId={}, productId={}", tenantId, product.getId());
            }
            String policyNo = policyDto.getPolicyNo();
            InsurancePolicy existingPolicy = insurancePolicyService.queryByPolicyNoAndTenantId(policyNo, tenantId);
            if (existingPolicy != null) {
                return ResultModel.success("该保单号对应数据已存在");
            }

            // ================= 3. 精准架构寻址（上一回合我们定好的完美逻辑） =================
            CalcCommission calcParam = new CalcCommission();
            String roleKey = salesUser.getRoles().get(0).getRoleKey();
            SysDeptVo currentDept = sysDeptService.selectDeptById(salesDeptId);
            String deptCategory = currentDept.getDeptCategory();

            if (roleKey.equals("bizman")) { // 业务员
                calcParam.setSalesUserId(salesUserId);
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
            } else if (roleKey.equals("teamleader")) { // 团队负责人自己出单
                calcParam.setSalesUserId(salesUserId);
                calcParam.setSalesUserName(salesUser.getNickName());
                calcParam.setTeamUserId(salesUserId);
                calcParam.setTeamUserName(salesUser.getNickName());

                SysDeptVo parentDept = sysDeptService.selectDeptById(currentDept.getParentId());
                if (parentDept != null) {
                    Long projectLeaderId = parentDept.getLeader();
                    SysUserVo projectLeader = projectLeaderId != null ? sysUserService.selectUserById(projectLeaderId) : null;
                    calcParam.setProjectUserId(projectLeaderId);
                    calcParam.setProjectUserName(projectLeader != null ? projectLeader.getNickName() : "");
                }
            } else { // 项目负责人（大老板）亲自出单
                calcParam.setSalesUserId(salesUserId);
                calcParam.setSalesUserName(salesUser.getNickName());
                calcParam.setTeamUserId(salesUserId);
                calcParam.setTeamUserName(salesUser.getNickName());
                calcParam.setProjectUserId(salesUserId);
                calcParam.setProjectUserName(salesUser.getNickName());
            }

            // ================= 4. 锁定数据底层归属权 =================
            calcParam.setBizSource(2);
            calcParam.setPolicyNo(policyNo);
            calcParam.setProductId(product.getId());
            calcParam.setProductName(product.getProductName());
            calcParam.setTenantId(tenantId);
            BigDecimal premium = parseAmountOrZero(policyDto.getPrem());
            calcParam.setPolicyPremium(premium);
            calcParam.setNetPremium(premium); // 常规保单实交保费等于原价

            // 🌟 强行将佣金参数的创建人和部门，锚定为该业务员！
            // 配合咱们刚才写的 calcCommission 里的 recordBo.setCreateBy(...)，佣金记录就完美隔离了
            calcParam.setCreateById(salesUserId);
            calcParam.setCreateDeptId(salesDeptId);

            // ================= 5. 保存保单 (废弃 switchTo 伪装) =================
            // 回调接口无真实登录态，直接传参进去，在里面显式 set
            Long policyId = createPolicy(policyCallbackDto, product, salesUser, tenantId, salesDeptId);
            calcParam.setPolicyId(policyId);

            // ================= 6. 触发佣金计算事件 =================
            applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));

            return ResultModel.success(calcParam);

        } finally {
            TenantHelper.clearDynamic();
        }
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
        insurancePolicyBo.setPolicyNo(policyCallbackDto.getPolicy().getPolicyNo());
        insurancePolicyBo.setOrderNo(policyCallbackDto.getPolicy().getOrderNo());
        insurancePolicyBo.setAgentName(sysUser.getNickName());
        insurancePolicyBo.setAgentUserId(sysUser.getUserId());
        insurancePolicyBo.setAgentDeptId(sysUser.getDeptId());
        insurancePolicyBo.setPremium(parseAmountOrZero(policyCallbackDto.getPolicy().getPrem()));
        insurancePolicyBo.setAmt(parseAmountOrZero(policyCallbackDto.getPolicy().getAmt()));
        // 未结算
        insurancePolicyBo.setCommissionStatus(1);
        // 已生效
        insurancePolicyBo.setStatus(0);
        insurancePolicyBo.setAppntDate(DateUtil.parse(policyCallbackDto.getPolicy().getAppntDate()));
        insurancePolicyBo.setAccecptDate(DateUtil.parse(policyCallbackDto.getPolicy().getAcceptDate()));
        insurancePolicyBo.setPolicyStartDate(DateUtil.parse(policyCallbackDto.getPolicy().getPolicyStartDate()));
        insurancePolicyBo.setPolicyEndDate(DateUtil.parse(policyCallbackDto.getPolicy().getPolicyEndDate()));
        insurancePolicyBo.setApplicantName(policyCallbackDto.getAppnt().getName());
        insurancePolicyBo.setApplicantSex(policyCallbackDto.getAppnt().getSex());
        insurancePolicyBo.setApplicantIdNo(policyCallbackDto.getAppnt().getIdNo());
        // 固定为身份证
        insurancePolicyBo.setApplicantIdType("0");
        insurancePolicyBo.setApplicantMobile(policyCallbackDto.getAppnt().getMobile());
        // 暂时固定为其他
        insurancePolicyBo.setRelationshipToInsured("4");
        insurancePolicyBo.setInsuredName(policyCallbackDto.getInsureds().get(0).getName());
        insurancePolicyBo.setInsuredSex(policyCallbackDto.getInsureds().get(0).getSex());
        insurancePolicyBo.setInsuredIdNo(policyCallbackDto.getInsureds().get(0).getIdNo());
        // 固定为身份证
        insurancePolicyBo.setInsuredIdType("0");
        insurancePolicyBo.setInsuredMobile(policyCallbackDto.getInsureds().get(0).getMobile());

        insurancePolicyBo.setCreateBy(sysUser.getUserId());
        insurancePolicyBo.setCreateDept(sysUser.getDeptId());
        insurancePolicyBo.setUpdateBy(sysUser.getUserId());
        insurancePolicyBo.setTenantId(tenantId);

        Boolean flag = insurancePolicyService.insertByBo(insurancePolicyBo);
        if (!flag) {
            throw new ServiceException("新增保单失败");
        }

        return insurancePolicyBo.getId();
    }

    private BigDecimal parseAmountOrZero(String amount) {
        return amount == null || amount.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(amount);
    }


}
