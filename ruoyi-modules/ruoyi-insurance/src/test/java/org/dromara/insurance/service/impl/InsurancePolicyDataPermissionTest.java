package org.dromara.insurance.service.impl;

import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("dev")
class InsurancePolicyDataPermissionTest {

    static Stream<Method> businessOwnershipPermissionMethods() {
        return Stream.of(
            Arrays.stream(InsurancePolicyServiceImpl.class.getDeclaredMethods())
                .filter(method -> SetHolder.POLICY_METHODS.contains(method.getName())),
            Arrays.stream(InsuranceApplyRecordServiceImpl.class.getDeclaredMethods())
                .filter(method -> SetHolder.APPLY_RECORD_METHODS.contains(method.getName())),
            Arrays.stream(InsuranceCardOrderServiceImpl.class.getDeclaredMethods())
                .filter(method -> SetHolder.CARD_ORDER_METHODS.contains(method.getName()))
        ).flatMap(stream -> stream);
    }

    @ParameterizedTest
    @MethodSource("businessOwnershipPermissionMethods")
    @DisplayName("普通保单和订单入口按代理用户及代理部门隔离")
    void useAgentOwnershipColumns(Method method) {
        DataPermission permission = method.getAnnotation(DataPermission.class);

        assertNotNull(permission);
        DataColumn[] columns = permission.value();
        assertArrayEquals(new String[]{"agent_dept_id"}, columns[0].value());
        assertArrayEquals(new String[]{"agent_user_id"}, columns[1].value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"queryAdminById", "queryAdminPageList", "queryAdminList"})
    @DisplayName("卡密订单平台管理入口不应用普通角色数据权限")
    void keepCardOrderAdminMethodsUnchanged(String methodName) {
        Method method = Arrays.stream(InsuranceCardOrderServiceImpl.class.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(methodName))
            .findFirst()
            .orElseThrow();

        assertNull(method.getAnnotation(DataPermission.class));
    }

    private static final class SetHolder {
        private static final java.util.Set<String> POLICY_METHODS = java.util.Set.of(
            "queryById", "queryPageList", "queryList"
        );
        private static final java.util.Set<String> APPLY_RECORD_METHODS = java.util.Set.of(
            "queryById", "queryPageList", "queryList", "exportList", "generateVoucherPdfByPolicyNo"
        );
        private static final java.util.Set<String> CARD_ORDER_METHODS = java.util.Set.of(
            "queryById", "queryPageList", "queryList"
        );
    }
}
