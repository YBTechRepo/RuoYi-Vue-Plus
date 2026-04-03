package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceOrderInsuredVo;
import org.dromara.insurance.domain.bo.InsuranceOrderInsuredBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 被保人明细Service接口
 *
 * @author li.xiang
 * @date 2026-03-30
 */
public interface IInsuranceOrderInsuredService {

    /**
     * 查询被保人明细
     *
     * @param id 主键
     * @return 被保人明细
     */
    InsuranceOrderInsuredVo queryById(Long id);

    /**
     * 分页查询被保人明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 被保人明细分页列表
     */
    TableDataInfo<InsuranceOrderInsuredVo> queryPageList(InsuranceOrderInsuredBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的被保人明细列表
     *
     * @param bo 查询条件
     * @return 被保人明细列表
     */
    List<InsuranceOrderInsuredVo> queryList(InsuranceOrderInsuredBo bo);

    /**
     * 新增被保人明细
     *
     * @param bo 被保人明细
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceOrderInsuredBo bo);

    /**
     * 修改被保人明细
     *
     * @param bo 被保人明细
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceOrderInsuredBo bo);

    /**
     * 校验并批量删除被保人明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
