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
import org.dromara.system.domain.bo.SysUserInviteBo;
import org.dromara.system.domain.vo.SysUserInviteVo;
import org.dromara.system.domain.SysUserInvite;
import org.dromara.system.mapper.SysUserInviteMapper;
import org.dromara.system.service.ISysUserInviteService;

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
    public List<SysUserInviteVo> queryList(SysUserInviteBo bo) {
        LambdaQueryWrapper<SysUserInvite> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysUserInvite> buildQueryWrapper(SysUserInviteBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysUserInvite> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SysUserInvite::getId);
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
        SysUserInvite add = MapstructUtils.convert(bo, SysUserInvite.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
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
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
