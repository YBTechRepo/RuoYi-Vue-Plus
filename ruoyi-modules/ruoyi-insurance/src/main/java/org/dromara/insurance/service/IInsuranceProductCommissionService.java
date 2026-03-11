package org.dromara.insurance.service;

import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.insurance.domain.vo.InsuranceProductCommissionVo;
import org.dromara.insurance.domain.bo.InsuranceProductCommissionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 佣金配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-06
 */
public interface IInsuranceProductCommissionService {

    /**
     * 查询佣金配置
     *
     * @param id 主键
     * @return 佣金配置
     */
    InsuranceProductCommissionVo queryById(Long id);

    /**
     * 分页查询佣金配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 佣金配置分页列表
     */
    TableDataInfo<InsuranceProductCommissionVo> queryPageList(InsuranceProductCommissionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的佣金配置列表
     *
     * @param bo 查询条件
     * @return 佣金配置列表
     */
    List<InsuranceProductCommissionVo> queryList(InsuranceProductCommissionBo bo);

    /**
     * 新增佣金配置
     *
     * @param bo 佣金配置
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductCommissionBo bo);

    /**
     * 修改佣金配置
     *
     * @param bo 佣金配置
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductCommissionBo bo);

    /**
     * 校验并批量删除佣金配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据产品ID和租户ID查询佣金配置
     *
     * @param productId 产品ID
     * @param tenantId  租户ID
     * @return 佣金配置
     */
    InsuranceProductCommission queryByProductIdAndTenantId(Long productId, String tenantId);
}
