package org.dromara.insurance.service;

import org.dromara.insurance.domain.ResultModel;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.system.domain.vo.SysUserVo;

public interface IOpenPolicyFacadeService {
    ResultModel processCallback(PolicyCallbackDto policyCallbackDto);

    SysUserVo getUserByUserId(Long userId);
}
