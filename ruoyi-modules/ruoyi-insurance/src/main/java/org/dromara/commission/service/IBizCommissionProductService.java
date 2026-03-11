package org.dromara.commission.service;

import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.commission.domain.vo.BizCommissionProductVo;
import org.dromara.commission.domain.bo.BizCommissionProductBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 特殊产品费率配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-09
 */
public interface IBizCommissionProductService {

    /**
     * 查询特殊产品费率配置
     *
     * @param id 主键
     * @return 特殊产品费率配置
     */
    BizCommissionProductVo queryById(Long id);

    /**
     * 分页查询特殊产品费率配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 特殊产品费率配置分页列表
     */
    TableDataInfo<BizCommissionProductVo> queryPageList(BizCommissionProductBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的特殊产品费率配置列表
     *
     * @param bo 查询条件
     * @return 特殊产品费率配置列表
     */
    List<BizCommissionProductVo> queryList(BizCommissionProductBo bo);

    /**
     * 新增特殊产品费率配置
     *
     * @param bo 特殊产品费率配置
     * @return 是否新增成功
     */
    Boolean insertByBo(BizCommissionProductBo bo);

    /**
     * 修改特殊产品费率配置
     *
     * @param bo 特殊产品费率配置
     * @return 是否修改成功
     */
    Boolean updateByBo(BizCommissionProductBo bo);

    /**
     * 校验并批量删除特殊产品费率配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询特殊产品费率配置
     *
     * @param productId 产品ID
     * @param tenantId  租户ID
     * @return 特殊产品费率配置
     */
    BizCommissionProduct queryByProductIdAndTenantId(Long productId, String tenantId);
}
