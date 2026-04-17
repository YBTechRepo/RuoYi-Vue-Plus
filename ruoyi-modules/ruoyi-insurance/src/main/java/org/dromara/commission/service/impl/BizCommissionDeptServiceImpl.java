package org.dromara.commission.service.impl;

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
import org.dromara.commission.domain.bo.BizCommissionDeptBo;
import org.dromara.commission.domain.vo.BizCommissionDeptVo;
import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.mapper.BizCommissionDeptMapper;
import org.dromara.commission.service.IBizCommissionDeptService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 机构费率配置Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizCommissionDeptServiceImpl implements IBizCommissionDeptService {

    private final BizCommissionDeptMapper baseMapper;

    /**
     * 查询机构费率配置
     *
     * @param id 主键
     * @return 机构费率配置
     */
    @Override
    public BizCommissionDeptVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询机构费率配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 机构费率配置分页列表
     */
    @Override
    public TableDataInfo<BizCommissionDeptVo> queryPageList(BizCommissionDeptBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizCommissionDept> lqw = buildQueryWrapper(bo);
        Page<BizCommissionDeptVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的机构费率配置列表
     *
     * @param bo 查询条件
     * @return 机构费率配置列表
     */
    @Override
    public List<BizCommissionDeptVo> queryList(BizCommissionDeptBo bo) {
        LambdaQueryWrapper<BizCommissionDept> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizCommissionDept> buildQueryWrapper(BizCommissionDeptBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizCommissionDept> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BizCommissionDept::getId);
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), BizCommissionDept::getDeptName, bo.getDeptName());
        lqw.eq(bo.getEffectiveStart() != null, BizCommissionDept::getEffectiveStart, bo.getEffectiveStart());
        lqw.eq(bo.getEffectiveEnd() != null, BizCommissionDept::getEffectiveEnd, bo.getEffectiveEnd());
        lqw.eq(bo.getStatus() != null, BizCommissionDept::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增机构费率配置
     *
     * @param bo 机构费率配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizCommissionDeptBo bo) {
        BizCommissionDept add = MapstructUtils.convert(bo, BizCommissionDept.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改机构费率配置
     *
     * @param bo 机构费率配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizCommissionDeptBo bo) {
        BizCommissionDept update = MapstructUtils.convert(bo, BizCommissionDept.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizCommissionDept entity){
        if (entity.getDeptId() == null) {
            throw new org.dromara.common.core.exception.ServiceException("机构部门不能为空！");
        }
        if (entity.getEffectiveStart() == null) {
            throw new org.dromara.common.core.exception.ServiceException("生效开始时间不能为空！");
        }
        if (entity.getEffectiveEnd() != null && entity.getEffectiveStart().after(entity.getEffectiveEnd())) {
            throw new org.dromara.common.core.exception.ServiceException("生效开始时间不能晚于结束时间！");
        }

        // 查询数据库中该部门已有的费率配置 (排除自己)
        LambdaQueryWrapper<BizCommissionDept> lqw = Wrappers.lambdaQuery();
        lqw.eq(BizCommissionDept::getDeptId, entity.getDeptId());
        if (entity.getId() != null) {
            lqw.ne(BizCommissionDept::getId, entity.getId());
        }
        
        List<BizCommissionDept> existList = baseMapper.selectList(lqw);
        
        Date newStart = entity.getEffectiveStart();
        Date newEnd = entity.getEffectiveEnd();
        
        for (BizCommissionDept exist : existList) {
            Date existStart = exist.getEffectiveStart();
            Date existEnd = exist.getEffectiveEnd();
            
            // 判断两个时间段 [newStart, newEnd] 和 [existStart, existEnd] 是否存在重叠
            // 不重叠的条件是：一个的结束时间 <= 另一个的开始时间
            boolean isNotOverlap = false;
            
            // 情况1：新配置的结束时间早于(或等于)已有配置的开始时间
            if (newEnd != null && existStart != null && !newEnd.after(existStart)) {
                isNotOverlap = true;
            }
            // 情况2：已有配置的结束时间早于(或等于)新配置的开始时间
            else if (existEnd != null && newStart != null && !existEnd.after(newStart)) {
                isNotOverlap = true;
            }
            
            if (!isNotOverlap) {
                throw new org.dromara.common.core.exception.ServiceException("该机构在该时间段内已存在费率配置，请检查生效时间，避免时间段重叠！");
            }
        }
    }

    /**
     * 校验并批量删除机构费率配置信息
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
    public BizCommissionDept queryByDeptIdAndTenantId(Long deptId, String tenantId) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(BizCommissionDept.class)
                .eq(BizCommissionDept::getDeptId, deptId)
                .eq(BizCommissionDept::getTenantId, tenantId));
    }

    @Override
    public BizCommissionDept queryByDeptId(Long deptId) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(BizCommissionDept.class)
            .eq(BizCommissionDept::getDeptId, deptId)
            .eq(BizCommissionDept::getStatus, 0)
        );
    }
}
