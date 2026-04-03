package org.dromara.finance.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.finance.domain.BizUserAccount;
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 账户信息Mapper接口
 *
 * @author li.xiang
 * @date 2026-03-27
 */
public interface BizUserAccountMapper extends BaseMapperPlus<BizUserAccount, BizUserAccountVo> {
    /**
     * 【财务总后台专用】忽略租户隔离，查询全量用户资金账户
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM biz_user_account ${ew.customSqlSegment}")
    Page<BizUserAccountVo> selectAdminVoPage(@Param("page") Page<BizUserAccount> page, @Param(Constants.WRAPPER) Wrapper<BizUserAccount> queryWrapper);


    /**
     * 【财务总后台专用】忽略租户隔离，查询全量用户资金账户列表（不分页，主要用于导出Excel）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM biz_user_account ${ew.customSqlSegment}")
    List<BizUserAccountVo> selectAdminVoList(@Param(Constants.WRAPPER) Wrapper<BizUserAccount> queryWrapper);
}
