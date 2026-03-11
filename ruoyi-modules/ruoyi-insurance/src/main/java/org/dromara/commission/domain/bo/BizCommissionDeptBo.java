package org.dromara.commission.domain.bo;

import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 机构费率配置业务对象 biz_commission_dept
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizCommissionDept.class, reverseConvertGenerate = false)
public class BizCommissionDeptBo extends BaseEntity {

    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long deptId;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deptName;

    /**
     * 项目负责人比例
     */
    @NotNull(message = "项目负责人比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal projectRatio;

    /**
     * 团队负责人比例
     */
    @NotNull(message = "团队负责人比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal teamRatio;

    /**
     * 业务员比例
     */
    @NotNull(message = "业务员比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salesRatio;

    /**
     * 生效开始时间
     */
    @NotNull(message = "生效开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date effectiveStart;

    /**
     * 生效结束时间
     */
    @NotNull(message = "生效结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date effectiveEnd;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 删除标志
     */
    private String delFlag;


}
