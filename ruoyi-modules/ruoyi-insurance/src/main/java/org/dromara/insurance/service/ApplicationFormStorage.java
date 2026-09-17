package org.dromara.insurance.service;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.factory.OssFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

@Component
public class ApplicationFormStorage {
    @Value("${insurance.application-form.oss-config:}")
    private String configKey;
    public String config() {
        if (configKey == null || configKey.isBlank()) throw new ServiceException("请配置投保单存储 insurance.application-form.oss-config");
        client(configKey); return configKey;
    }
    private OssClient client(String config) {
        return OssFactory.instance(config);
    }
    public void put(String config,String key,byte[] data,String type) {
        client(config).upload(new ByteArrayInputStream(data),key,(long)data.length,type);
    }
    public byte[] get(String config,String key) {
        if(key==null||!key.startsWith("insurance-applications/"))throw new ServiceException("无效的投保单文件");
        var out=new ByteArrayOutputStream();
        client(config).download(key,out,length->{if(length>20*1024*1024)throw new ServiceException("投保单文件超过20MB限制");});
        return out.toByteArray();
    }

    public void putExport(String config, String key, Path file) {
        assertExportKey(key);
        client(config).upload(file, key, null, "application/zip");
    }

    public void writeExport(String config, String key, OutputStream out, long maxBytes) {
        assertExportKey(key);
        client(config).download(key, out, length -> {
            if (length > maxBytes) throw new ServiceException("投保单批量导出文件超过限制");
        });
    }

    public void deleteExport(String config, String key) {
        assertExportKey(key);
        client(config).delete(key);
    }

    private void assertExportKey(String key) {
        if (key == null || !key.startsWith("insurance-application-exports/")) {
            throw new ServiceException("无效的投保单批量导出文件");
        }
    }
}
