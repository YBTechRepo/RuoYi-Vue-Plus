package org.dromara.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.domain.model.MobileLoginBody;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 手机号认证策略
 *
 * @author Lion Li
 */
@Slf4j
@Service("mobile" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class MobileAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final SysUserMapper userMapper;

    @Override
    public LoginVo login(String body, SysClientVo client) {
        // 1. 解析参数
        MobileLoginBody loginBody = JsonUtils.parseObject(body, MobileLoginBody.class);
        ValidatorUtils.validate(loginBody);

        String phone = loginBody.getPhoneNumber();
        String password = loginBody.getPassword();

        // 2. 根据手机号查询用户（手机号全局唯一）
        SysUserVo user = loadUserByPhone(phone);

        // 3. 动态切换到用户所在租户
        String tenantId = user.getTenantId();
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            // 4. 密码校验（带失败次数限制）
            loginService.checkLogin(
                LoginType.PASSWORD,
                tenantId,
                phone,
                () -> !BCrypt.checkpw(password, user.getPassword())
            );

            // 5. 构建登录用户对象
            return loginService.buildLoginUser(user);
        });

        // 6. 设置客户端信息
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());

        // 7. 配置 Sa-Token 登录参数
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType());
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());

        // 8. 生成 Token 并登录
        LoginHelper.login(loginUser, model);

        // 9. 返回登录凭证
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());

        log.info("手机号 {} 登录成功，用户 ID: {}, 租户 ID: {}", phone, user.getUserId(), tenantId);
        return loginVo;
    }

    /**
     * 根据手机号加载用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    private SysUserVo loadUserByPhone(String phone) {
        // 手机号全局唯一，直接查询
        SysUserVo user = userMapper.selectVoOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhonenumber, phone)
        );

        if (ObjectUtil.isNull(user)) {
            log.info("手机号 {} 不存在", phone);
            throw new UserException("user.phone.not.exists");
        }

        // 检查用户状态
        if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("手机号 {} 对应的用户已被停用", phone);
            throw new UserException("user.blocked", phone);
        }

        return user;
    }
}
