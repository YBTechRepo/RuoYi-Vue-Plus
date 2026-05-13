package org.dromara.insurance.service;

import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.vo.InsurancePolicyVo;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 承保保单Service接口
 *
 * @author li.xiang
 * @date 2026-03-09
 */
public interface IInsurancePolicyService {

    /**
     * 查询承保保单
     *
     * @param id 主键
     * @return 承保保单
     */
    InsurancePolicyVo queryById(Long id);

    /**
     * 平台查询承保保单
     *
     * @param id 主键
     * @return 承保保单
     */
    InsurancePolicyVo queryAdminById(Long id);

    /**
     * 分页查询承保保单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 承保保单分页列表
     */
    TableDataInfo<InsurancePolicyVo> queryPageList(InsurancePolicyBo bo, PageQuery pageQuery);

    /**
     * 平台分页查询承保保单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 承保保单分页列表
     */
    TableDataInfo<InsurancePolicyVo> queryAdminPageList(InsurancePolicyBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的承保保单列表
     *
     * @param bo 查询条件
     * @return 承保保单列表
     */
    List<InsurancePolicyVo> queryList(InsurancePolicyBo bo);

    /**
     * 平台查询符合条件的承保保单列表
     *
     * @param bo 查询条件
     * @return 承保保单列表
     */
    List<InsurancePolicyVo> queryAdminList(InsurancePolicyBo bo);

    /**
     * 新增承保保单
     *
     * @param bo 承保保单
     * @return 是否新增成功
     */
    Boolean insertByBo(InsurancePolicyBo bo);

    /**
     * 修改承保保单
     *
     * @param bo 承保保单
     * @return 是否修改成功
     */
    Boolean updateByBo(InsurancePolicyBo bo);

    /**
     * 校验并批量删除承保保单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据保单号和租户ID查询承保保单
     *
     * @param policyNo 保单号
     * @param tenantId 租户ID
     * @return 承保保单
     */
    InsurancePolicy queryByPolicyNoAndTenantId(String policyNo, String tenantId);

}
