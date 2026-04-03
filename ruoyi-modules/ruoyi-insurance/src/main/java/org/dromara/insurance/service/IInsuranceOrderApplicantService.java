package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceOrderApplicantVo;
import org.dromara.insurance.domain.bo.InsuranceOrderApplicantBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 投保人信息Service接口
 *
 * @author li.xiang
 * @date 2026-03-30
 */
public interface IInsuranceOrderApplicantService {

    /**
     * 查询投保人信息
     *
     * @param id 主键
     * @return 投保人信息
     */
    InsuranceOrderApplicantVo queryById(Long id);

    /**
     * 分页查询投保人信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保人信息分页列表
     */
    TableDataInfo<InsuranceOrderApplicantVo> queryPageList(InsuranceOrderApplicantBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的投保人信息列表
     *
     * @param bo 查询条件
     * @return 投保人信息列表
     */
    List<InsuranceOrderApplicantVo> queryList(InsuranceOrderApplicantBo bo);

    /**
     * 新增投保人信息
     *
     * @param bo 投保人信息
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceOrderApplicantBo bo);

    /**
     * 修改投保人信息
     *
     * @param bo 投保人信息
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceOrderApplicantBo bo);

    /**
     * 校验并批量删除投保人信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
