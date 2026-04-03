package org.dromara.finance.service;

import org.dromara.finance.domain.RechargeApplyReqDTO;
import org.dromara.finance.domain.RechargeAuditReqDTO;
import org.dromara.finance.domain.vo.BizRechargeRecordVo;
import org.dromara.finance.domain.bo.BizRechargeRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 充值申请Service接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface IBizRechargeRecordService {

    /**
     * 查询充值申请
     *
     * @param id 主键
     * @return 充值申请
     */
    BizRechargeRecordVo queryById(Long id);

    /**
     * 分页查询充值申请列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 充值申请分页列表
     */
    TableDataInfo<BizRechargeRecordVo> queryPageList(BizRechargeRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的充值申请列表
     *
     * @param bo 查询条件
     * @return 充值申请列表
     */
    List<BizRechargeRecordVo> queryList(BizRechargeRecordBo bo);

    /**
     * 新增充值申请
     *
     * @param bo 充值申请
     * @return 是否新增成功
     */
    Boolean insertByBo(BizRechargeRecordBo bo);

    /**
     * 修改充值申请
     *
     * @param bo 充值申请
     * @return 是否修改成功
     */
    Boolean updateByBo(BizRechargeRecordBo bo);

    /**
     * 校验并批量删除充值申请信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 发起充值申请
     * @param bo 充值申请业务对象
     * @return 结果
     */
    Boolean applyRecharge(RechargeApplyReqDTO rechargeApplyReqDTO);

    /**
     * 管理员查询充值申请列表
     * @param bo
     * @param pageQuery
     * @return
     */
    TableDataInfo<BizRechargeRecordVo> queryAdminPageList(BizRechargeRecordBo bo, PageQuery pageQuery);

    /**
     * 管理员查询充值申请详情
     * @param id
     * @return
     */
    BizRechargeRecordVo queryAdminById(Long id);

    /**
     * 充值审批
     * @param reqDTO
     */
    Boolean auditRecharge(RechargeAuditReqDTO reqDTO);
}
