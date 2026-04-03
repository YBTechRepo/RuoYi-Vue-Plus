package org.dromara.finance.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.finance.domain.BizRechargeRecord;
import org.dromara.finance.domain.vo.BizRechargeRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 充值申请Mapper接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface BizRechargeRecordMapper extends BaseMapperPlus<BizRechargeRecord, BizRechargeRecordVo> {

    /**
     * 【财务后台专用】无视多租户，查全盘数据
     * 这里的 ${ew.customSqlSegment} 会自动替换为 Service 层传来的 Wrapper 条件！
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM biz_recharge_record ${ew.customSqlSegment}")
    Page<BizRechargeRecordVo> selectAdminVoPage(@Param("page") Page<BizRechargeRecord> page, @Param(Constants.WRAPPER) Wrapper<BizRechargeRecord> queryWrapper);
}
