package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceProductVo;
import org.dromara.insurance.domain.bo.InsuranceProductBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 产品配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-02
 */
public interface IInsuranceProductService {

    /**
     * 查询产品配置
     *
     * @param id 主键
     * @return 产品配置
     */
    InsuranceProductVo queryById(Long id);

    /**
     * 分页查询产品配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品配置分页列表
     */
    TableDataInfo<InsuranceProductVo> queryPageList(InsuranceProductBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的产品配置列表
     *
     * @param bo 查询条件
     * @return 产品配置列表
     */
    List<InsuranceProductVo> queryList(InsuranceProductBo bo);

    /**
     * 新增产品配置
     *
     * @param bo 产品配置
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductBo bo);

    /**
     * 修改产品配置
     *
     * @param bo 产品配置
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductBo bo);

    /**
     * 校验并批量删除产品配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
