package org.dromara.insurance.service;

import org.dromara.common.core.domain.R;
import org.dromara.insurance.domain.vo.BannerVo;
import org.dromara.insurance.domain.bo.BannerBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 轮播图配置Service接口
 *
 * @author li.xiang
 * @date 2026-03-31
 */
public interface IBannerService {

    /**
     * 查询轮播图配置
     *
     * @param id 主键
     * @return 轮播图配置
     */
    BannerVo queryById(Long id);

    /**
     * 分页查询轮播图配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 轮播图配置分页列表
     */
    TableDataInfo<BannerVo> queryPageList(BannerBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的轮播图配置列表
     *
     * @param bo 查询条件
     * @return 轮播图配置列表
     */
    List<BannerVo> queryList(BannerBo bo);

    /**
     * 新增轮播图配置
     *
     * @param bo 轮播图配置
     * @return 是否新增成功
     */
    Boolean insertByBo(BannerBo bo);

    /**
     * 修改轮播图配置
     *
     * @param bo 轮播图配置
     * @return 是否修改成功
     */
    Boolean updateByBo(BannerBo bo);

    /**
     * 校验并批量删除轮播图配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<BannerVo> querySalesList();
}
