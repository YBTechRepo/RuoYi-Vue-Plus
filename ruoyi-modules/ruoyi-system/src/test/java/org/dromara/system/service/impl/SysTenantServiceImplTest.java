package org.dromara.system.service.impl;

import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysRoleTemplate;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysConfigMapper;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysDictDataMapper;
import org.dromara.system.mapper.SysDictTypeMapper;
import org.dromara.system.mapper.SysRoleDeptMapper;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysRoleMenuMapper;
import org.dromara.system.mapper.SysRoleTemplateMapper;
import org.dromara.system.mapper.SysTenantMapper;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class SysTenantServiceImplTest {

    @BeforeAll
    static void initializeJsonContext() {
        TestJsonContext.initialize();
    }

    @Test
    @DisplayName("新租户管理员及模板角色显式初始化安全数据范围")
    void initializeRoleDataScopesFromLegacyTemplate() {
        SysTenantPackageMapper packageMapper = mock(SysTenantPackageMapper.class);
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleTemplateMapper templateMapper = mock(SysRoleTemplateMapper.class);
        SysTenantServiceImpl service = new SysTenantServiceImpl(
            mock(SysTenantMapper.class), packageMapper, mock(SysUserMapper.class),
            mock(SysDeptMapper.class), roleMapper, mock(SysRoleMenuMapper.class),
            mock(SysRoleDeptMapper.class), mock(SysUserRoleMapper.class),
            mock(SysDictTypeMapper.class), mock(SysDictDataMapper.class),
            mock(SysConfigMapper.class), templateMapper
        );

        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setMenuIds("");
        SysRoleTemplate template = new SysRoleTemplate();
        template.setRolesJson("["
            + "{\"roleName\":\"总代理\",\"roleKey\":\"leader\",\"sort\":1},"
            + "{\"roleName\":\"团队长\",\"roleKey\":\"teamleader\",\"sort\":2},"
            + "{\"roleName\":\"业务员\",\"roleKey\":\"bizman\",\"sort\":3}]");
        when(packageMapper.selectById(1L)).thenReturn(tenantPackage);
        when(templateMapper.selectById(2L)).thenReturn(template);

        ReflectionTestUtils.invokeMethod(service, "createTenantRolesWithTemplate", "123456", 1L, 2L);

        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper, times(4)).insert(captor.capture());
        Map<String, String> scopes = captor.getAllValues().stream()
            .collect(Collectors.toMap(SysRole::getRoleKey, SysRole::getDataScope));
        assertEquals(Map.of(
            "admin", "1",
            "leader", "1",
            "teamleader", "4",
            "bizman", "5"
        ), scopes);
    }
}
