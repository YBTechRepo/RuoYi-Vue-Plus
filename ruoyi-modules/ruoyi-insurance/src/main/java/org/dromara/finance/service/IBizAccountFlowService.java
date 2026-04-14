package org.dromara.finance.service;

import org.dromara.finance.domain.vo.BizAccountFlowVo;
import org.dromara.finance.domain.bo.BizAccountFlowBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 账户明细Service接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface IBizAccountFlowService {

    /**
     * 查询账户明细
     *
     * @param id 主键
     * @return 账户明细
     */
    BizAccountFlowVo queryById(Long id);

    /**
     * 分页查询账户明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户明细分页列表
     */
    TableDataInfo<BizAccountFlowVo> queryPageList(BizAccountFlowBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的账户明细列表
     *
     * @param bo 查询条件
     * @return 账户明细列表
     */
    List<BizAccountFlowVo> queryList(BizAccountFlowBo bo);

    /**
     * 新增账户明细
     *
     * @param bo 账户明细
     * @return 是否新增成功
     */
    Boolean insertByBo(BizAccountFlowBo bo);

    /**
     * 修改账户明细
     *
     * @param bo 账户明细
     * @return 是否修改成功
     */
    Boolean updateByBo(BizAccountFlowBo bo);

    /**
     * 校验并批量删除账户明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    TableDataInfo<BizAccountFlowVo> queryAdminPageList(BizAccountFlowBo bo, PageQuery pageQuery);

    List<BizAccountFlowVo> queryUserAccountFlow(BizAccountFlowBo bo);

    TableDataInfo<BizAccountFlowVo> queryUserAccountFlowList(BizAccountFlowBo bo, PageQuery pageQuery);
}
