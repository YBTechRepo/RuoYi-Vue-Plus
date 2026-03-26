package org.dromara.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.BCrypt;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.SysUserInviteBo;
import org.dromara.system.domain.vo.SysUserInviteVo;
import org.dromara.system.domain.SysUserInvite;
import org.dromara.system.mapper.SysUserInviteMapper;
import org.dromara.system.service.ISysUserInviteService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 人员邀请登记Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserInviteServiceImpl implements ISysUserInviteService {

    private final SysUserInviteMapper baseMapper;

    private final SysUserInviteMapper userInviteMapper;

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysDeptMapper sysDeptMapper;

    private final ISysUserService sysUserService;

    private final ISysDeptService sysDeptService;

    /**
     * 查询人员邀请登记
     *
     * @param id 主键
     * @return 人员邀请登记
     */
    @Override
    public SysUserInviteVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询人员邀请登记列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 人员邀请登记分页列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public TableDataInfo<SysUserInviteVo> queryPageList(SysUserInviteBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUserInvite> lqw = buildQueryWrapper(bo);
        Page<SysUserInviteVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的人员邀请登记列表
     *
     * @param bo 查询条件
     * @return 人员邀请登记列表
     */
    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    public List<SysUserInviteVo> queryList(SysUserInviteBo bo) {
        LambdaQueryWrapper<SysUserInvite> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysUserInvite> buildQueryWrapper(SysUserInviteBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysUserInvite> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(SysUserInvite::getId);
        lqw.like(StringUtils.isNotBlank(bo.getNickName()), SysUserInvite::getNickName, bo.getNickName());
        lqw.eq(StringUtils.isNotBlank(bo.getPhoneNumber()), SysUserInvite::getPhoneNumber, bo.getPhoneNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCard()), SysUserInvite::getIdCard, bo.getIdCard());
        lqw.like(StringUtils.isNotBlank(bo.getReferrerName()), SysUserInvite::getReferrerName, bo.getReferrerName());
        lqw.eq(bo.getStatus() != null, SysUserInvite::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增人员邀请登记
     *
     * @param bo 人员邀请登记
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SysUserInviteBo bo) {
        if (bo.getReferrerId() == null) {
            throw new ServiceException("邀请链接无效，缺少推荐人参数");
        }

        // 1. 绝对霸体：无视当前环境，跨租户查推荐人
        SysUserVo referrer = TenantHelper.ignore(() -> sysUserMapper.selectVoById(bo.getReferrerId()));
        if (referrer == null) {
            throw new ServiceException("邀请人不存在或已被删除");
        }

        String tenantId = referrer.getTenantId();

        // 2. 时空穿梭：将接下来的所有危险操作，全部包裹在目标租户的上下文中
        return TenantHelper.dynamic(tenantId, () -> {

            // 此时，Redis 拦截器会乖乖拼上 998923 的前缀
            String newAgentCode = this.generateAgentCode();

            SysUserInvite add = MapstructUtils.convert(bo, SysUserInvite.class);
            add.setUserName(newAgentCode);

            // 依然手动设置好审计字段（此时底层 MetaObjectHandler 也会识别到正确的租户）
            add.setTenantId(tenantId);
            add.setCreateBy(referrer.getUserId());
            add.setUpdateBy(referrer.getUserId());
            add.setCreateDept(referrer.getDeptId());

            validEntityBeforeSave(add);

            // 此时执行 insert，MyBatis-Plus 会完全认定你就是 998923 租户的人，绝不报错
            boolean flag = baseMapper.insert(add) > 0;
            if (flag) {
                bo.setId(add.getId());
            }
            return flag;
        });
    }

    /**
     * 修改人员邀请登记
     *
     * @param bo 人员邀请登记
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SysUserInviteBo bo) {
        SysUserInvite update = MapstructUtils.convert(bo, SysUserInvite.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysUserInvite entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除人员邀请登记信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验，判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndInsertUserByInviteId(SysUserInviteBo bo) {
        // 参数校验
        if (bo == null || bo.getInviteId() == null) {
            throw new ServiceException("参数错误");
        }

        Boolean updateFlag = this.updateByBo(bo);
        if(!updateFlag){
            throw new ServiceException("更新邀请记录失败");
        }

        if(bo.getStatus() == 2){ //审核拒绝
            return true;
        }

        // 审核通过，新增用户
        // 先查询邀请记录
        SysUserInviteVo inviteVo = userInviteMapper.selectVoById(bo.getInviteId());
        if (inviteVo == null) {
            throw new ServiceException("邀请记录不存在");
        }

        // 查询推荐人信息（只查询一次）
        Long referrerId = inviteVo.getReferrerId();
        if (referrerId == null) {
            throw new ServiceException("推荐人 ID 不能为空");
        }
        SysUserVo sysUserVo = sysUserMapper.selectVoById(referrerId);
        if(sysUserVo == null){
            throw new ServiceException("推荐人不存在");
        }

        // 判断 inviteType 是否为 teamleader，如果是则创建新部门
//        Long teamDeptId = null;
//        SysDeptBo sysDeptBo = null;
//        if("teamleader".equals(inviteVo.getInviteType())){
//            sysDeptBo = new SysDeptBo();
//            sysDeptBo.setParentId(sysUserVo.getDeptId());
//            sysDeptBo.setDeptName(StringUtils.defaultString(inviteVo.getNickName(), "") + "团队");
//            sysDeptBo.setDeptCategory("2");
//            sysDeptBo.setOrderNum(0);
//            sysDeptBo.setPhone(StringUtils.defaultString(inviteVo.getPhoneNumber(), ""));
//            sysDeptBo.setStatus("0");
//            sysDeptService.insertDept(sysDeptBo);
//            teamDeptId = sysDeptBo.getDeptId();
//        }
        Long teamDeptId = null;
        SysDeptBo sysDeptBo = null;
        if("teamleader".equals(inviteVo.getInviteType())){
            // 构建部门实体对象
            SysDept dept = new SysDept();
            dept.setParentId(sysUserVo.getDeptId());
            dept.setDeptName(StringUtils.defaultString(inviteVo.getNickName(), "") + "团队");
            dept.setDeptCategory("2");
            dept.setOrderNum(0);
            dept.setPhone(StringUtils.defaultString(inviteVo.getPhoneNumber(), ""));
            dept.setStatus("0");

            // 查询父部门信息以设置 ancestors
            SysDept parentDept = sysDeptMapper.selectById(sysUserVo.getDeptId());
            if (parentDept == null || !"0".equals(parentDept.getStatus())) {
                throw new ServiceException("父部门不存在或已停用");
            }
            dept.setAncestors(parentDept.getAncestors() + "," + parentDept.getDeptId());

            // 直接插入，MyBatis-Plus 会自动回填主键
            int result = sysDeptMapper.insert(dept);
            if (result <= 0) {
                throw new ServiceException("创建部门失败");
            }

            // 此时 dept.getDeptId() 已经有值了
            teamDeptId = dept.getDeptId();

            // 转换为 SysDeptBo 用于后续更新
            sysDeptBo = BeanUtil.toBean(dept, SysDeptBo.class);
        }

        // 根据 inviteType 查询对应 roleId
        String inviteType = inviteVo.getInviteType();
        if (StringUtils.isBlank(inviteType)) {
            throw new ServiceException("邀请类型不能为空");
        }
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, inviteType)
            .eq(SysRole::getStatus, "0"));
        if (role == null) {
            throw new ServiceException("邀请类型对应的角色不存在或已停用");
        }
        Long roleId = role.getRoleId();

        // 确定用户的部门 ID
        Long deptId;
        if(teamDeptId != null){
            deptId = teamDeptId;
        }else{
            deptId = sysUserVo.getDeptId();
        }

        SysUserBo userBo = new SysUserBo();
        userBo.setUserName(inviteVo.getUserName());
        userBo.setNickName(inviteVo.getNickName());
        userBo.setPhonenumber(inviteVo.getPhoneNumber());
        userBo.setIdNo(inviteVo.getIdCard());
        userBo.setReferrerName(inviteVo.getReferrerName());
        userBo.setReferrerId(inviteVo.getReferrerId());
        userBo.setDeptId(deptId);
        userBo.setStatus("0");
        String initPassword = SpringUtils.getBean(ISysConfigService.class).selectConfigByKey("sys.user.initPassword");
        userBo.setPassword(BCrypt.hashpw(initPassword));
        userBo.setInviteId(inviteVo.getId());
        userBo.setInviteType(inviteType);
        userBo.setRoleIds(new Long[]{roleId});

        // 执行插入用户（如果失败会抛异常，触发事务回滚）
        int rows = sysUserService.insertUser(userBo);
        if (rows <= 0) {
            throw new ServiceException("新增用户失败");
        }

        // 如果是新建了部门，则更新部门负责人
        if(teamDeptId != null && sysDeptBo != null){
            sysDeptBo.setLeader(userBo.getUserId());
            int updateResult = sysDeptService.updateDept(sysDeptBo);
            if (updateResult <= 0) {
                throw new ServiceException("更新部门负责人失败");
            }
        }

        return true;
    }

    public String generateAgentCode() {
        String rolePrefix = "A";

        // 1. 获取当前年份 (例如 "26")
        String year = DateUtil.format(new Date(), "yy");

        // 2. 定义 Redis 的 Key，按年归零 (例如 "sys:agent_seq:26")
        String redisKey = "sys:agent_seq:" + year;

        // 3. 利用 Redis 原子自增，获取下一个序号 (初始从 1 开始)
        Long nextSeq = RedisUtils.incrAtomicValue(redisKey);

        // 4. 将序号补齐为 4 位数 (例如 1 变成 "0001", 12 变成 "0012")
        String seqStr = String.format("%04d", nextSeq);

        // 5. 拼装返回最终工号：A260001
        return rolePrefix + year + seqStr;
    }
}
