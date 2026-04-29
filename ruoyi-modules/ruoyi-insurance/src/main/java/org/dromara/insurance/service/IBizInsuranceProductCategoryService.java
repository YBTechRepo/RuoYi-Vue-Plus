package org.dromara.insurance.service;

import org.dromara.insurance.domain.vo.InsuranceProductCategoryVo;
import org.dromara.insurance.domain.bo.InsuranceProductCategoryBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import cn.hutool.core.lang.tree.Tree;

import java.util.Collection;
import java.util.List;

/**
 * 产品分类管理Service接口
 *
 * @author lixiang
 * @date 2026-04-29
 */
public interface IBizInsuranceProductCategoryService {

    /**
     * 查询分类树列表
     *
     * @param bo 查询条件
     * @return 分类树列表
     */
    List<Tree<Long>> selectCategoryTreeList(InsuranceProductCategoryBo bo);

    /**
     * 查询产品分类管理
     *
     * @param categoryId 主键
     * @return 产品分类管理
     */
    InsuranceProductCategoryVo queryById(Long categoryId);

    /**
     * 分页查询产品分类管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品分类管理分页列表
     */
    TableDataInfo<InsuranceProductCategoryVo> queryPageList(InsuranceProductCategoryBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的产品分类管理列表
     *
     * @param bo 查询条件
     * @return 产品分类管理列表
     */
    List<InsuranceProductCategoryVo> queryList(InsuranceProductCategoryBo bo);

    /**
     * 新增产品分类管理
     *
     * @param bo 产品分类管理
     * @return 是否新增成功
     */
    Boolean insertByBo(InsuranceProductCategoryBo bo);

    /**
     * 修改产品分类管理
     *
     * @param bo 产品分类管理
     * @return 是否修改成功
     */
    Boolean updateByBo(InsuranceProductCategoryBo bo);

    /**
     * 校验并批量删除产品分类管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
