package org.dromara.insurance.service.impl;

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
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.service.IInsuranceApplyRecordService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 投保记录Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceApplyRecordServiceImpl implements IInsuranceApplyRecordService {

    private final InsuranceApplyRecordMapper baseMapper;

    /**
     * 查询投保记录
     *
     * @param id 主键
     * @return 投保记录
     */
    @Override
    public InsuranceApplyRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询投保记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 投保记录分页列表
     */
    @Override
    public TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的投保记录列表
     *
     * @param bo 查询条件
     * @return 投保记录列表
     */
    @Override
    public List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo) {
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<InsuranceApplyRecord> buildQueryWrapper(InsuranceApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
        lqw.eq(bo.getStatus() != null, InsuranceApplyRecord::getStatus, bo.getStatus());
        lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());
        return lqw;
    }

    /**
     * 新增投保记录
     *
     * @param bo 投保记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord add = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改投保记录
     *
     * @param bo 投保记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord update = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceApplyRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除投保记录信息
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
