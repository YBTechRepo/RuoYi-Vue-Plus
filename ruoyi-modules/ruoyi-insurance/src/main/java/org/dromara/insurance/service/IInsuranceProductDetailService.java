package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceProductDetailVo;
import org.dromara.insurance.domain.bo.InsuranceProductDetailBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 保险产品-图文详情Service接口
 *
 * @author li.xiang
 * @date 2026-03-23
 */
public interface IInsuranceProductDetailService {

    /**
     * 查询保险产品-图文详情
     *
     * @param id 主键
     * @return 保险产品-图文详情
     */
    InsuranceProductDetailVo queryById(Long id);

    /**
     * 分页查询保险产品-图文详情列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 保险产品-图文详情分页列表
     */
    TableDataInfo<InsuranceProductDetailVo> queryPageList(InsuranceProductDetailBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的保险产品-图文详情列表
     *
     * @param bo 查询条件
     * @return 保险产品-图文详情列表
     */
    List<InsuranceProductDetailVo> queryList(InsuranceProductDetailBo bo);

    /**
     * 新增保险产品-图文详情
     *
     * @param bo 保险产品-图文详情
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductDetailBo bo);

    /**
     * 修改保险产品-图文详情
     *
     * @param bo 保险产品-图文详情
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductDetailBo bo);

    /**
     * 校验并批量删除保险产品-图文详情信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
