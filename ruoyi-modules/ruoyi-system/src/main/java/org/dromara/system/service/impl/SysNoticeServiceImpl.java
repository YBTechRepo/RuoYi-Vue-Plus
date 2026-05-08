package org.dromara.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.ObjectUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysNotice;
import org.dromara.system.domain.SysNoticeRead;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysNoticeMapper;
import org.dromara.system.mapper.SysNoticeReadMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 公告 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    private final SysNoticeMapper baseMapper;
    private final SysUserMapper userMapper;
    private final SysNoticeReadMapper noticeReadMapper;

    /**
     * 分页查询通知公告列表
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @return 通知公告分页列表
     */
    @Override
    public TableDataInfo<SysNoticeVo> selectPageNoticeList(SysNoticeBo notice, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        Page<SysNoticeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNoticeVo selectNoticeById(Long noticeId) {
        return baseMapper.selectVoById(noticeId);
    }

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNoticeVo> selectNoticeList(SysNoticeBo notice) {
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 分页查询用户端可见通知公告列表
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @return 通知公告分页列表
     */
    @Override
    public TableDataInfo<SysNoticeVo> selectPageUserNoticeList(SysNoticeBo notice, PageQuery pageQuery) {
        return TenantHelper.dynamic("000000", () -> {
            LambdaQueryWrapper<SysNotice> lqw = buildUserQueryWrapper(notice);
            Page<SysNoticeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
            return TableDataInfo.build(page);
        });
    }

    /**
     * 查询用户端可见公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNoticeVo selectUserNoticeById(Long noticeId) {
        return TenantHelper.dynamic("000000", () -> {
            SysNoticeBo bo = new SysNoticeBo();
            bo.setClientId(LoginHelper.getClientId());
            LambdaQueryWrapper<SysNotice> lqw = buildUserQueryWrapper(bo);
            lqw.eq(SysNotice::getNoticeId, noticeId);
            return baseMapper.selectVoOne(lqw);
        });
    }

    /**
     * 查询当前用户未确认的登录弹窗通知
     *
     * @return 通知公告集合
     */
    @Override
    public List<SysNoticeVo> selectUnreadPopupNotices(String clientId) {
        Long userId = LoginHelper.getUserId();
        String targetClientId = StringUtils.blankToDefault(clientId, LoginHelper.getClientId());
        Date now = new Date();
        List<Long> readNoticeIds = noticeReadMapper.selectList(Wrappers.lambdaQuery(SysNoticeRead.class)
            .eq(SysNoticeRead::getUserId, userId))
            .stream()
            .map(SysNoticeRead::getNoticeId)
            .toList();

        return TenantHelper.dynamic("000000", () -> {
            LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
            lqw.eq(SysNotice::getStatus, "0");
            lqw.eq(StringUtils.isNotBlank(targetClientId), SysNotice::getClientId, targetClientId);
            lqw.eq(SysNotice::getPopupFlag, "1");
            lqw.and(wrapper -> wrapper.isNull(SysNotice::getPopupStartTime).or().le(SysNotice::getPopupStartTime, now));
            lqw.and(wrapper -> wrapper.isNull(SysNotice::getPopupEndTime).or().ge(SysNotice::getPopupEndTime, now));
            lqw.notIn(!readNoticeIds.isEmpty(), SysNotice::getNoticeId, readNoticeIds);
            lqw.orderByAsc(SysNotice::getCreateTime);
            return baseMapper.selectVoList(lqw);
        });
    }

    /**
     * 标记登录弹窗通知已读
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int readPopupNotice(Long noticeId) {
        Long userId = LoginHelper.getUserId();
        Long existCount = noticeReadMapper.selectCount(Wrappers.lambdaQuery(SysNoticeRead.class)
            .eq(SysNoticeRead::getNoticeId, noticeId)
            .eq(SysNoticeRead::getUserId, userId));
        if (existCount > 0) {
            return 1;
        }

        Long noticeCount = TenantHelper.dynamic("000000", () -> baseMapper.selectCount(Wrappers.lambdaQuery(SysNotice.class)
            .eq(SysNotice::getNoticeId, noticeId)
            .eq(SysNotice::getStatus, "0")
            .eq(SysNotice::getPopupFlag, "1")));
        if (noticeCount <= 0) {
            return 0;
        }

        SysNoticeRead read = new SysNoticeRead();
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(new Date());
        try {
            return noticeReadMapper.insert(read);
        } catch (DuplicateKeyException e) {
            return 1;
        }
    }

    private LambdaQueryWrapper<SysNotice> buildQueryWrapper(SysNoticeBo bo) {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(StringUtils.isNotBlank(bo.getClientId()), SysNotice::getClientId, bo.getClientId());
        if (StringUtils.isNotBlank(bo.getCreateByName())) {
            SysUserVo sysUser = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, bo.getCreateByName()));
            lqw.eq(SysNotice::getCreateBy, ObjectUtils.notNullGetter(sysUser, SysUserVo::getUserId));
        }
        lqw.orderByAsc(SysNotice::getNoticeId);
        return lqw;
    }

    private LambdaQueryWrapper<SysNotice> buildUserQueryWrapper(SysNoticeBo bo) {
        bo = bo == null ? new SysNoticeBo() : bo;
        String targetClientId = StringUtils.blankToDefault(bo.getClientId(), LoginHelper.getClientId());
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(SysNotice::getStatus, "0");
        if (StringUtils.isNotBlank(targetClientId)) {
            lqw.and(wrapper -> wrapper.isNull(SysNotice::getClientId)
                .or().eq(SysNotice::getClientId, "")
                .or().eq(SysNotice::getClientId, targetClientId));
        } else {
            lqw.and(wrapper -> wrapper.isNull(SysNotice::getClientId).or().eq(SysNotice::getClientId, ""));
        }
        lqw.orderByDesc(SysNotice::getCreateTime);
        return lqw;
    }

    /**
     * 新增公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    @Override
    public int insertNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.insert(notice);
    }

    /**
     * 修改公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    @Override
    public int updateNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.updateById(notice);
    }

    /**
     * 删除公告对象
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        return baseMapper.deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteByIds(Arrays.asList(noticeIds));
    }
}
