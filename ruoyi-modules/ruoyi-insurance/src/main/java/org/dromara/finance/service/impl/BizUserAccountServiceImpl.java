package org.dromara.finance.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
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
import org.dromara.finance.domain.AccountAdjustReqDTO;
import org.dromara.finance.domain.BizAccountFlow;
import org.dromara.finance.mapper.BizAccountFlowMapper;
import org.springframework.stereotype.Service;
import org.dromara.finance.domain.bo.BizUserAccountBo;
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.finance.domain.BizUserAccount;
import org.dromara.finance.mapper.BizUserAccountMapper;
import org.dromara.finance.service.IBizUserAccountService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 账户信息Service业务层处理
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserAccountServiceImpl implements IBizUserAccountService {

    private final BizUserAccountMapper baseMapper;

    private final BizAccountFlowMapper accountFlowMapper;

    /**
     * 查询账户信息
     *
     * @param userId 主键
     * @return 账户信息
     */
    @Override
    public BizUserAccountVo queryById(Long userId){
        // 既然是给用户自己用的，我们必须保证他传进来的 userId 和他当前登录的 Token 身份是一致的。
        Long currentLoginId = LoginHelper.getUserId();
        if (!currentLoginId.equals(userId)) {
            throw new RuntimeException("非法请求：您无权查看他人的资金账户");
        }

        // 开启绝对上帝视角：嵌套使用 Helper，同时穿透【多租户隔离墙】和【数据权限隔离墙】
        return TenantHelper.ignore(() ->
            DataPermissionHelper.ignore(() ->
                baseMapper.selectVoById(userId)
            )
        );
    }

    /**
     * 分页查询账户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户信息分页列表
     */
    @Override
    public TableDataInfo<BizUserAccountVo> queryPageList(BizUserAccountBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizUserAccount> lqw = buildQueryWrapper(bo);
        Page<BizUserAccountVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的账户信息列表
     *
     * @param bo 查询条件
     * @return 账户信息列表
     */
    @Override
    public List<BizUserAccountVo> queryList(BizUserAccountBo bo) {
        LambdaQueryWrapper<BizUserAccount> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BizUserAccount> buildQueryWrapper(BizUserAccountBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BizUserAccount> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BizUserAccount::getUserId);
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), BizUserAccount::getUserName, bo.getUserName());
        lqw.like(StringUtils.isNotBlank(bo.getUserNickName()), BizUserAccount::getUserNickName, bo.getUserNickName());
        lqw.eq(bo.getBalance() != null, BizUserAccount::getBalance, bo.getBalance());
        lqw.eq(bo.getStatus() != null, BizUserAccount::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增账户信息
     *
     * @param bo 账户信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BizUserAccountBo bo) {
        BizUserAccount add = MapstructUtils.convert(bo, BizUserAccount.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setUserId(add.getUserId());
        }
        return flag;
    }

    /**
     * 修改账户信息
     *
     * @param bo 账户信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BizUserAccountBo bo) {
        BizUserAccount update = MapstructUtils.convert(bo, BizUserAccount.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BizUserAccount entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除账户信息信息
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
    public TableDataInfo<BizUserAccountVo> queryAdminPageList(BizUserAccountBo bo, PageQuery pageQuery) {
        // 1. 调用专为管理员优化的查询条件构造器
        LambdaQueryWrapper<BizUserAccount> lqw = buildAdminQueryWrapper(bo);

        // 2. 调用 Mapper 中的上帝视角查询方法
        Page<BizUserAccountVo> result = baseMapper.selectAdminVoPage(pageQuery.build(), lqw);

        return TableDataInfo.build(result);
    }

    @Override
    public List<BizUserAccountVo> queryAdminList(BizUserAccountBo bo) {
        // 1. 直接复用上一轮写好的管理员专用查询条件 (自动带上 update_time 倒序)
        LambdaQueryWrapper<BizUserAccount> lqw = buildAdminQueryWrapper(bo);

        // 2. 调用 Mapper 中的上帝视角 List 查询方法
        return baseMapper.selectAdminVoList(lqw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustBalance(AccountAdjustReqDTO reqDTO) {
        // 1. 校验流水类型是否合法
        if (reqDTO.getFlowType() != 4 && reqDTO.getFlowType() != 5) {
            throw new RuntimeException("非法的调账类型");
        }

        // 2. 开启上帝视角，跨租户查询目标用户的钱包
        BizUserAccount account = TenantHelper.ignore(() ->
            baseMapper.selectOne(Wrappers.<BizUserAccount>lambdaQuery()
                .eq(BizUserAccount::getUserId, reqDTO.getUserId()))
        );

        if (account == null) {
            throw new RuntimeException("该用户尚未开通资金账户，无法进行调账");
        }

        BigDecimal beforeBalance = account.getBalance();
        BigDecimal amount = reqDTO.getAmount();
        BigDecimal afterBalance;

        // 3. 核心计算逻辑：加钱 or 扣钱
        if (reqDTO.getFlowType() == 4) {
            // 其他收入：直接加钱
            afterBalance = beforeBalance.add(amount);
        } else {
            // 其他支出：扣钱前必须校验余额是否充足
            if (beforeBalance.compareTo(amount) < 0) {
                throw new RuntimeException("调账失败：用户当前余额为 " + beforeBalance + " 元，不足以扣减 " + amount + " 元");
            }
            afterBalance = beforeBalance.subtract(amount);
        }

        // 4. 更新钱包余额（利用实体类自带的 @Version 乐观锁防并发）
        account.setBalance(afterBalance);
        boolean updateAccountOk = TenantHelper.ignore(() -> baseMapper.updateById(account)) > 0;

        if (!updateAccountOk) {
            throw new RuntimeException("账户资金正在发生变动，请稍后重新提交");
        }

        // 5. 沉淀资金动账流水
        BizAccountFlow flow = new BizAccountFlow();
        flow.setUserId(account.getUserId());
        flow.setUserName(account.getUserName());
        flow.setUserNickName(account.getUserNickName());
        flow.setFlowType(reqDTO.getFlowType());

        // 生成一个专属的“调账单号” (TZ + 时间戳 + 4位随机数)
        String bizNo = "TZ" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
        flow.setBizNo(bizNo);

        flow.setAmount(amount);
        flow.setBalanceBefore(beforeBalance);
        flow.setBalanceAfter(afterBalance);
        // 将前端填写的摘要保存下来
        flow.setRemark("【人工调账】" + reqDTO.getRemark());

        // 插入流水表
        TenantHelper.ignore(() -> accountFlowMapper.insert(flow));

        log.info("管理员对用户 [{}] 进行人工调账，类型: {}, 金额: {}, 余额变动: {} -> {}",
            account.getUserId(), reqDTO.getFlowType(), amount, beforeBalance, afterBalance);
    }

    /**
     * 订单出单专属扣费方法
     * * @param userId  扣款用户ID
     * @param orderNo 关联的业务单号 (applyNo 或 orderNo)
     * @param amount  扣款金额 (必须是后端从数据库查出来的真实金额)
     * @param remark  流水摘要 (如: "余额支付投保申请：P123")
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForOrder(Long userId, String orderNo, BigDecimal amount, String remark) {
        // ==========================================
        // 1. 查验钱包与余额
        // ==========================================
        BizUserAccount account = baseMapper.selectOne(Wrappers.<BizUserAccount>lambdaQuery()
            .eq(BizUserAccount::getUserId, userId));

        if (account == null) {
            throw new RuntimeException("您的资金账户未初始化，无法进行支付");
        }

        BigDecimal beforeBalance = account.getBalance();

        // 🛑 核心防线：余额不足直接抛异常，阻断外层的订单状态更新！
        if (beforeBalance.compareTo(amount) < 0) {
            throw new RuntimeException("余额不足！当前余额为 " + beforeBalance + " 元，需支付 " + amount + " 元");
        }

        BigDecimal afterBalance = beforeBalance.subtract(amount);

        // ==========================================
        // 2. 乐观锁安全扣减
        // ==========================================
        account.setBalance(afterBalance);
        // 注意：这里的 updateById 会自动触发 BizUserAccount 实体类里 @Version 注解的乐观锁机制
        // 底层执行的 SQL 是: UPDATE table SET balance = ?, version = version + 1 WHERE id = ? AND version = ?
        int rows = baseMapper.updateById(account);

        if (rows == 0) {
            // 如果返回 0，说明在这一毫秒内，别的线程把钱扣了，当前操作直接回滚
            throw new RuntimeException("系统繁忙，扣款失败，请重新尝试");
        }

        // ==========================================
        // 3. 沉淀消费流水
        // ==========================================
        BizAccountFlow flow = new BizAccountFlow();
        flow.setUserId(userId);
        flow.setUserName(account.getUserName());
        flow.setUserNickName(account.getUserNickName());

        // 💡 流水类型设为 2 (假设您的字典里 2 代表产品消费/出单支出)
        flow.setFlowType(2);

        // 💡 极度重要：把业务单号死死地绑定在流水表里，以后财务对账一目了然！
        flow.setBizNo(orderNo);

        flow.setAmount(amount);
        flow.setBalanceBefore(beforeBalance);
        flow.setBalanceAfter(afterBalance);
        flow.setRemark(remark);

        accountFlowMapper.insert(flow);

        log.info("出单扣款成功！用户ID: {}, 订单号: {}, 扣除金额: {}, 剩余余额: {}",
            userId, orderNo, amount, afterBalance);
    }

    /**
     * 专供财务管理员使用的条件构造器
     */
    private LambdaQueryWrapper<BizUserAccount> buildAdminQueryWrapper(BizUserAccountBo bo) {
        LambdaQueryWrapper<BizUserAccount> lqw = Wrappers.lambdaQuery();

        // 🌟 体验优化：财务通常更关心最近账户发生过变动的用户，因此按 update_time 倒序排列
        lqw.orderByDesc(BizUserAccount::getCreateTime);

        // 基础过滤条件保持不变
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), BizUserAccount::getUserName, bo.getUserName());
        lqw.like(StringUtils.isNotBlank(bo.getUserNickName()), BizUserAccount::getUserNickName, bo.getUserNickName());
        lqw.eq(bo.getBalance() != null, BizUserAccount::getBalance, bo.getBalance());
        lqw.eq(bo.getStatus() != null, BizUserAccount::getStatus, bo.getStatus());

        return lqw;
    }
}
