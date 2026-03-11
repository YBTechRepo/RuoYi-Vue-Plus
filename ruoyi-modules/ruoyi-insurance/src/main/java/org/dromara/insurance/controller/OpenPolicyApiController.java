package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.config.properties.OpenApiProperties;
import org.dromara.insurance.domain.ResultModel;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.insurance.domain.dto.PolicyCallbackReqDto;
import org.dromara.insurance.service.IOpenPolicyFacadeService;
import org.dromara.insurance.utils.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/open/api/v1/policy")
@RequiredArgsConstructor
public class OpenPolicyApiController {

    private final OpenApiProperties openApiProperties;

    private final ObjectMapper objectMapper;

    private final IOpenPolicyFacadeService openPolicyFacadeService;

    @SaIgnore
    @PostMapping("/underwrite/callback")
    public R<Void> policyCallback(@RequestBody PolicyCallbackReqDto policyCallbackReqDto){
        // 参数校验
        if (policyCallbackReqDto == null) {
            log.error("请求参数为空");
            return R.fail("请求参数为空");
        }

        String content = policyCallbackReqDto.getContent();
        String sign = policyCallbackReqDto.getSign();

        if (content == null || content.trim().isEmpty()) {
            log.error("加密内容为空");
            return R.fail("加密内容为空");
        }

        if (sign == null || sign.trim().isEmpty()) {
            log.error("签名为空");
            return R.fail("签名为空");
        }

        // 解密
        String decryptContent;
        try {
            decryptContent = RSAUtil.decryptByPrivateKey(content, openApiProperties.getPrivateKey(), "UTF-8");
        } catch (Exception e) {
            log.error("解密异常，content: {}", content.substring(0, Math.min(50, content.length())), e);
            return R.fail("解密异常");
        }

        // 验证签名
        try {
            String encryptContent = RSAUtil.encryptByPublicKey(decryptContent, openApiProperties.getPublicKey(), "UTF-8");
            String calculatedSign = RSAUtil.generateSign(encryptContent, openApiProperties.getAppCode());
            if (!calculatedSign.equals(sign)) {
                log.error("签名验证失败，期望：{}, 实际：{}", calculatedSign, sign);
                return R.fail("签名验证失败");
            }
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return R.fail("签名验证异常");
        }

        // decryptContent 转换为 PolicyCallbackDto
        PolicyCallbackDto policyCallbackDto;
        try {
            policyCallbackDto = objectMapper.readValue(decryptContent, PolicyCallbackDto.class);
        } catch (JsonProcessingException e) {
            log.error("PolicyCallbackDto 对象转换异常，decryptContent: {}", decryptContent, e);
            return R.fail("数据格式错误");
        }

        // 保存保单并发布异步事件
        try {
            // 这里会执行：1.存保单 2.发布 PolicyUnderwrittenEvent
            ResultModel resultModel = openPolicyFacadeService.processCallback(policyCallbackDto);

            if (resultModel.getCode() != 0) {
                return R.fail(resultModel.getMsg());
            }
        } catch (ServiceException e) {
            // 捕获抛出的业务异常（如产品不存在、业务员不存在等）
            log.warn("【业务拦截】保单处理未成功: {}", e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            // 兜底系统异常
            log.error("【系统异常】保单入库过程崩溃", e);
            return R.fail("系统繁忙，请稍后重试");
        }


        log.info("【回调成功】保单已入库，单号: {}", policyCallbackDto.getPolicyDto().getPolicyNo());
        return R.ok();
    }

    @SaIgnore
    @GetMapping("/getUserInfo")
    public Object getUserInfo(@RequestParam Long userId){
        return openPolicyFacadeService.getUserByUserId(userId);
    }
}
