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
        String str = "{\"appnt\":{\"birthday\":\"1987-03-20\",\"email\":\"2655061493@qq.com\",\"idNo\":\"211224198703208915\",\"idType\":\"1\",\"mobile\":\"18713890292\",\"name\":\"韩永超\",\"sex\":\"0\"},\"insureds\":[{\"birthday\":\"1964-08-18\",\"idNo\":\"211224196408183912\",\"idType\":\"1\",\"mobile\":\"15027494257\",\"name\":\"韩国林\",\"relationToAppnt\":\"4\",\"riskList\":[{\"amt\":\"2000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"1403.15\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"0\"}],\"policy\":{\"acceptDate\":\"2026-08-30 19:11:13\",\"agentCode\":\"KD2089897835404476417\",\"amt\":\"2000000\",\"appntDate\":\"2026-08-30 19:00:53\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601410201547\",\"policyEndDate\":\"2027-08-30 23:59:59\",\"policyNo\":\"12426826601335751007\",\"policyStartDate\":\"2026-08-31 00:00:00\",\"prem\":\"1403.15\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

//        for (int i = 0; i < 5; i++) {
//            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
//        }

        String encryptData = "CpfuAO6fO7AchS9iC+dbO5qvSdBcOzl2wx/UxkS28prtOJAy4cqZSMt/61JS3FINhTQBOtoT+zRljyHHYOualz0jNWZCQ5HBoV9+s/tnl9WvAETLaUWue12OUO6vtazzV/aPmY/GTHrDuyIETNZ3nnGwwbI6rm3VIOZSYw6DGuMIMeC4gk8rWgXj8+cw7gMF96limE/NWIDJj33ssZNuAjD7zx2oztfVNhIbmK93OjeHXiDHyGvE57mJRGyP8yDhgB/eGxA2u7w423yebWlzlcR0N7COB5z+MNbxKVGAP06O52iITAo9E/7J0f7KWhKpOudQmY5+sFtPOxDDE3WnjQROEicetm21+CbzhH1+RYrGUYWVD+YAknuT3QkKMsZGPaeo3urSA49JpWh5jl6DDa6Q6Q3IhKfdRf/3vadCfhECgS3yS8AT7aQjLP9b+tcWJUd0Ma7q+k7Lln97R5/cq9d7wK8Ubiy3KJYyP0B6aUYwsZmqqsQ1j+IVVPH8fBgOX289Nu7DSmhitqNC2hhpd4MftIHBCh3fPJLl8hbgwBvzm/PrqdhPS6y5uBbZ4JZqWwL6TjSTRH6Nh9oDpnCuXGnYKWpB5rRyn62+/foHGnJFspjMbkdCyl4IMax70yBk3eEMoDta+Yjwn6Mzrk9wFcKu1Lw3OrNjvh8Meevnu3IERNxdeK8q0SZcvAzHkRXmP53lKprSqDQH4bgm3wh+xgLacLF4RSMfzlp/00flpvN73HZNnjKu/6A/tjF5bM8ehWUsO4ZfUTCdAN57NoulbyrqsSmb9SGJubDK7C7kfad5e9RzeWaU2/uZFZfn5pluT4cB6xiQvGFV5ilD0t1SmJEOqUVv4xU1tnIgQRMtof9+rju5Wwp2Wli+5RkDoRI/IqlPWmzInSvmbqCnu5JOiZwrls8T5OHulhKLlX0zsJYweYUIMlOXmSLl+8Zy6LuX0HM3iTCJPEtB4C6d+Fgt19Oh74Z4P4TvnaZ2H2gyqS9CRYDlr5SDrZCzii61mu88MOFy1JuSNPTEx7tpXTEd9lxHdDpIaWJB/DXxUozcOzYUG9hE7nlxAuehMPZWcfwVKXkqElOLU2KR1m1jIr9SAFOB2kDk+3JFas4WUlb1zR+QqrTDwFoKP5lvJZgy4Y0v5bCjqbp+q95mQIzmO3bz5zVeXUqWZT6gvwr5rC9C1Zle5cuLreQHUDK6dWAKeiHUZrJVBMiYloi7saUjOD+sbqLted+RbvhZRW1MiR3JCggGawTwEs96GxzIaa9ij8RzvibyOzMtuRseYiWIMd8gr6RVfPQVfYGaLhoWC2yX0taKOBXBw8KZxAZ8dQWxg1LFO7yFhyfsq7w00MCIZLDrdAXZJDX6v0XyD2N2vEPHGstP8s25F6uAYyb/1jEX8L/Bj2+MI17EHefHSoE7Ssrsjd9Zp/yCx6FC9va3AoXZjm/8bR4m/JHOA1ygby+edarYM7t44Q4HEw3ow7cKKYJ5Jfl+eLEDlxupgpoeV3DnGblHX0WQfTnZMNQjxrQLu7zr";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
