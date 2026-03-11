package org.dromara.commission.service;

import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.domain.bo.BizCommissionRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 佣金分配明细Service接口
 *
 * @author li.xiang
 * @date 2026-03-11
 */
public interface IBizCommissionRecordService {

    /**
     * 查询佣金分配明细
     *
     * @param id 主键
     * @return 佣金分配明细
     */
    BizCommissionRecordVo queryById(Long id);

    /**
     * 分页查询佣金分配明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 佣金分配明细分页列表
     */
    TableDataInfo<BizCommissionRecordVo> queryPageList(BizCommissionRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的佣金分配明细列表
     *
     * @param bo 查询条件
     * @return 佣金分配明细列表
     */
    List<BizCommissionRecordVo> queryList(BizCommissionRecordBo bo);

    /**
     * 新增佣金分配明细
     *
     * @param bo 佣金分配明细
     * @return 是否新增成功
     */
    Boolean insertByBo(BizCommissionRecordBo bo);

    /**
     * 修改佣金分配明细
     *
     * @param bo 佣金分配明细
     * @return 是否修改成功
     */
    Boolean updateByBo(BizCommissionRecordBo bo);

    /**
     * 校验并批量删除佣金分配明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 佣金分配
     *
     * @param calcCommission
     */
    void calcCommission(CalcCommission calcCommission);
}
