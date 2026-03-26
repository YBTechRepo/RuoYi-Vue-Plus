import org.dromara.common.json.utils.JsonUtils;
import org.dromara.insurance.utils.RSAUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestClass {

    @Test
    public void test01() throws Exception {
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCU+mHYt2evAYX53Z1D/KbUbD+y4D9J5ield+3EnkxvrY31LDX4oyk4qAnpyGX6xLDsoRZ9b8w5CVVrmrnqATGb6/QaFZLm/vPJXhpYk1Iwbm71W9q9KImG3XCHAeK+YFZr2GW284Zpr64O/5PTCc7Z+vAe0Q0dB3lcK3PML6GHlwIDAQAB";
        String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJT6Ydi3Z68BhfndnUP8ptRsP7LgP0nmJ6V37cSeTG+tjfUsNfijKTioCenIZfrEsOyhFn1vzDkJVWuaueoBMZvr9BoVkub+88leGliTUjBubvVb2r0oiYbdcIcB4r5gVmvYZbbzhmmvrg7/k9MJztn68B7RDR0HeVwrc8wvoYeXAgMBAAECgYAK1Em6vKSk51a+hOb/jN6KdqHv76mtRmjF+CB7y3v7HdUOjnVDF4bE3D/FBpdIvsJ6BcDb3Mp640+AkzTSqndZTBGqOAQxUdkhgW2pXFuIin7jUrQV7bSLkpZb34K7nGeQS9lCG6VMv2zWESL3xrbt7drnVFoQvNF5jAn+vmKTjQJBAM0vHvWKhM0/nwPbu0jL6NhkljUe95PRjF4vgYWcRemYeywnhfR+3EDpmBEN2pfmuWfvvCJmVuMNNGhPfyK/YTUCQQC5374tVQhkKRnG1WyDN8ettzegulR1aaFkqbnFXmHwwRgvcgjGA3wiHJ2wTH9gFkvP7sQKc5QwVH2/M1kw0QsbAkALGUZqb8C5PXzDf6pEyMrikaZSX0dzFn2qRpWR/Z8GgLoA7G5hiE8vr7MLER3G47MZvS0HC1Qd2gQAtHQjFAkVAkAa+7o2BoQjVjwhu1X3bp+UPmba9HHSYbVGUcVgO6VoN//0008mv9bDdPgHYfOT1WjIFNds1P6MTZe7rA6koZRFAkBncZwn1vD4SB/5P8UhTHoc1D3uQMb36WyCm6JI460DEFCjc+nr4BVLuo7mJ0GipOk1v3sxoSAy5t1KAa/DNQYG";
        String appCode = "f9d6d9c8d97a467e";
        Map<String,String> map = new HashMap<>();
        String str = "{\"appnt\":{\"birthday\":\"2002-04-10\",\"email\":\"mail@qq.com\",\"idType\":\"1\",\"idNo\":\"140702200204104812\",\"mobile\":\"13031550823\",\"name\":\" 测试投保人\",\"homeAddress\":\"北京市\",\"sex\":\"0\"},\"insureds\":[{\"birthday\":\"2002-04-10\",\"idNo\":\"140702200204104812\",\"idType\":\"1\",\"mobile\":\"13031550823\",\"name\":\"测试被保人\",\"homeAddress\":\"北京市\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"6010000.00\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"600.00\",\"riskCode\":\"test_002\",\"riskName\":\"测试医疗险\"}],\"sex\":\"0\"}],\"policy\":{\"acceptDate\":\"2026-01-22 00:00:00\",\"agentCode\":\"2027656293889654786\",\"amt\":\"6010000.00\",\"appntDate\":\"2026-01-22 14:14:07\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"IH0100022100508994XX\",\"policyEndDate\":\"2027-02-01 00:00:00\",\"policyNo\":\"IH1100022100509019XX\",\"policyStartDate\":\"2026-02-01 00:00:00\",\"prem\":\"600.00\",\"productPlanCode\":\"test_002\",\"productPlanName\":\"测试医疗险\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

    }


}
