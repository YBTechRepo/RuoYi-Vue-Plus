package org.dromara.commission.service;

import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.domain.vo.BizCommissionDeptVo;
import org.dromara.commission.domain.bo.BizCommissionDeptBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 机构费率配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-09
 */
public interface IBizCommissionDeptService {

    /**
     * 查询机构费率配置
     *
     * @param id 主键
     * @return 机构费率配置
     */
    BizCommissionDeptVo queryById(Long id);

    /**
     * 分页查询机构费率配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 机构费率配置分页列表
     */
    TableDataInfo<BizCommissionDeptVo> queryPageList(BizCommissionDeptBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的机构费率配置列表
     *
     * @param bo 查询条件
     * @return 机构费率配置列表
     */
    List<BizCommissionDeptVo> queryList(BizCommissionDeptBo bo);

    /**
     * 新增机构费率配置
     *
     * @param bo 机构费率配置
     * @return 是否新增成功
     */
    Boolean insertByBo(BizCommissionDeptBo bo);

    /**
     * 修改机构费率配置
     *
     * @param bo 机构费率配置
     * @return 是否修改成功
     */
    Boolean updateByBo(BizCommissionDeptBo bo);

    /**
     * 校验并批量删除机构费率配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询机构费率配置
     *
     * @param deptId 部门ID
     * @param tenantId  租户ID
     * @return 机构费率配置
     */
    BizCommissionDept queryByDeptIdAndTenantId(Long deptId, String tenantId);

    /**
     * 根据部门ID查询机构费率配置
     *
     * @param deptId 部门ID
     * @return 机构费率配置
     */
    BizCommissionDept queryByDeptId(Long deptId);
}
