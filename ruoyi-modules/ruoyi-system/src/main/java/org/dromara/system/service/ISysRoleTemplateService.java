package org.dromara.system.service;

import org.dromara.system.domain.vo.SysRoleTemplateVo;
import org.dromara.system.domain.bo.SysRoleTemplateBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 租户角色管理Service接口
 *
 * @author li.xiang
 * @date 2026-04-22
 */
public interface ISysRoleTemplateService {

    /**
     * 查询租户角色管理
     *
     * @param id 主键
     * @return 租户角色管理
     */
    SysRoleTemplateVo queryById(Long id);

    /**
     * 分页查询租户角色管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 租户角色管理分页列表
     */
    TableDataInfo<SysRoleTemplateVo> queryPageList(SysRoleTemplateBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的租户角色管理列表
     *
     * @param bo 查询条件
     * @return 租户角色管理列表
     */
    List<SysRoleTemplateVo> queryList(SysRoleTemplateBo bo);

    /**
     * 新增租户角色管理
     *
     * @param bo 租户角色管理
     * @return 是否新增成功
     */
    Boolean insertByBo(SysRoleTemplateBo bo);

    /**
     * 修改租户角色管理
     *
     * @param bo 租户角色管理
     * @return 是否修改成功
     */
    Boolean updateByBo(SysRoleTemplateBo bo);

    /**
     * 校验并批量删除租户角色管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
