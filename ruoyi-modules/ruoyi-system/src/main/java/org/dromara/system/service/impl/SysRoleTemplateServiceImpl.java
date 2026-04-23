package org.dromara.system.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.SysRoleTemplateBo;
import org.dromara.system.domain.vo.SysRoleTemplateVo;
import org.dromara.system.domain.SysRoleTemplate;
import org.dromara.system.mapper.SysRoleTemplateMapper;
import org.dromara.system.service.ISysRoleTemplateService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 租户角色管理Service业务层处理
 *
 * @author li.xiang
 * @date 2026-04-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysRoleTemplateServiceImpl implements ISysRoleTemplateService {

    private final SysRoleTemplateMapper baseMapper;

    /**
     * 查询租户角色管理
     *
     * @param id 主键
     * @return 租户角色管理
     */
    @Override
    public SysRoleTemplateVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询租户角色管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 租户角色管理分页列表
     */
    @Override
    public TableDataInfo<SysRoleTemplateVo> queryPageList(SysRoleTemplateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysRoleTemplate> lqw = buildQueryWrapper(bo);
        Page<SysRoleTemplateVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的租户角色管理列表
     *
     * @param bo 查询条件
     * @return 租户角色管理列表
     */
    @Override
    public List<SysRoleTemplateVo> queryList(SysRoleTemplateBo bo) {
        LambdaQueryWrapper<SysRoleTemplate> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysRoleTemplate> buildQueryWrapper(SysRoleTemplateBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysRoleTemplate> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SysRoleTemplate::getId);
        lqw.like(StringUtils.isNotBlank(bo.getTemplateName()), SysRoleTemplate::getTemplateName, bo.getTemplateName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysRoleTemplate::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增租户角色管理
     *
     * @param bo 租户角色管理
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SysRoleTemplateBo bo) {
        SysRoleTemplate add = MapstructUtils.convert(bo, SysRoleTemplate.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改租户角色管理
     *
     * @param bo 租户角色管理
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SysRoleTemplateBo bo) {
        SysRoleTemplate update = MapstructUtils.convert(bo, SysRoleTemplate.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysRoleTemplate entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除租户角色管理信息
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
