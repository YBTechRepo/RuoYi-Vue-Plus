package org.dromara.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.springframework.stereotype.Service;
import org.dromara.finance.domain.bo.BizAccountFlowBo;
import org.dromara.finance.domain.vo.BizAccountFlowVo;
import org.dromara.finance.domain.BizAccountFlow;
import org.dromara.finance.mapper.BizAccountFlowMapper;
import org.dromara.finance.service.IBizAccountFlowService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 账户明细Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizAccountFlowServiceImpl implements IBizAccountFlowService {

    private final BizAccountFlowMapper baseMapper;

    /**
     * 查询账户明细
     *
     * @param id 主键
     * @return 账户明细
     */
    @Override
    public BizAccountFlowVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询账户明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户明细分页列表
     */
    @Override
    public TableDataInfo<BizAccountFlowVo> queryPageList(BizAccountFlowBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizAccountFlow> lqw = buildQueryWrapper(bo);
        Page<BizAccountFlowVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的账户明细列表
     *
     * @param bo 查询条件
     * @return 账户明细列表
     */
    @Override
    public List<BizAccountFlowVo> queryList(BizAccountFlowBo bo) {
        LambdaQueryWrapper<BizAccountFlow> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizAccountFlow> buildQueryWrapper(BizAccountFlowBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizAccountFlow> lqw = Wrappers.lambdaQuery();
        //lqw.orderByDesc(BizAccountFlow::getId);
        lqw.orderByDesc(BizAccountFlow::getCreateTime);
        lqw.eq(bo.getUserId() != null, BizAccountFlow::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getBizNo()), BizAccountFlow::getBizNo, bo.getBizNo());
        return lqw;
    }

    /**
     * 新增账户明细
     *
     * @param bo 账户明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizAccountFlowBo bo) {
        BizAccountFlow add = MapstructUtils.convert(bo, BizAccountFlow.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改账户明细
     *
     * @param bo 账户明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizAccountFlowBo bo) {
        BizAccountFlow update = MapstructUtils.convert(bo, BizAccountFlow.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizAccountFlow entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除账户明细信息
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
    public TableDataInfo<BizAccountFlowVo> queryAdminPageList(BizAccountFlowBo bo, PageQuery pageQuery) {
        // 1. 复用原有的 buildQueryWrapper 拼装基础过滤条件（单号、用户ID等）
        LambdaQueryWrapper<BizAccountFlow> lqw = buildQueryWrapper(bo);

        // 2. 强制加上时间倒序，确保财务一眼看到最新的流水
        lqw.orderByDesc(BizAccountFlow::getCreateTime);

        // 3. 调用 Mapper 里的“上帝视角”方法
        Page<BizAccountFlowVo> result = baseMapper.selectAdminVoPage(pageQuery.build(), lqw);

        return TableDataInfo.build(result);
    }

    @Override
    public List<BizAccountFlowVo> queryUserAccountFlow(BizAccountFlowBo bo) {
        Long currentLoginId = LoginHelper.getUserId();
        if (!currentLoginId.equals(bo.getUserId())) {
            throw new RuntimeException("非法请求：您无权查看他人的资金账户");
        }

        LambdaQueryWrapper<BizAccountFlow> lqw = buildUserQueryWrapper(bo);

        return TenantHelper.ignore(() ->
            DataPermissionHelper.ignore(() ->
                baseMapper.selectVoList(lqw)
            )
        );
    }

    private LambdaQueryWrapper<BizAccountFlow> buildUserQueryWrapper(BizAccountFlowBo bo) {
        LambdaQueryWrapper<BizAccountFlow> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BizAccountFlow::getCreateTime);
        lqw.eq(bo.getUserId() != null, BizAccountFlow::getUserId, bo.getUserId());
        return lqw;
    }
}
