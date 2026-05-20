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
        String str = "{\"appnt\":{\"birthday\":\"2002-04-10\",\"email\":\"mail@qq.com\",\"idType\":\"1\",\"idNo\":\"140702200204104812\",\"mobile\":\"13031550823\",\"name\":\" 测试投保人\",\"homeAddress\":\"北京市\",\"sex\":\"0\"},\"insureds\":[{\"birthday\":\"2002-04-10\",\"idNo\":\"140702200204104812\",\"idType\":\"1\",\"mobile\":\"13031550823\",\"name\":\"测试被保人\",\"homeAddress\":\"北京市\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"6010000.00\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"600.00\",\"riskCode\":\"test_003\",\"riskName\":\"测试学平险\"}],\"sex\":\"0\"}],\"policy\":{\"acceptDate\":\"2026-01-22 00:00:00\",\"agentCode\":\"KD2027656293889654786\",\"amt\":\"6010000.00\",\"appntDate\":\"2026-01-22 14:14:07\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"IH01000221005089AC\",\"policyEndDate\":\"2027-05-01 00:00:00\",\"policyNo\":\"IH01000221005089AC\",\"policyStartDate\":\"2026-05-01 00:00:00\",\"prem\":\"600.00\",\"productPlanCode\":\"test_003\",\"productPlanName\":\"测试学平险\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "i00JnNibaeIFL6zeXcfLYnBLI4zP282LVNr/luAbQ1GjSfwCHHfvWah+w+vbqU1M5jwM2wFpAdBye8dcNYFgYVLqiDhx6wYlfmOvC5gF7jIRPDGPYH5Uo/SAeoOSUu6cy3tR/ma0lHiiKXgbVgxQOSwvQ59xWMQJvCq0omhsaKQGyL6qiX4dfTqmJQW+jBkHUIOPfRomGlXYZLxn7gbjyDHHH1/82q5DOczyu8wH7n4iFfeEwxW5WgPgt19KZg1XMYvebpha+bqeGFofe0vOVBW+4qHPYTlg8nGAlt/Z8QrWigdLUebs88EbAjKUcSToy9sdZQ96D0YYBPSR0o2qdFMsb1GvaVJyNVJ55k7PfqJAXZLSk8RBHufg95+Pf2zwxJunP0h9beA19+sf+dv1Zgj8BpmovKMlQaUfVvVLyRlMakYtZQyx0tjV57KTDEsgEKC4nI8IdsEFATvxyvVo5hS0ryQSD9fULWWOjGQqWRgPMr4prlSNUjaLoUkOjed8QrlPY8zIAe1jptP1u9trRyYAad2UnGt3NYq+ZZ9HFDEF5pVOFMnJL//BLdmfpSeigRhslYuJ/bDjDhjLHkzhGWflLJtj37SF5rO+A9emxxb9MLZS1U4vdmHFvkGdKbcb0U4K/WDIgUVwpYyNpWCRkEFj7uHmcm751F2vbsNGR6caTk85lDLoWXhNrSIMaZe6JWAQxxi+immDcejaoYZr4vtv+2pXW328owaXw3oc9xOBEK4UJwtCQe6EEeq12dmH+mGCoRC+rAT+kefvNgJw1/K8xDDnZw3qb/I26lIkJ809Yq3zoAabQ3too0WCwHhsOknxy9fEZEgzowKj5BQo4WK0cW77rQOV99QJ/7Mi9Fm+8/sWFiNtynncfjY6t6oG1v7VqUYcuAhsHs/Q+1+V3xUYu3ZHwI52uxPi1NqgTBX5OMp8Niak7dLjnNWNDTELKGG8D3qCrhKZoBUFKPgF8pgL+wkpMsZRGlBtoOBl8WDeU4Z1GPzyr6qM7sP9Urf4hfH0NmijfxEla8g5Fn2Qd0TouwQrQlnJarwv4qEG6rmnPCpraa1CBjIE8eaRo1CU9i0kZIYI5S0rTPzV2ijsGeTOcMlEdH6D8KIfrVAYbDlmT6ChGHg5Hn4c4X7Qf52WtnxXo3GfFZtwIpC+A4B3euagjm1g1PfuBs9e5Ou7MXKa38mY/mDhndcLdYgO0X/LIyObHPA62QMhHOWQ4KvmKxSbSXf7T6SJ2ob+n/cylQ7GRFvOKYm0TFGZUk055Yyka6I1rwDR+G2ohZIWD3THRJddulfVOOZgjb4JN4AMfmwZcBSIZSVEpJklWKohDJLCNbd/xqeDA9HaWu22fnidpJWzkLelcOeMxk4kfsH3E12VnCW1Osw8ZlKD4WaK0X+mobcU3PyLm43SQYXeK6Rm8ihPnQJlLLgS3U4ZIkQzP8wqnMvZPOVz6JzcJoj22+9mPfKGGdcdyWJrX9mUGoF0nL9ND+YFRETIevpqKuFWBMKf4U3FV4qKAaVu8jt88djy";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
