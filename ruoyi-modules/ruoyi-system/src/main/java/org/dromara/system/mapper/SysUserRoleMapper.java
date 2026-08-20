package org.dromara.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysUserRole;

import java.util.Collection;
import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author Lion Li
 */
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRole, SysUserRole> {

    /**
     * 根据角色ID查询关联的用户ID列表
     *
     * @param roleId 角色ID
     * @return 关联到指定角色的用户ID列表
     */
    default List<Long> selectUserIdsByRoleId(Long roleId) {
        return this.selectObjs(new LambdaQueryWrapper<SysUserRole>()
            .select(SysUserRole::getUserId).eq(SysUserRole::getRoleId, roleId)
        );
    }

    /**
     * 根据角色ID集合查询关联的用户ID列表
     *
     * @param roleIds 角色ID集合
     * @return 去重后的用户ID列表
     */
    default List<Long> selectUserIdsByRoleIds(Collection<Long> roleIds) {
        return this.<Long>selectObjs(new LambdaQueryWrapper<SysUserRole>()
            .select(SysUserRole::getUserId).in(SysUserRole::getRoleId, roleIds)
        ).stream().distinct().toList();
    }

}
