package org.dromara.commission.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.commission.domain.BizCommissionRecord;
import org.dromara.commission.domain.bo.AppCommissionQueryBo;
import org.dromara.commission.domain.vo.AppCommissionItemVo;
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.domain.vo.CommissionSummaryVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 佣金分配明细Mapper接口
 *
 * @author li.xiang
 * @date 2026-03-11
 */
public interface BizCommissionRecordMapper extends BaseMapperPlus<BizCommissionRecord, BizCommissionRecordVo> {
    /**
     * 1. APP端：获取佣金头部汇总看板数据
     */
    CommissionSummaryVo getCommissionSummary(@Param("userId") Long userId,
                                             @Param("queryMonth") String queryMonth);

    /**
     * 2. APP端：获取佣金明细分页列表
     */
    Page<AppCommissionItemVo> queryAppCommissionList(Page<AppCommissionItemVo> page,
                                                     @Param("bo") AppCommissionQueryBo bo,
                                                     @Param("userId") Long userId);
}
