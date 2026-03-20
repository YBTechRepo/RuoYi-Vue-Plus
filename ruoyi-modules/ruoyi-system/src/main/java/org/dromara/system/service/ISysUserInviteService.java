package org.dromara.system.service;

import org.dromara.system.domain.vo.SysUserInviteVo;
import org.dromara.system.domain.bo.SysUserInviteBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 人员邀请登记Service接口
 *
 * @author li.xiang
 * @date 2026-03-09
 */
public interface ISysUserInviteService {

    /**
     * 查询人员邀请登记
     *
     * @param id 主键
     * @return 人员邀请登记
     */
    SysUserInviteVo queryById(Long id);

    /**
     * 分页查询人员邀请登记列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 人员邀请登记分页列表
     */
    TableDataInfo<SysUserInviteVo> queryPageList(SysUserInviteBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的人员邀请登记列表
     *
     * @param bo 查询条件
     * @return 人员邀请登记列表
     */
    List<SysUserInviteVo> queryList(SysUserInviteBo bo);

    /**
     * 新增人员邀请登记
     *
     * @param bo 人员邀请登记
     * @return 是否新增成功
     */
    Boolean insertByBo(SysUserInviteBo bo);

    /**
     * 修改人员邀请登记
     *
     * @param bo 人员邀请登记
     * @return 是否修改成功
     */
    Boolean updateByBo(SysUserInviteBo bo);

    /**
     * 校验并批量删除人员邀请登记信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    Boolean updateAndInsertUserByInviteId(SysUserInviteBo bo);
}
