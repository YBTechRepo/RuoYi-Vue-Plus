package org.dromara.system.domain.dto;

import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class RoleTemplateDtoTest {

    @ParameterizedTest
    @CsvSource({
        "leader,1",
        "teamleader,4",
        "bizman,5"
    })
    @DisplayName("旧模板按已知角色键补齐安全数据范围")
    void resolveLegacyDataScope(String roleKey, String expected) {
        RoleTemplateDto role = new RoleTemplateDto();
        role.setRoleKey(roleKey);

        assertEquals(expected, role.resolveDataScope());
    }

    @Test
    @DisplayName("显式配置的有效数据范围原样保留")
    void keepExplicitSupportedDataScope() {
        RoleTemplateDto role = new RoleTemplateDto();
        role.setRoleKey("customRole");
        role.setDataScope("3");

        assertEquals("3", role.resolveDataScope());
    }

    @Test
    @DisplayName("未知角色缺少数据范围时拒绝创建")
    void rejectUnknownRoleWithoutDataScope() {
        RoleTemplateDto role = new RoleTemplateDto();
        role.setRoleKey("customRole");

        assertThrows(ServiceException.class, role::resolveDataScope);
    }

    @Test
    @DisplayName("角色模板不支持缺少部门集合的自定数据权限")
    void rejectUnsupportedCustomDataScope() {
        RoleTemplateDto role = new RoleTemplateDto();
        role.setRoleKey("customRole");
        role.setDataScope("2");

        assertThrows(ServiceException.class, role::resolveDataScope);
    }
}
