package org.dromara.finance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.finance.domain.*;
import org.dromara.finance.mapper.BizAccountFlowMapper;
import org.dromara.finance.mapper.BizUserAccountMapper;
import org.dromara.system.domain.SysOss;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.dromara.finance.domain.bo.BizRechargeRecordBo;
import org.dromara.finance.domain.vo.BizRechargeRecordVo;
import org.dromara.finance.mapper.BizRechargeRecordMapper;
import org.dromara.finance.service.IBizRechargeRecordService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 充值申请Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizRechargeRecordServiceImpl implements IBizRechargeRecordService {

    private final ISysUserService sysUserService;

    private final BizRechargeRecordMapper baseMapper;

    private final BizUserAccountMapper userAccountMapper;

    private final BizAccountFlowMapper accountFlowMapper;

    /**
     * 查询充值申请
     *
     * @param id 主键
     * @return 充值申请
     */
    @Override
    public BizRechargeRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询充值申请列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 充值申请分页列表
     */
    @Override
    public TableDataInfo<BizRechargeRecordVo> queryPageList(BizRechargeRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizRechargeRecord> lqw = buildQueryWrapper(bo);
        Page<BizRechargeRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的充值申请列表
     *
     * @param bo 查询条件
     * @return 充值申请列表
     */
    @Override
    public List<BizRechargeRecordVo> queryList(BizRechargeRecordBo bo) {
        LambdaQueryWrapper<BizRechargeRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizRechargeRecord> buildQueryWrapper(BizRechargeRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizRechargeRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BizRechargeRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getRechargeNo()), BizRechargeRecord::getRechargeNo, bo.getRechargeNo());
        lqw.eq(bo.getUserId() != null, BizRechargeRecord::getUserId, bo.getUserId());
        lqw.eq(bo.getStatus() != null, BizRechargeRecord::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增充值申请
     *
     * @param bo 充值申请
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizRechargeRecordBo bo) {
        BizRechargeRecord add = MapstructUtils.convert(bo, BizRechargeRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改充值申请
     *
     * @param bo 充值申请
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizRechargeRecordBo bo) {
        BizRechargeRecord update = MapstructUtils.convert(bo, BizRechargeRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizRechargeRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除充值申请信息
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyRecharge(RechargeApplyReqDTO rechargeApplyReqDTO) {
        // 1. 兜底校验（虽然 Controller 层应该加了 @Validated，这里做个双保险）
        if (rechargeApplyReqDTO.getApplyAmount() == null || rechargeApplyReqDTO.getApplyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0"); // 建议换成您项目的全局业务异常类
        }

        // 2. 从 Sa-Token 获取当前登录的真实用户 ID
        String rawLoginId = StpUtil.getLoginIdAsString();
        String idStr = rawLoginId.contains(":") ? rawLoginId.substring(rawLoginId.lastIndexOf(":") + 1) : rawLoginId;
        Long currentUserId = Long.valueOf(idStr);

        // 3. 后端生成全局唯一的充值单号 (防重复、防篡改)
        // 格式建议：CZ + 年月日时分秒 + 4位随机数 (例如: CZ202603281430001234)
        // 或者直接使用雪花算法 IdUtil.getSnowflakeNextIdStr()
        String rechargeNo = "CZ" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);

        // 获取用户详细信息 (增加防空校验)
        SysUserVo sysUserVo = sysUserService.selectUserById(currentUserId);
        if (sysUserVo == null) {
            log.error("充值异常: 找不到ID为 [{}] 的用户信息", currentUserId);
            throw new RuntimeException("获取用户信息失败，请重新登录");
        }

        // 4. 构建并组装入库实体
        BizRechargeRecord record = new BizRechargeRecord();

        // --- 核心业务数据 (来自前端) ---
        record.setApplyAmount(rechargeApplyReqDTO.getApplyAmount());
        record.setVoucherImg(rechargeApplyReqDTO.getVoucherImg());

        // --- 系统强制控制数据 (绝对安全) ---
        record.setUserId(currentUserId);      // 强绑定当前用户
        record.setUserName(sysUserVo.getUserName());
        record.setUserNickName(sysUserVo.getNickName());
        record.setRechargeNo(rechargeNo);     // 后端生成单号
        record.setStatus(0);                  // 强制初始状态为：0-待审核

        // 5. 执行落库保存
        log.info("用户 [{}] 提交充值申请，单号: {}", currentUserId, rechargeNo);
        int rows = baseMapper.insert(record);
        return rows > 0;
    }

    @Override
    public TableDataInfo<BizRechargeRecordVo> queryAdminPageList(BizRechargeRecordBo bo, PageQuery pageQuery) {
        // 1. 调用专用的条件构造器
        LambdaQueryWrapper<BizRechargeRecord> lqw = buildAdminQueryWrapper(bo);

        // 2. 调用我们在 Mapper 中写的“上帝视角”查询方法
        Page<BizRechargeRecordVo> result = baseMapper.selectAdminVoPage(pageQuery.build(), lqw);

        return TableDataInfo.build(result);
    }

    @Override
    public List<BizRechargeRecordVo> queryAdminList(BizRechargeRecordBo bo) {
        LambdaQueryWrapper<BizRechargeRecord> lqw = buildAdminQueryWrapper(bo);
        return baseMapper.selectAdminVoList(lqw);
    }

    @Override
    public BizRechargeRecordVo queryAdminById(Long id) {
        return TenantHelper.ignore(() -> baseMapper.selectVoById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditRecharge(RechargeAuditReqDTO reqDTO) {
        // 1. 修改充值记录状态 (沿用我们之前写的安全改状态逻辑)
        // ------------------------------------------------------
        BizRechargeRecord oldRecord = TenantHelper.ignore(() -> baseMapper.selectById(reqDTO.getId()));
        if (oldRecord == null || oldRecord.getStatus() != 0) {
            throw new RuntimeException("单据状态异常，请刷新后重试");
        }

        String rawLoginId = StpUtil.getLoginIdAsString();
        String idStr = rawLoginId.contains(":") ? rawLoginId.substring(rawLoginId.lastIndexOf(":") + 1) : rawLoginId;
        Long auditorId = Long.valueOf(idStr);

        BigDecimal actualAmount = reqDTO.getActualAmount() != null ? reqDTO.getActualAmount() : oldRecord.getApplyAmount();

        // 更新充值单状态为：已通过(1) 或 驳回(2)
        BizRechargeRecord updateRecord = new BizRechargeRecord();
        updateRecord.setId(oldRecord.getId());
        updateRecord.setStatus(reqDTO.getAuditStatus());
        updateRecord.setActualAmount(actualAmount);
        updateRecord.setAuditRemark(reqDTO.getAuditRemark());
        updateRecord.setAuditBy(auditorId);
        updateRecord.setAuditTime(new Date());

        // 安全更新：只有状态为 0 时才允许改
        boolean updateStatusOk = TenantHelper.ignore(() -> baseMapper.update(updateRecord,
            Wrappers.<BizRechargeRecord>lambdaUpdate()
                .eq(BizRechargeRecord::getId, oldRecord.getId())
                .eq(BizRechargeRecord::getStatus, 0))) > 0;

        if (!updateStatusOk) {
            throw new RuntimeException("操作失败，单据可能已被处理");
        }

        // 2. 如果审核通过，进入核心“加钱”环节
        // ------------------------------------------------------
        if (reqDTO.getAuditStatus() == 1) {
            // A. 获取用户钱包 (注意：这里要跨租户获取用户的钱包，因为财务和用户可能不在一个租户)
            BizUserAccount account = TenantHelper.ignore(() ->
                userAccountMapper.selectOne(Wrappers.<BizUserAccount>lambdaQuery()
                    .eq(BizUserAccount::getUserId, oldRecord.getUserId()))
            );

            if (account == null) {
                throw new RuntimeException("该用户尚未开通资金账户，请联系技术人员");
            }

            BigDecimal beforeBalance = account.getBalance(); // 变动前余额
            BigDecimal afterBalance = beforeBalance.add(actualAmount); // 预期变动后余额

            // B. 执行乐观锁更新：SQL 会自动带上 WHERE version = ?
            // 如果更新返回 0，说明在这一秒内有其他业务（如另一笔充值或消费）修改了余额
            account.setBalance(afterBalance);
            // 注意：MyBatis-Plus 开启乐观锁插件后，updateById 会自动处理 version + 1
            boolean updateAccountOk = TenantHelper.ignore(() -> userAccountMapper.updateById(account)) > 0;

            if (!updateAccountOk) {
                throw new RuntimeException("资金账户繁忙，请稍后重新点击审核");
            }

            // C. 记录资金动账流水 (biz_account_flow)
            // ------------------------------------------------------
            BizAccountFlow flow = new BizAccountFlow();
            flow.setUserId(oldRecord.getUserId());
            flow.setUserName(oldRecord.getUserName());
            flow.setUserNickName(oldRecord.getUserNickName());
            flow.setFlowType(1); // 业务类型：充值
            flow.setBizNo(oldRecord.getRechargeNo()); // 关联充值单号
            flow.setAmount(actualAmount); // 变动金额
            flow.setBalanceBefore(beforeBalance); // 变动前
            flow.setBalanceAfter(afterBalance);   // 变动后
            flow.setRemark("在线充值成功："+oldRecord.getRechargeNo());

            TenantHelper.ignore(() -> accountFlowMapper.insert(flow));

            log.info("用户 [{}] 充值成功，金额: {}, 账户余额从 {} 变为 {}",
                oldRecord.getUserId(), actualAmount, beforeBalance, afterBalance);
        }
        return true;
    }

    /**
     * 专供财务管理员的条件构造器
     */
    private LambdaQueryWrapper<BizRechargeRecord> buildAdminQueryWrapper(BizRechargeRecordBo bo) {
        LambdaQueryWrapper<BizRechargeRecord> lqw = Wrappers.lambdaQuery();

        // 🌟 优化1：财务审核必须是“最新提交的排在最前面”
        lqw.orderByDesc(BizRechargeRecord::getCreateTime);

        // 基础等值查询
        lqw.eq(StringUtils.isNotBlank(bo.getRechargeNo()), BizRechargeRecord::getRechargeNo, bo.getRechargeNo());
        lqw.eq(bo.getUserId() != null, BizRechargeRecord::getUserId, bo.getUserId());
        lqw.eq(bo.getStatus() != null, BizRechargeRecord::getStatus, bo.getStatus());

        // 🌟 优化2：若依标准的“时间范围查询”处理方式
        Map<String, Object> params = bo.getParams();
        if (params != null && params.get("beginTime") != null && params.get("endTime") != null) {
            lqw.between(BizRechargeRecord::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }

        return lqw;
    }
}
