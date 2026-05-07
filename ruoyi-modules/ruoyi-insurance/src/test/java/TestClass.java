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
        String str = "{\"appnt\":{\"birthday\":\"1968-04-23\",\"email\":\"786503110@qq.com\",\"idNo\":\"132302196804235620\",\"idType\":\"1\",\"mobile\":\"15030188163\",\"name\":\"维康梅\",\"sex\":\"1\"},\"insureds\":[{\"birthday\":\"1973-07-26\",\"idNo\":\"150421197307263816\",\"idType\":\"1\",\"mobile\":\"15620564450\",\"name\":\"李振山\",\"relationToAppnt\":\"1\",\"riskList\":[{\"amt\":\"2000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"619.4\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"0\"}],\"policy\":{\"acceptDate\":\"2026-04-30 15:18:17\",\"agentCode\":\"KD2046784302018215938\",\"amt\":\"2000000\",\"appntDate\":\"2026-04-30 15:16:34\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601235862574\",\"policyEndDate\":\"2027-04-30 23:59:59\",\"policyNo\":\"12426826601176140137\",\"policyStartDate\":\"2026-05-01 00:00:00\",\"prem\":\"619.4\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "V3GfVCTIeuDEzY8GPDMAoODQvK/7OmnYmA/6nf3SZNTTJkZXVVnF34OSk9F15z1ybKXTrIcix+2Tg3taymvy6Nily1C5CtN9JKBnDUqw7CI3tRcETvSCg4hC0QEuaonZGDaV+sg92DcCHWKcw7/E+yscWlaGcfFEdcPt4BCPq2KGakzTr18Acs3t2y/+uM6Hc0g5zneXGWz1bgvAiP6urlvxtS/SWVKRLP2vCoRC6me09nG0A1kzyjK55fFC5CIFm6+KvcmRiiZKBSi38RtWslPacF1bF4DsgpcGHe2Ufq3CCwrj4V2FUnGp1yO82GBuaGrA+qg1jLbWyfOdGpVgjzzEjMiOLeF4apxD7hVE80mTnkVCUkjhYrrGB5+4UqC0TfrQffOcvNtyXkMG0t15NUku8yczXjF1wqPNwSonZFaE6EnwV9YLrFyVOd8eHPddf2ymP2yR9o5zIwxnEXdVuvd/9C/xSQVSY8M2yepXPswZUWpJm7iBwdAKPUhVkyEUIqdHEOlBBvUcnyrHJZT1/Prx7m9pqx23Jfyhb7XxnXWR+WwhOMkjKLUZ0MUrE5MUqbdGy4j0T2yYk/TfzguSYSdPD/W49NuRYKoGHXkfjPwhI66xhsBaV4HYJR1UIxMbnrlDzir26Rm4RAz20XpEhEvNG4sEPp48WwYoCduX2Tg5crFv4tcf3hL2iS3tFG50LL+bdgcYvZfa5Sn6Bds0PBMK2REjf4sGv7sXVjmGbiZaKTsXPq5RZleyih9ZHtEE6oM963SV/9Qi4bfM8t2Bp6cM1t13B785d2W4KS5J5c1ZWYvcyCT3X7zAcdtDQPHpW5rbxpWNSsnR78uDb8nWb4933O1YcPpzLzVIFxN2aLWrQV1y0zm1swIEVH9wo19ILn5ksTp0hg8JLhQGv+cfQghxvzxZ4AZ83uv3tSiu/I3vOIGCvDcpUwFsNCL8aeJwuk0U1+Z/IUHByxUbGwdrA4ZjZTKOBT2J3sZcO0H2oQU/socx9HTwrdeNOaQKMQ29d4JKyHo4l/msZVwjI++g3mmHguTvO1yDv4OAj2HnSRgh/Rj2+6nP0Q0zGd0H9c3CUeK+3XuC+v+T/dtXnhACJfTtE7mcKwt4vW8ZeUuRTRBOSc7hHTRYicusynuSAfoOIg+LYM+AtMUXvvf4XiVm9prjgt8mst5iuBYIJdy0HdaZjeM2JGyVHjhtRYlA7CrxWhkOQcAmKHXtaMc46cN0csPATLHXSKk4wZRccpfZuzXTuDp6wcEP75WCJolRKaGkxw8zv0Rsx1s95ZKrFCchzYNX5W4J4kJXFDuJjuGJVaj0c2F22CNmPcM8C7y7gnyn+/N+1xDYldW8eQatmuMLDJZ1mL2uA1gCftUOa3CRlNHpMrLcPRTYIEBieeO/uJrg2N3LaS++cYhkQhzOK2OrYSC+KSXhqyD6GPSzo3oFBYnEi+vSq8tJJRSuIbOnRE9QCvYBymfSDCtBFQBdDGPczrobTtvEYcOr53ZCJrgynpD53EwmHYtNlqzKpmUPg+Mx";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
