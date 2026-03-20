package org.dromara.insurance.service;

import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.vo.InsuranceProductConfigVo;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.domain.vo.MarketProductVo;

import java.util.Collection;
import java.util.List;

/**
 * 产品配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-06
 */
public interface IInsuranceProductConfigService {

    /**
     * 查询产品配置
     *
     * @param id 主键
     * @return 产品配置
     */
    InsuranceProductConfigVo queryById(Long id);

    /**
     * 分页查询产品配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品配置分页列表
     */
    TableDataInfo<InsuranceProductConfigVo> queryPageList(InsuranceProductConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的产品配置列表
     *
     * @param bo 查询条件
     * @return 产品配置列表
     */
    List<InsuranceProductConfigVo> queryList(InsuranceProductConfigBo bo);

    /**
     * 新增产品配置
     *
     * @param bo 产品配置
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductConfigBo bo);

    /**
     * 修改产品配置
     *
     * @param bo 产品配置
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductConfigBo bo);

    /**
     * 校验并批量删除产品配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据产品编码和租户id查询产品配置
     * @param productCode 产品编码
     * @param tenantId    租户id
     * @return 产品配置详情
     */
    InsuranceProductConfig queryByProductCodeAndTenantId(String productCode, String tenantId);

    /**
     * 销售端专属：分页查询产品列表（聚合了动态佣金费率）
     */
    TableDataInfo<InsuranceSalesProductVo> querySalesPageList(InsuranceProductConfigBo bo, PageQuery pageQuery);

    /**
     * 查询授权产品
     */
    TableDataInfo<MarketProductVo> queryMarketPageList(InsuranceProductConfigBo bo, PageQuery pageQuery);
}
