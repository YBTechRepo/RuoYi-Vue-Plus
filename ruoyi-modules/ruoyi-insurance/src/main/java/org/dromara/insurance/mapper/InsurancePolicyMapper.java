package org.dromara.insurance.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.vo.InsurancePolicyVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 承保保单Mapper接口
 *
 * @author li.xiang
 * @date 2026-03-11
 */
public interface InsurancePolicyMapper extends BaseMapperPlus<InsurancePolicy, InsurancePolicyVo> {

    /**
     * 平台查询承保保单详情
     *
     * @param id 主键
     * @return 承保保单
     */
    InsurancePolicyVo selectAdminVoById(@Param("id") Long id);

    /**
     * 平台分页查询承保保单
     *
     * @param page 分页参数
     * @param bo   查询条件
     * @return 承保保单分页列表
     */
    Page<InsurancePolicyVo> selectAdminVoPage(Page<InsurancePolicyVo> page, @Param("bo") InsurancePolicyBo bo);

    /**
     * 平台查询承保保单列表
     *
     * @param bo 查询条件
     * @return 承保保单列表
     */
    List<InsurancePolicyVo> selectAdminVoList(@Param("bo") InsurancePolicyBo bo);
}
