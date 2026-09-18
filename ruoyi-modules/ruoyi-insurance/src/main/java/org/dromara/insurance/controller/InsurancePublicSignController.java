package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.insurance.service.InsurancePublicSignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@SaIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/open/api/v1/application-sign")
public class InsurancePublicSignController {
    private final InsurancePublicSignService service;

    @GetMapping
    public R<?> resolve(@RequestParam String token) {
        return R.ok(service.resolve(token));
    }

    @PostMapping("/sign")
    public R<?> sign(@RequestParam String token,
                     @RequestPart(required = false) MultipartFile special,
                     @RequestPart(required = false) MultipartFile applicant,
                     @RequestPart(required = false) MultipartFile insured,
                     HttpServletRequest request) throws IOException {
        Map<String, byte[]> images = new LinkedHashMap<>();
        if (special != null) images.put("special", special.getBytes());
        if (applicant != null) images.put("applicant", applicant.getBytes());
        if (insured != null) images.put("insured", insured.getBytes());
        return R.ok(service.sign(token, images, request.getRemoteAddr(), request.getHeader("User-Agent")));
    }
}
