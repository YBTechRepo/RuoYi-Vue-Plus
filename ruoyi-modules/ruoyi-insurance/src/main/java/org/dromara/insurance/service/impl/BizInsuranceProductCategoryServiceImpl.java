package org.dromara.insurance.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.helper.DataBaseHelper;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import org.dromara.common.core.utils.TreeBuildUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.insurance.domain.bo.InsuranceProductCategoryBo;
import org.dromara.insurance.domain.vo.InsuranceProductCategoryVo;
import org.dromara.insurance.domain.InsuranceProductCategory;
import org.dromara.insurance.mapper.BizInsuranceProductCategoryMapper;
import org.dromara.insurance.service.IBizInsuranceProductCategoryService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 产品分类管理Service业务层处理
 *
 * @author lixiang
 * @date 2026-04-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizInsuranceProductCategoryServiceImpl implements IBizInsuranceProductCategoryService {

    private final BizInsuranceProductCategoryMapper baseMapper;

    @Override
    public List<Tree<Long>> selectCategoryTreeList(InsuranceProductCategoryBo bo) {
        List<InsuranceProductCategoryVo> list = queryList(bo);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.buildMultiRoot(
            list,
            InsuranceProductCategoryVo::getCategoryId,
            InsuranceProductCategoryVo::getParentId,
            (node, treeNode) -> treeNode
                .setId(node.getCategoryId())
                .setParentId(node.getParentId())
                .setName(node.getCategoryName())
                .setWeight(node.getSort())
                .putExtra("icon", node.getIcon())
        );
    }

    /**
     * 查询产品分类管理
     *
     * @param categoryId 主键
     * @return 产品分类管理
     */
    @Override
    public InsuranceProductCategoryVo queryById(Long categoryId){
        return baseMapper.selectVoById(categoryId);
    }

    /**
     * 分页查询产品分类管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品分类管理分页列表
     */
    @Override
    public TableDataInfo<InsuranceProductCategoryVo> queryPageList(InsuranceProductCategoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceProductCategory> lqw = buildQueryWrapper(bo);
        Page<InsuranceProductCategoryVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的产品分类管理列表
     *
     * @param bo 查询条件
     * @return 产品分类管理列表
     */
    @Override
    public List<InsuranceProductCategoryVo> queryList(InsuranceProductCategoryBo bo) {
        LambdaQueryWrapper<InsuranceProductCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceProductCategory> buildQueryWrapper(InsuranceProductCategoryBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceProductCategory> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceProductCategory::getSort);
        lqw.eq(bo.getParentId() != null, InsuranceProductCategory::getParentId, bo.getParentId());
        lqw.eq(StringUtils.isNotBlank(bo.getAncestors()), InsuranceProductCategory::getAncestors, bo.getAncestors());
        lqw.like(StringUtils.isNotBlank(bo.getCategoryName()), InsuranceProductCategory::getCategoryName, bo.getCategoryName());
        lqw.eq(StringUtils.isNotBlank(bo.getIcon()), InsuranceProductCategory::getIcon, bo.getIcon());
        lqw.eq(bo.getSort() != null, InsuranceProductCategory::getSort, bo.getSort());
        lqw.eq(bo.getStatus() != null, InsuranceProductCategory::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增产品分类管理
     *
     * @param bo 产品分类管理
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceProductCategoryBo bo) {
        InsuranceProductCategory add = MapstructUtils.convert(bo, InsuranceProductCategory.class);
        validEntityBeforeSave(add);

        if (add.getParentId() == null || add.getParentId() == 0L) {
            add.setAncestors("0");
        } else {
            InsuranceProductCategory parent = baseMapper.selectById(add.getParentId());
            if (parent != null) {
                add.setAncestors(parent.getAncestors() + "," + add.getParentId());
            } else {
                add.setAncestors("0");
            }
        }

        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setCategoryId(add.getCategoryId());
        }
        return flag;
    }

    /**
     * 修改产品分类管理
     *
     * @param bo 产品分类管理
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(InsuranceProductCategoryBo bo) {
        InsuranceProductCategory update = MapstructUtils.convert(bo, InsuranceProductCategory.class);
        validEntityBeforeSave(update);

        InsuranceProductCategory old = baseMapper.selectById(update.getCategoryId());
        if (update.getParentId() != null && !update.getParentId().equals(old.getParentId())) {
            String newAncestors;
            if (update.getParentId() == 0L) {
                newAncestors = "0";
            } else {
                InsuranceProductCategory parent = baseMapper.selectById(update.getParentId());
                if (parent != null) {
                    newAncestors = parent.getAncestors() + "," + update.getParentId();
                } else {
                    newAncestors = "0";
                }
            }
            String oldAncestors = old.getAncestors();
            update.setAncestors(newAncestors);
            updateCategoryChildren(update.getCategoryId(), newAncestors, oldAncestors);
        }

        return baseMapper.updateById(update) > 0;
    }

    private void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors) {
        List<InsuranceProductCategory> children = baseMapper.selectList(Wrappers.<InsuranceProductCategory>lambdaQuery()
            .apply(DataBaseHelper.findInSet(categoryId, "ancestors")));
        if (CollUtil.isNotEmpty(children)) {
            for (InsuranceProductCategory child : children) {
                InsuranceProductCategory update = new InsuranceProductCategory();
                update.setCategoryId(child.getCategoryId());
                update.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
                baseMapper.updateById(update);
            }
        }
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceProductCategory entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除产品分类管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
