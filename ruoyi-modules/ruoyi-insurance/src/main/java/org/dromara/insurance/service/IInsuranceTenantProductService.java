package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceTenantProductVo;
import org.dromara.insurance.domain.bo.InsuranceTenantProductBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 产品库Service接口
 *
 * @author li.xiang
 * @date 2026-03-20
 */
public interface IInsuranceTenantProductService {

    /**
     * 查询产品库
     *
     * @param id 主键
     * @return 产品库
     */
    InsuranceTenantProductVo queryById(Long id);

    /**
     * 分页查询产品库列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品库分页列表
     */
    TableDataInfo<InsuranceTenantProductVo> queryPageList(InsuranceTenantProductBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的产品库列表
     *
     * @param bo 查询条件
     * @return 产品库列表
     */
    List<InsuranceTenantProductVo> queryList(InsuranceTenantProductBo bo);

    /**
     * 新增产品库
     *
     * @param bo 产品库
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceTenantProductBo bo);

    /**
     * 修改产品库
     *
     * @param bo 产品库
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceTenantProductBo bo);

    /**
     * 校验并批量删除产品库信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 批量添加产品
     *
     * @param productIds 产品id
     * @return 是否添加成功
     */
    Boolean batchAddProducts(List<Long> productIds);
}
