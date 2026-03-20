package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 投保记录Service接口
 *
 * @author li.xiang
 * @date 2026-03-13
 */
public interface IInsuranceApplyRecordService {

    /**
     * 查询投保记录
     *
     * @param id 主键
     * @return 投保记录
     */
    InsuranceApplyRecordVo queryById(Long id);

    /**
     * 分页查询投保记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保记录分页列表
     */
    TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的投保记录列表
     *
     * @param bo 查询条件
     * @return 投保记录列表
     */
    List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo);

    /**
     * 新增投保记录
     *
     * @param bo 投保记录
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceApplyRecordBo bo);

    /**
     * 修改投保记录
     *
     * @param bo 投保记录
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceApplyRecordBo bo);

    /**
     * 校验并批量删除投保记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
