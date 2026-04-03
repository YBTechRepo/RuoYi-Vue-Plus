package org.dromara.finance.service;

import org.dromara.finance.domain.AccountAdjustReqDTO;
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.finance.domain.bo.BizUserAccountBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 账户信息Service接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface IBizUserAccountService {

    /**
     * 查询账户信息
     *
     * @param userId 主键
     * @return 账户信息
     */
    BizUserAccountVo queryById(Long userId);

    /**
     * 分页查询账户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户信息分页列表
     */
    TableDataInfo<BizUserAccountVo> queryPageList(BizUserAccountBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的账户信息列表
     *
     * @param bo 查询条件
     * @return 账户信息列表
     */
    List<BizUserAccountVo> queryList(BizUserAccountBo bo);

    /**
     * 新增账户信息
     *
     * @param bo 账户信息
     * @return 是否新增成功
     */
    Boolean insertByBo(BizUserAccountBo bo);

    /**
     * 修改账户信息
     *
     * @param bo 账户信息
     * @return 是否修改成功
     */
    Boolean updateByBo(BizUserAccountBo bo);

    /**
     * 校验并批量删除账户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    TableDataInfo<BizUserAccountVo> queryAdminPageList(BizUserAccountBo bo, PageQuery pageQuery);

    List<BizUserAccountVo> queryAdminList(BizUserAccountBo bo);

    void adjustBalance(AccountAdjustReqDTO reqDTO);

    void deductForOrder(Long userId, String orderNo, BigDecimal amount, String remark);
}
