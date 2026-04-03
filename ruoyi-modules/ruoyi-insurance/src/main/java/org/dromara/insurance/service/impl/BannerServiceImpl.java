package org.dromara.insurance.service.impl;

import org.dromara.common.core.domain.R;
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
import org.dromara.insurance.domain.bo.BannerBo;
import org.dromara.insurance.domain.vo.BannerVo;
import org.dromara.insurance.domain.Banner;
import org.dromara.insurance.mapper.BannerMapper;
import org.dromara.insurance.service.IBannerService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 轮播图配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BannerServiceImpl implements IBannerService {

    private final BannerMapper baseMapper;

    /**
     * 查询轮播图配置
     *
     * @param id 主键
     * @return 轮播图配置
     */
    @Override
    public BannerVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询轮播图配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 轮播图配置分页列表
     */
    @Override
    public TableDataInfo<BannerVo> queryPageList(BannerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Banner> lqw = buildQueryWrapper(bo);
        Page<BannerVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的轮播图配置列表
     *
     * @param bo 查询条件
     * @return 轮播图配置列表
     */
    @Override
    public List<BannerVo> queryList(BannerBo bo) {
        LambdaQueryWrapper<Banner> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Banner> buildQueryWrapper(BannerBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Banner> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Banner::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), Banner::getTitle, bo.getTitle());
        lqw.eq(bo.getJumpType() != null, Banner::getJumpType, bo.getJumpType());
        lqw.eq(StringUtils.isNotBlank(bo.getJumpUrl()), Banner::getJumpUrl, bo.getJumpUrl());
        lqw.eq(bo.getSortOrder() != null, Banner::getSortOrder, bo.getSortOrder());
        lqw.eq(bo.getStatus() != null, Banner::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增轮播图配置
     *
     * @param bo 轮播图配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BannerBo bo) {
        Banner add = MapstructUtils.convert(bo, Banner.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改轮播图配置
     *
     * @param bo 轮播图配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BannerBo bo) {
        Banner update = MapstructUtils.convert(bo, Banner.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Banner entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除轮播图配置信息
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

    @Override
    public List<BannerVo> querySalesList() {
        // 1. 创建纯净的条件构造器
        LambdaQueryWrapper<Banner> lqw = Wrappers.lambdaQuery();

        // 2. 核心业务规则：只查询状态为 "0" (正常/启用) 的轮播图，并按排序号升序排列
        lqw.eq(Banner::getStatus, 0)
            .eq(Banner::getDelFlag, "0")
            .orderByAsc(Banner::getSortOrder);

        // 3. 调用 RuoYi-Vue-Plus 封装的 selectVoList 直接查出 VO 集合
        return baseMapper.selectVoList(lqw);
    }
}
