package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 代投保订单查询Service接口
 *
 * @author li.xiang
 * @date 2026-04-14
 */
public interface IInsuranceProxyOrderService {

    /**
     * 查询代投保订单详情 (仅限 insureMode=1, 跨租户)
     *
     * @param id 主键
     * @return 代投保订单查询
     */
    InsuranceApplyRecordVo queryById(Long id);

    /**
     * 分页查询代投保订单列表 (仅限 insureMode=1, 跨租户)
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 代投保订单查询分页列表
     */
    TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的代投保订单列表 (仅限 insureMode=1, 跨租户)
     *
     * @param bo 查询条件
     * @return 代投保订单查询列表
     */
    List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo);

    /**
     * 查询用于导出的代投保订单列表 (过滤 isBatch 为 0 和 2，且 status 为 0，包含投被保人信息)
     *
     * @param bo 查询条件
     * @return 导出用代投保订单列表
     */
    List<InsuranceApplyRecordVo> exportList(InsuranceApplyRecordBo bo);

    /**
     * 查询批次子单列表
     *
     * @param batchOrderNo 批次主单号
     * @return 子单列表
     */
    List<Map<String, Object>> querySubOrders(String batchOrderNo);

    /**
     * 个人详情 (投被保人信息)
     *
     * @param orderNo 订单号
     * @return 个人详情
     */
    Map<String, Object> queryPersonDetail(String orderNo);

    /**
     * 新增代投保订单查询
     *
     * @param bo 代投保订单查询
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceApplyRecordBo bo);

    /**
     * 修改代投保订单查询 (仅限 insureMode=1, 跨租户)
     *
     * @param bo 代投保订单查询
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceApplyRecordBo bo);

    /**
     * 校验并批量删除代投保订单查询信息 (仅限 insureMode=1, 跨租户)
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
