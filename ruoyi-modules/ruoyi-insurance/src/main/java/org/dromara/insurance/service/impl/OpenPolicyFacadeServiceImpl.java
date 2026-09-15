package org.dromara.insurance.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.InsuranceProductChannelMapping;
import org.dromara.insurance.domain.InsuranceTenantProduct;
import org.dromara.insurance.domain.ResultModel;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.insurance.domain.dto.PolicyDto;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.mapper.InsuranceProductChannelMappingMapper;
import org.dromara.insurance.mapper.InsuranceTenantProductMapper;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.dromara.insurance.service.IOpenPolicyFacadeService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenPolicyFacadeServiceImpl implements IOpenPolicyFacadeService {

    private final IInsurancePolicyService insurancePolicyService;

    private final InsuranceProductConfigMapper insuranceProductConfigMapper;

    private final InsuranceProductChannelMappingMapper productChannelMappingMapper;

    private final InsuranceTenantProductMapper insuranceTenantProductMapper;

    private final InsuranceApplyRecordMapper insuranceApplyRecordMapper;

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
        String policyNo = requireCallbackValue(policyDto.getPolicyNo(), "保单号不能为空");
        String orderNo = requireCallbackValue(policyDto.getOrderNo(), "订单号不能为空");
        BigDecimal premium = parsePremium(policyDto.getPrem());
        policyDto.setPolicyNo(policyNo);
        policyDto.setOrderNo(orderNo);

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

            InsurancePolicy existingPolicy = insurancePolicyService.queryByPolicyNoAndTenantId(policyNo, tenantId);
            if (existingPolicy != null) {
                return ResultModel.success("该保单号对应数据已存在");
            }

            InsuranceApplyRecord applyRecord = insuranceApplyRecordMapper.selectOne(
                new LambdaQueryWrapper<InsuranceApplyRecord>()
                    .eq(InsuranceApplyRecord::getOrderNo, orderNo)
                    .last("LIMIT 1")
            );

            // ================= 2. 查产品详情与防重 =================
            InsuranceProductConfig product = resolveProduct(policyDto, applyRecord);

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
            if (applyRecord != null) {
                Integer commissionStatus = Objects.equals(applyRecord.getCommissionStatus(), 0) ? 0 : 1;
                Long policyId = createPolicy(policyCallbackDto, product, salesUser, tenantId,
                    orderNo, premium, commissionStatus);
                log.info("【承保回调】保单已关联投保订单，不重复计算佣金，policyId={}, orderNo={}", policyId, orderNo);
                return ResultModel.success(policyId);
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
            calcParam.setPolicyPremium(premium);
            calcParam.setNetPremium(premium); // 常规保单实交保费等于原价

            // 🌟 强行将佣金参数的创建人和部门，锚定为该业务员！
            // 配合咱们刚才写的 calcCommission 里的 recordBo.setCreateBy(...)，佣金记录就完美隔离了
            calcParam.setCreateById(salesUserId);
            calcParam.setCreateDeptId(salesDeptId);

            // ================= 5. 保存保单 (废弃 switchTo 伪装) =================
            // 回调接口无真实登录态，直接传参进去，在里面显式 set
            Long policyId = createPolicy(policyCallbackDto, product, salesUser, tenantId,
                orderNo, premium, 1);
            calcParam.setPolicyId(policyId);

            // ================= 6. 触发佣金计算事件 =================
            applicationContext.publishEvent(new PolicyUnderwrittenEvent(calcParam));

            return ResultModel.success(calcParam);

        } finally {
            TenantHelper.clearDynamic();
        }
    }

    InsuranceProductConfig resolveProduct(PolicyDto policyDto, InsuranceApplyRecord applyRecord) {
        return TenantHelper.dynamic("000000", () -> {
            if (applyRecord != null) {
                InsuranceProductConfig applyProduct = insuranceProductConfigMapper.selectById(applyRecord.getProductId());
                if (applyProduct == null) {
                    throw new ServiceException("投保订单对应的平台产品不存在");
                }
                return applyProduct;
            }

            String sourceProductCode = requireCallbackValue(
                policyDto.getProductPlanCode(), "回调产品编码不能为空");
            InsuranceProductConfig directProduct = insuranceProductConfigMapper.selectOne(
                new LambdaQueryWrapper<InsuranceProductConfig>()
                    .eq(InsuranceProductConfig::getProductCode, sourceProductCode)
                    .last("LIMIT 1")
            );
            if (directProduct != null) {
                return directProduct;
            }

            String companyType = requireCallbackValue(policyDto.getCompanyType(), "回调渠道类型不能为空");
            InsuranceProductChannelMapping mapping = productChannelMappingMapper.selectOne(
                new LambdaQueryWrapper<InsuranceProductChannelMapping>()
                    .eq(InsuranceProductChannelMapping::getCompanyType, companyType)
                    .eq(InsuranceProductChannelMapping::getSourceProductCode, sourceProductCode)
                    .last("LIMIT 1")
            );
            if (mapping == null) {
                log.warn("未配置渠道产品映射，companyType={}, productPlanCode={}", companyType, sourceProductCode);
                throw new ServiceException("未配置渠道产品映射：" + companyType + "/" + sourceProductCode);
            }

            InsuranceProductConfig mappedProduct = insuranceProductConfigMapper.selectById(mapping.getProductId());
            if (mappedProduct == null) {
                log.error("渠道产品映射指向不存在的平台产品，mappingId={}, productId={}",
                    mapping.getId(), mapping.getProductId());
                throw new ServiceException("渠道产品映射对应的平台产品不存在");
            }
            return mappedProduct;
        });
    }

    @Override
    public SysUserVo getUserByUserId(Long userId) {
        return sysUserService.selectUserById(userId);
    }

    /**
     * 新增保单
     */
    private Long createPolicy(PolicyCallbackDto policyCallbackDto, InsuranceProductConfig product,
                              SysUserVo sysUser, String tenantId, String orderNo,
                              BigDecimal premium, Integer commissionStatus) {
        InsurancePolicyBo insurancePolicyBo = new InsurancePolicyBo();
        insurancePolicyBo.setProductId(product.getId());
        insurancePolicyBo.setProductCode(policyCallbackDto.getPolicy().getProductPlanCode());
        insurancePolicyBo.setProductName(policyCallbackDto.getPolicy().getProductPlanName());
        insurancePolicyBo.setPolicyNo(policyCallbackDto.getPolicy().getPolicyNo());
        insurancePolicyBo.setOrderNo(orderNo);
        insurancePolicyBo.setSourceCompanyType(policyCallbackDto.getPolicy().getCompanyType());
        insurancePolicyBo.setSourceProductCode(policyCallbackDto.getPolicy().getProductPlanCode());
        insurancePolicyBo.setSourceProductName(policyCallbackDto.getPolicy().getProductPlanName());
        insurancePolicyBo.setAgentName(sysUser.getNickName());
        insurancePolicyBo.setAgentUserId(sysUser.getUserId());
        insurancePolicyBo.setAgentDeptId(sysUser.getDeptId());
        insurancePolicyBo.setPremium(premium);
        insurancePolicyBo.setAmt(parseAmountOrZero(policyCallbackDto.getPolicy().getAmt()));
        // 未结算
        insurancePolicyBo.setCommissionStatus(commissionStatus);
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

    private String requireCallbackValue(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private BigDecimal parsePremium(String amount) {
        String premiumText = requireCallbackValue(amount, "保费不能为空");
        try {
            BigDecimal premium = new BigDecimal(premiumText);
            if (premium.signum() < 0) {
                throw new ServiceException("保费不能小于0");
            }
            return premium;
        } catch (NumberFormatException e) {
            throw new ServiceException("保费格式错误");
        }
    }

    private BigDecimal parseAmountOrZero(String amount) {
        return amount == null || amount.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(amount);
    }


}
