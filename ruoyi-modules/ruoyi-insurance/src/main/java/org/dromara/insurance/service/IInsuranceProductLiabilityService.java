package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceProductLiabilityVo;
import org.dromara.insurance.domain.bo.InsuranceProductLiabilityBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 保险产品-保障责任Service接口
 *
 * @author li.xiang
 * @date 2026-03-23
 */
public interface IInsuranceProductLiabilityService {

    /**
     * 查询保险产品-保障责任
     *
     * @param id 主键
     * @return 保险产品-保障责任
     */
    InsuranceProductLiabilityVo queryById(Long id);

    /**
     * 分页查询保险产品-保障责任列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 保险产品-保障责任分页列表
     */
    TableDataInfo<InsuranceProductLiabilityVo> queryPageList(InsuranceProductLiabilityBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的保险产品-保障责任列表
     *
     * @param bo 查询条件
     * @return 保险产品-保障责任列表
     */
    List<InsuranceProductLiabilityVo> queryList(InsuranceProductLiabilityBo bo);

    /**
     * 新增保险产品-保障责任
     *
     * @param bo 保险产品-保障责任
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductLiabilityBo bo);

    /**
     * 修改保险产品-保障责任
     *
     * @param bo 保险产品-保障责任
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductLiabilityBo bo);

    /**
     * 校验并批量删除保险产品-保障责任信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
