package org.dromara.system.service.impl;

import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysRoleTemplate;
import org.dromara.system.domain.SysTenant;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysRoleMenuMapper;
import org.dromara.system.mapper.SysRoleTemplateMapper;
import org.dromara.system.mapper.SysTenantMapper;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class SysTenantPackageServiceImplTest {

    @BeforeAll
    static void initializeJsonContext() {
        TestJsonContext.initialize();
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("数据范围同步成功提交后才精准下线关联用户")
    void cleanChangedRoleUsersOnlyAfterCommit() {
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        ISysRoleService roleService = mock(ISysRoleService.class);
        SysTenantPackageServiceImpl service = new SysTenantPackageServiceImpl(
            mock(SysTenantPackageMapper.class),
            mock(SysTenantMapper.class),
            mock(SysRoleTemplateMapper.class),
            mock(SysRoleMapper.class),
            mock(SysRoleMenuMapper.class),
            userRoleMapper,
            roleService
        );
        when(userRoleMapper.selectUserIdsByRoleIds(Set.of(10L, 20L))).thenReturn(List.of(100L, 200L));

        TransactionSynchronizationManager.initSynchronization();
        service.cleanChangedRoleUsersAfterCommit(Set.of(10L, 20L));

        verify(roleService, never()).cleanOnlineUser(List.of(100L, 200L));
        TransactionSynchronizationManager.getSynchronizations().forEach(synchronization -> synchronization.afterCommit());
        verify(roleService).cleanOnlineUser(List.of(100L, 200L));
    }

    @Test
    @DisplayName("事务回滚时不处理在线会话")
    void doNotCleanOnlineUsersAfterRollback() {
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        ISysRoleService roleService = mock(ISysRoleService.class);
        SysTenantPackageServiceImpl service = createService(
            mock(SysTenantPackageMapper.class), mock(SysTenantMapper.class),
            mock(SysRoleTemplateMapper.class), mock(SysRoleMapper.class),
            mock(SysRoleMenuMapper.class), userRoleMapper, roleService
        );
        when(userRoleMapper.selectUserIdsByRoleIds(Set.of(10L))).thenReturn(List.of(100L));

        TransactionSynchronizationManager.initSynchronization();
        service.cleanChangedRoleUsersAfterCommit(Set.of(10L));
        TransactionSynchronizationManager.getSynchronizations()
            .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(roleService, never()).cleanOnlineUser(any());
    }

    @Test
    @DisplayName("套餐同步只更新数据范围发生变化的角色")
    void syncOnlyChangedRoleDataScopes() {
        SysTenantPackageMapper packageMapper = mock(SysTenantPackageMapper.class);
        SysTenantMapper tenantMapper = mock(SysTenantMapper.class);
        SysRoleTemplateMapper templateMapper = mock(SysRoleTemplateMapper.class);
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        SysTenantPackageServiceImpl service = createService(
            packageMapper, tenantMapper, templateMapper, roleMapper,
            roleMenuMapper, userRoleMapper, mock(ISysRoleService.class)
        );

        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1L);
        tenantPackage.setMenuIds("");
        SysTenant tenant = new SysTenant();
        tenant.setTenantId("123456");
        tenant.setRoleTemplateId(2L);
        SysRoleTemplate template = new SysRoleTemplate();
        template.setId(2L);
        template.setRolesJson("[{\"roleKey\":\"leader\"},{\"roleKey\":\"teamleader\"},{\"roleKey\":\"bizman\"}]");

        when(packageMapper.selectById(1L)).thenReturn(tenantPackage);
        when(tenantMapper.selectList(any())).thenReturn(List.of(tenant));
        when(templateMapper.selectList(any())).thenReturn(List.of(template));
        when(roleMapper.selectList(any())).thenReturn(List.of(
            role(10L, "leader", "1"),
            role(20L, "teamleader", "1"),
            role(30L, "bizman", "1")
        ));
        when(userRoleMapper.selectUserIdsByRoleIds(Set.of(20L, 30L))).thenReturn(List.of());

        service.syncPackageRoles(1L);

        var captor = org.mockito.ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper, times(2)).updateById(captor.capture());
        assertEquals(Set.of("20:4", "30:5"), captor.getAllValues().stream()
            .map(value -> value.getRoleId() + ":" + value.getDataScope())
            .collect(java.util.stream.Collectors.toSet()));
        verify(roleMapper, atLeastOnce()).selectList(any());
        verify(packageMapper, never()).updateById(any(SysTenantPackage.class));
    }

    private static SysTenantPackageServiceImpl createService(
        SysTenantPackageMapper packageMapper,
        SysTenantMapper tenantMapper,
        SysRoleTemplateMapper templateMapper,
        SysRoleMapper roleMapper,
        SysRoleMenuMapper roleMenuMapper,
        SysUserRoleMapper userRoleMapper,
        ISysRoleService roleService
    ) {
        return new SysTenantPackageServiceImpl(
            packageMapper, tenantMapper, templateMapper, roleMapper,
            roleMenuMapper, userRoleMapper, roleService
        );
    }

    private static SysRole role(Long roleId, String roleKey, String dataScope) {
        SysRole role = new SysRole(roleId);
        role.setRoleKey(roleKey);
        role.setDataScope(dataScope);
        return role;
    }
}
