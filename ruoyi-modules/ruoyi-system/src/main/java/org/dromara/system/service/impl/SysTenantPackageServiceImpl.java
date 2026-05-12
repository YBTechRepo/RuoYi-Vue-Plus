package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysRoleMenu;
import org.dromara.system.domain.SysRoleTemplate;
import org.dromara.system.domain.SysTenant;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.domain.bo.SysTenantPackageBo;
import org.dromara.system.domain.vo.SysTenantPackageVo;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysRoleMenuMapper;
import org.dromara.system.mapper.SysRoleTemplateMapper;
import org.dromara.system.mapper.SysTenantMapper;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.dromara.system.service.ISysTenantPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 租户套餐Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantPackageServiceImpl implements ISysTenantPackageService {

    private final SysTenantPackageMapper baseMapper;
    private final SysTenantMapper tenantMapper;
    private final SysRoleTemplateMapper roleTemplateMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Data
    private static class RoleTemplateDTO {
        private String roleName;
        private String roleKey;
        private List<Long> menuIds;
        private Integer sort;
    }

    /**
     * 查询租户套餐
     */
    @Override
    public SysTenantPackageVo queryById(Long packageId){
        return baseMapper.selectVoById(packageId);
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public TableDataInfo<SysTenantPackageVo> queryPageList(SysTenantPackageBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        Page<SysTenantPackageVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysTenantPackageVo> selectList() {
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysTenantPackage>()
                .eq(SysTenantPackage::getStatus, SystemConstants.NORMAL));
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public List<SysTenantPackageVo> queryList(SysTenantPackageBo bo) {
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysTenantPackage> buildQueryWrapper(SysTenantPackageBo bo) {
        LambdaQueryWrapper<SysTenantPackage> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), SysTenantPackage::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysTenantPackage::getStatus, bo.getStatus());
        lqw.orderByAsc(SysTenantPackage::getPackageId);
        return lqw;
    }

    /**
     * 新增租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantPackageBo bo) {
        SysTenantPackage add = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        add.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setPackageId(add.getPackageId());
        }
        return flag;
    }

    /**
     * 修改租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SysTenantPackageBo bo) {
        SysTenantPackage update = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        update.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 校验套餐名称是否唯一
     */
    @Override
    public boolean checkPackageNameUnique(SysTenantPackageBo bo) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysTenantPackage>()
            .eq(SysTenantPackage::getPackageName, bo.getPackageName())
            .ne(ObjectUtil.isNotNull(bo.getPackageId()), SysTenantPackage::getPackageId, bo.getPackageId()));
        return !exist;
    }

    /**
     * 修改套餐状态
     *
     * @param bo 套餐信息
     * @return 结果
     */
    @Override
    public int updatePackageStatus(SysTenantPackageBo bo) {
        SysTenantPackage tenantPackage = MapstructUtils.convert(bo, SysTenantPackage.class);
        return baseMapper.updateById(tenantPackage);
    }

    /**
     * 批量同步套餐权限到使用该套餐的租户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncPackageRoles(Long packageId) {
        if (ObjectUtil.isNull(packageId)) {
            throw new ServiceException("套餐ID不能为空");
        }
        return TenantHelper.ignore(() -> {
            SysTenantPackage tenantPackage = baseMapper.selectById(packageId);
            if (ObjectUtil.isNull(tenantPackage)) {
                throw new ServiceException("套餐不存在");
            }
            Set<Long> packageMenuIds = new HashSet<>(StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong));
            List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getPackageId, packageId)
                .ne(SysTenant::getTenantId, TenantConstants.DEFAULT_TENANT_ID));
            if (CollUtil.isEmpty(tenants)) {
                return true;
            }

            List<SysRoleTemplate> templates = roleTemplateMapper.selectList(new LambdaQueryWrapper<SysRoleTemplate>()
                .eq(SysRoleTemplate::getTenantPackageId, packageId)
                .eq(SysRoleTemplate::getStatus, SystemConstants.NORMAL));
            Map<Long, List<RoleTemplateDTO>> templateRoleMap = buildTemplateRoleMap(templates);

            for (SysTenant tenant : tenants) {
                List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getTenantId, tenant.getTenantId()));
                if (CollUtil.isEmpty(roles)) {
                    continue;
                }
                Map<String, SysRole> roleMap = roles.stream()
                    .collect(Collectors.toMap(SysRole::getRoleKey, role -> role, (left, right) -> left));
                prunePackageOutsideMenus(roles, packageMenuIds);

                SysRole adminRole = roleMap.get(TenantConstants.TENANT_ADMIN_ROLE_KEY);
                if (adminRole != null) {
                    appendRoleMenus(adminRole.getRoleId(), packageMenuIds);
                }

                List<RoleTemplateDTO> templateRoles = resolveTemplateRoles(tenant, templates, templateRoleMap);
                if (CollUtil.isEmpty(templateRoles)) {
                    continue;
                }
                for (RoleTemplateDTO templateRole : templateRoles) {
                    SysRole role = roleMap.get(templateRole.getRoleKey());
                    if (role == null || CollUtil.isEmpty(templateRole.getMenuIds())) {
                        continue;
                    }
                    Set<Long> safeMenuIds = templateRole.getMenuIds().stream()
                        .filter(packageMenuIds::contains)
                        .collect(Collectors.toSet());
                    appendRoleMenus(role.getRoleId(), safeMenuIds);
                }
            }
            return true;
        });
    }

    private Map<Long, List<RoleTemplateDTO>> buildTemplateRoleMap(List<SysRoleTemplate> templates) {
        if (CollUtil.isEmpty(templates)) {
            return Collections.emptyMap();
        }
        Map<Long, List<RoleTemplateDTO>> templateRoleMap = new HashMap<>(templates.size());
        for (SysRoleTemplate template : templates) {
            if (StringUtils.isBlank(template.getRolesJson())) {
                templateRoleMap.put(template.getId(), Collections.emptyList());
                continue;
            }
            List<RoleTemplateDTO> templateRoles = JsonUtils.parseArray(template.getRolesJson(), RoleTemplateDTO.class);
            templateRoleMap.put(template.getId(), templateRoles);
        }
        return templateRoleMap;
    }

    private List<RoleTemplateDTO> resolveTemplateRoles(SysTenant tenant, List<SysRoleTemplate> templates, Map<Long, List<RoleTemplateDTO>> templateRoleMap) {
        Long roleTemplateId = tenant.getRoleTemplateId();
        if (ObjectUtil.isNotNull(roleTemplateId)) {
            return templateRoleMap.getOrDefault(roleTemplateId, Collections.emptyList());
        }
        if (templates.size() == 1) {
            return templateRoleMap.getOrDefault(templates.get(0).getId(), Collections.emptyList());
        }
        return Collections.emptyList();
    }

    private void prunePackageOutsideMenus(List<SysRole> roles, Set<Long> packageMenuIds) {
        if (CollUtil.isEmpty(roles)) {
            return;
        }
        List<Long> roleIds = roles.stream()
            .map(SysRole::getRoleId)
            .toList();
        if (CollUtil.isEmpty(packageMenuIds)) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
            return;
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
            .in(SysRoleMenu::getRoleId, roleIds)
            .notIn(SysRoleMenu::getMenuId, packageMenuIds));
    }

    private void appendRoleMenus(Long roleId, Collection<Long> menuIds) {
        if (ObjectUtil.isNull(roleId) || CollUtil.isEmpty(menuIds)) {
            return;
        }
        Set<Long> existingMenuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toSet());
        List<SysRoleMenu> roleMenus = menuIds.stream()
            .filter(menuId -> !existingMenuIds.contains(menuId))
            .map(menuId -> {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            })
            .toList();
        if (CollUtil.isNotEmpty(roleMenus)) {
            roleMenuMapper.insertBatch(roleMenus);
        }
    }

    /**
     * 批量删除租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            boolean exists = tenantMapper.exists(new LambdaQueryWrapper<SysTenant>().in(SysTenant::getPackageId, ids));
            if (exists) {
                throw new ServiceException("租户套餐已被使用");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
