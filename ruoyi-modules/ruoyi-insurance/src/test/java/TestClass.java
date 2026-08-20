import org.dromara.common.json.utils.JsonUtils;
import org.dromara.insurance.utils.RSAUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestClass {

    @Test
    public void test01() throws Exception {
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcSWR7XHPRcqmjfW9jdkyZPTek+F5VEv8tUo8+8FA9fhegz5kwWNy0X+muwNYGl3VBokhjFMcnccSA9Fxizh64Y1iqfNvd/AMXVOHpfgqBs3Um87auNEN0UY9GIK00ks4A7xYl4CGE7o0cTyg75goji2Gy8lSX0rKYe33IH4dAnQIDAQAB";
        String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJxJZHtcc9FyqaN9b2N2TJk9N6T4XlUS/y1Sjz7wUD1+F6DPmTBY3LRf6a7A1gaXdUGiSGMUxydxxID0XGLOHrhjWKp82938AxdU4el+CoGzdSbztq40Q3RRj0YgrTSSzgDvFiXgIYTujRxPKDvmCiOLYbLyVJfSsph7fcgfh0CdAgMBAAECgYEAhM8pgFtUYIxb8/7IBXGmLiKDgsE9CHaYOl+KMyXsShU45wMDXr5k7zMCVeqw9fEHR7ssYGZrxu0624rMZ3pZR2vKCNvTrkBSXdKPdkCjNeY1XqxGSZne6USkJW3cP9MVdGgZ2OEHWT5UMe33nL/1qlDr+/QGw7VaS1L39U82YIECQQDKZjUNBnkelPP9s15L4LON3vr1EzivzerZ6PLHFiCE9w9m35385NSvXEKhB/kZH8Sp92p7iucb1KSnBAnIYMCZAkEAxaz0GGIeCfEiar1QVtacjQ1/l5Nqx13SaPhzOTi6SuV9ZMVtyJM5kgkKpx0RSWIhzlGtbRXi9wvXyMQ1fbDOpQJAAw60MnQslDevz99Lx12nmLcLl3Qo4Q79QzlvoW5GI3wP8X/jRRS32cZNA125d5WxRNgF9cPNZfA6ZIN9GlfL0QJBALq5PvHxbNKTVqFhJoyAuOx8fbPeapTcSS8i0SSSM1cgJCyWWbeHanBJ3igxSuz4p1pOyPUsIXKfWzuoK6cI5okCQQCuR8gmmHbGF424/VMyS4ZlRaRPPDiL4HqQzUMKgIVmGKPLvOwPDHfiVyL4S/SLAH2ySE05/kFgCq0CW/A9oQyl";
        String appCode = "a5cce81b7c70432f";
        Map<String,String> map = new HashMap<>();
        String str = "{\"appnt\":{\"birthday\":\"1991-06-13\",\"idNo\":\"130423199106130310\",\"idType\":\"1\",\"name\":\"史钊\",\"sex\":\"0\"},\"insureds\":[{\"birthday\":\"1991-06-13\",\"homeAddress\":\"\",\"idNo\":\"130423199106130310\",\"idType\":\"7\",\"mobile\":\"\",\"name\":\"史钊\",\"relationToAppnt\":\"32\",\"riskList\":[{\"amt\":\"4000000.00\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"338.00\",\"riskCode\":\"89008700\",\"riskName\":\"蓝医保·长期医疗险（好医好药版）\"}],\"sex\":\"0\"}],\"policy\":{\"orderNo\":\"029432612832932\",\"acceptDate\":\"2026-08-20 01:01:08\",\"agentCode\":\"KD2067061121200021506\",\"amt\":\"\",\"appntDate\":\"2026-08-20 01:01:08\",\"companyName\":\"太平洋健康险\",\"companyType\":\"taipingyangjiankangxian\",\"policyEndDate\":\"2027-08-20\",\"policyNo\":\"029432612832932\",\"policyStartDate\":\"2026-08-21\",\"prem\":\"338.00\",\"productPlanCode\":\"89008700\",\"productPlanName\":\"蓝医保·长期医疗险（好医好药版）\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "WwdcOdKYsJqfepcYp7MpVt2k/9Ad6t8fLDyrOu1Moq+oGbIP9iwXGqfE8MBLSo7/kjplj7FL9nqWkYfnsIyQ/vN+BbFM0dap7A9M2pE1Jha4Sy15X/0vV5a6twvSPLIOiRSl0Cwj1i4VjDW3t+vkR/DZH7+9v5NfRz1MyrvJqQ93+lN/IJYBWDuL1ajGE8SlB8EMuacMq/ckA1z5hmqt5kUvnP4mHDNTVpRcdEqobOL1H2s5UUS8T/jiNKsVbrS1wBalLEsCByhkdPt+3DI2nE6Mc6fhSbAzVS4YKh9nkRYu8/xqIYZ8mSbGfH1X39p6W2FtCqH2zkBJslDm1ULdvh1AIKrCSvgyHF/QK+zX6tZOye/TR/143hCEqzznCnwufsfV6AxAZl6PxTXME2p3hYEYXkw/qusq99MaEsIgcSQM1LXT9TgCH+3jDD81FH0i0Igz7Aoq+D7Cjnx2E5QByf0ZrttWe8XYIGk+ZzzgpHhAHFpaKen4BiAtcjzyxazDieSmvnr19fa5jCPh+VKPT0fshzaVvjEGmEJBeFX+t7jeXhvRfTZBysgltwT5YxWD8+kYFNjl6fE5PLtkpVpfMbN6O7GO/DFMl/ClkBAyAdSufP1p3tsmKrHuxwUyombt64HECJkVPY2bXxpQkLeCDwDRc06e0DobU0+jz5sYiykgMQmco7djmoG+Hy+yS5965u8zd0ud9Lmvlpj5pHI632SaHwfIXtaet6wrDPrdGHqVobH9oYR87HQzZuozM7K+DSjUFcW4OgbWs1Z8L1rJXRmBj5zW+WDz3yrfX/BYJyf0trmTcv3uCExttTNiKdnWGciq7Y+FuPN8+ExdW2sqJHSSIyMDX2nNs1WGBaRqmOofdqUruqBkbz9bpIx/Gi8oKEmks/clJyWxSs4qN9SUSUX06CJ+CgYpR6XMk7+jQJnpUjJS+KpWP4CrN3kTqJSgdm4eRSYGuIehnqSJ3IbJxpZFPjBi3v2A/9V1J+YWuABOJIkrTBOmHloYfSxqIaWqINWtWrhXWPAvhv9NYCenT1JrwJSKPlIHUiUvmmh7Ab2ADoojCBu+ZYR9NLfFdsgDgQ7O5OZfLx3XUhIh2vWDWam/ATKORpC+qvvwxg82aWPV+Uri2FtWlllyZJTFumiyesqPUjht5brLU/8h9siQXMZ1whYrUhJLbRbwlnKgc/cUT49b8SL/4+zb/5uYBJ96abOV/NoWCQiqWbwLOPPgkJxGLi7U8Yh19ZMSX/J4bQDqQ8kuz29ambk/xMARD7RR0XloM+TzBqW9SU1xXwgGBSBwzyctTqc0MXIqAULEBNqJJfHSvW/TxkMxcEPx5iflJRDVZVZ1NhTXBK/JwSk6wg==";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
