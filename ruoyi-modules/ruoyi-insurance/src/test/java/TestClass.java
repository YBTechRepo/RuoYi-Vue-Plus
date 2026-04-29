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
        String str = "{\"appnt\":{\"birthday\":\"1952-05-17\",\"email\":\"sulcw@foxmail.com\",\"idNo\":\"132801195205172900\",\"idType\":\"1\",\"mobile\":\"18631605200\",\"name\":\"尹玉梅\",\"sex\":\"1\"},\"insureds\":[{\"birthday\":\"1952-05-17\",\"idNo\":\"132801195205172900\",\"idType\":\"1\",\"mobile\":\"18631605200\",\"name\":\"尹玉梅\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"2000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"2737\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"1\"}],\"policy\":{\"acceptDate\":\"2026-04-25 14:06:34\",\"agentCode\":\"KD2047131675462586369\",\"amt\":\"2000000\",\"appntDate\":\"2026-04-25 14:05:43\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601231030067\",\"policyEndDate\":\"2027-04-25 23:59:59\",\"policyNo\":\"12426826601170808319\",\"policyStartDate\":\"2026-04-26 00:00:00\",\"prem\":\"2737\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

    }


}
