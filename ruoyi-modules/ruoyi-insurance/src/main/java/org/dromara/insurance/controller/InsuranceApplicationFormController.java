package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.insurance.service.InsuranceApplicationFormService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequiredArgsConstructor
@SaCheckLogin
@RequestMapping("/insurance/InsuranceApplyRecord/applicationForm")
public class InsuranceApplicationFormController {
    private final InsuranceApplicationFormService service;
    @GetMapping("/templates")
    public R<?> templates(@RequestParam(required=false) String code,@RequestParam(required=false) String version){
        if(code==null&&version==null)return R.ok(service.metadata());
        if(code==null||code.isBlank()||version==null||version.isBlank())throw new ServiceException("投保单模板编码和版本不能为空");
        return R.ok(service.metadata(code,version));
    }
    @GetMapping("/{orderNo}")
    public R<?> status(@PathVariable String orderNo){return R.ok(service.status(service.readable(orderNo)));}
    @PostMapping("/{orderNo}/prepare")
    public R<?> prepare(@PathVariable String orderNo){service.readable(orderNo);return R.ok(service.prepare(orderNo));}
    @GetMapping("/{orderNo}/preview")
    public void preview(@PathVariable String orderNo,@RequestParam Long documentId,HttpServletResponse response)throws IOException{
        service.readable(orderNo);send(response,service.preview(orderNo,documentId),"application/pdf",orderNo+"_待签署投保单.pdf",true);
    }
    @GetMapping("/{orderNo}/previewInfo")
    public R<?> previewInfo(@PathVariable String orderNo,@RequestParam Long documentId)throws IOException {
        service.readable(orderNo);return R.ok(Map.of("pageCount",service.pageCount(orderNo,documentId)));
    }
    @GetMapping("/{orderNo}/previewPage")
    public void previewPage(@PathVariable String orderNo,@RequestParam Long documentId,@RequestParam int page,HttpServletResponse response)throws IOException {
        service.readable(orderNo);send(response,service.page(orderNo,documentId,page),"image/png","page.png",true);
    }
    @GetMapping("/{orderNo}/input")
    public R<?> input(@PathVariable String orderNo) { return R.ok(service.input(service.readable(orderNo))); }
    @PostMapping("/{orderNo}/sign")
    public R<?> sign(@PathVariable String orderNo,@RequestParam Long documentId,@RequestParam String snapshotHash,
        @RequestParam boolean specialConfirmed,@RequestParam boolean applicantConfirmed,@RequestParam boolean insuredConfirmed,
        @RequestPart MultipartFile special,@RequestPart MultipartFile applicant,@RequestPart MultipartFile insured,
        HttpServletRequest request)throws IOException{
        service.readable(orderNo);
        for(var file:List.of(special,applicant,insured))if(file.getSize()>1024*1024)throw new ServiceException("单张签字图片不得超过1MB");
        return R.ok(service.sign(orderNo,documentId,snapshotHash,
            Map.of("special",special.getBytes(),"applicant",applicant.getBytes(),"insured",insured.getBytes()),
            specialConfirmed&&applicantConfirmed&&insuredConfirmed,request.getRemoteAddr(),request.getHeader("User-Agent")));
    }
    @PostMapping("/{orderNo}/retry")
    public R<?> retry(@PathVariable String orderNo,@RequestParam Long documentId){service.readable(orderNo);return R.ok(service.generate(orderNo,documentId));}
    private void send(HttpServletResponse response,byte[] bytes,String type,String name,boolean inline)throws IOException{
        response.setContentType(type);response.setHeader("Cache-Control","no-store");
        response.setHeader("Content-Disposition",(inline?"inline":"attachment")+"; filename*=UTF-8''"+URLEncoder.encode(name,StandardCharsets.UTF_8).replace("+","%20"));
        response.setContentLength(bytes.length);response.getOutputStream().write(bytes);
    }
}
