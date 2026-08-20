package org.dromara.system.domain.dto;

import lombok.Data;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 租户角色模板配置项
 */
@Data
public class RoleTemplateDto {

    private static final Map<String, String> DEFAULT_DATA_SCOPE = Map.of(
        "leader", "1",
        "teamleader", "4",
        "bizman", "5"
    );

    private static final Set<String> SUPPORTED_DATA_SCOPES = Set.of("1", "3", "4", "5", "6");

    private String roleName;

    private String roleKey;

    private List<Long> menuIds;

    private Integer sort;

    private String dataScope;

    /**
     * 获取有效数据范围。旧模板按已知角色键补齐安全默认值，未知角色必须显式配置。
     */
    public String resolveDataScope() {
        String resolved = dataScope;
        if (StringUtils.isBlank(resolved)) {
            resolved = DEFAULT_DATA_SCOPE.get(roleKey);
        }
        if (StringUtils.isBlank(resolved)) {
            throw new ServiceException("角色模板数据范围不能为空，roleKey=" + roleKey);
        }
        if (!SUPPORTED_DATA_SCOPES.contains(resolved)) {
            throw new ServiceException("角色模板数据范围不支持，roleKey=" + roleKey + "，dataScope=" + resolved);
        }
        return resolved;
    }
}
