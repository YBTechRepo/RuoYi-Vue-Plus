package org.dromara.commission.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppCommissionQueryBo {
    /**
     * 角色类型: 1-业务直推, 2-团队管理, 3-项目分红 (必填)
     */
    @NotNull(message = "角色类型不能为空")
    private Integer roleType;

    /**
     * 查询月份 (选填，格式：yyyy-MM)
     */
    private String queryMonth;
}
