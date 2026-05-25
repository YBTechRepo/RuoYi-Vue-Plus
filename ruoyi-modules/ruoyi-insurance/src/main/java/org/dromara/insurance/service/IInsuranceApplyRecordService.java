package org.dromara.insurance.service;

import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.dto.VoucherPdfResult;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * 导出投保记录列表，单产品导出时追加投保扩展字段列。
     *
     * @param bo       查询条件
     * @param response 响应体
     */
    void exportList(InsuranceApplyRecordBo bo, HttpServletResponse response);

    /**
     * 查询批次子单列表 (租户隔离)
     *
     * @param batchOrderNo 批次主单号
     * @return 子单列表
     */
    List<Map<String, Object>> querySubOrders(String batchOrderNo);

    /**
     * 个人详情 (投被保人信息, 租户隔离)
     *
     * @param orderNo 订单号
     * @return 个人详情
     */
    Map<String, Object> queryPersonDetail(String orderNo);

    /**
     * 生成投保凭证 PDF
     *
     * @param orderNo 订单号
     * @return PDF 文件名和内容
     */
    VoucherPdfResult generateVoucherPdf(String orderNo);

    /**
     * 查询批量投保的子单列表
     *
     * @param orderNo   主单订单号
     * @param pageQuery 分页参数
     * @return 投保子单分页列表
     */
    TableDataInfo<InsuranceApplyRecordVo> querySubPageList(String orderNo, PageQuery pageQuery);

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

    Boolean handleOrderPaySuccess(Long orderId);

    SaveInsureResultVO saveInsureInfo(String orderNo, OrderInsureInfoDTO infoDTO);

    Boolean payWithBalance(PayWithBalanceReqDTO payWithBalanceReqDTO);

    /**
     * 批量投保：生成主单 + 子单 + 投被保人记录
     */
    String submitBatch(BatchSubmitDTO submitDTO);

    /**
     * 移动端取消订单
     */
    Boolean cancelOrder(InsuranceApplyRecordBo bo);
}
