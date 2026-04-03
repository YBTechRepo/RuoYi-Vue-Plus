package org.dromara.finance.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.finance.domain.BizAccountFlow;
import org.dromara.finance.domain.BizRechargeRecord;
import org.dromara.finance.domain.vo.BizAccountFlowVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.finance.domain.vo.BizRechargeRecordVo;

/**
 * 账户明细Mapper接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface BizAccountFlowMapper extends BaseMapperPlus<BizAccountFlow, BizAccountFlowVo> {

    /**
     * 【管理员专用】忽略租户隔离，查询全量资金流水
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM biz_account_flow ${ew.customSqlSegment}")
    Page<BizAccountFlowVo> selectAdminVoPage(@Param("page") Page<BizAccountFlow> page, @Param(Constants.WRAPPER) Wrapper<BizAccountFlow> queryWrapper);}
