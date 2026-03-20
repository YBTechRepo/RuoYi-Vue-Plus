package org.dromara.commission.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open/api/v1/sales")
@Slf4j
public class SalesProductController {

    private final IInsuranceProductConfigService insuranceProductConfigService;

    /**
     * 查询销售端产品列表 (带最终费率)
     */
    @SaCheckLogin // 销售页通常只需校验登录，无需校验菜单权限。如果是公开页面可换成 @SaIgnore
    @GetMapping("/list")
    public TableDataInfo<InsuranceSalesProductVo> list(InsuranceProductConfigBo bo, PageQuery pageQuery,String userId) {
        return insuranceProductConfigService.querySalesPageList(bo, pageQuery);
    }
}
