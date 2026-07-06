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
        String str = "{\"appnt\":{\"birthday\":\"1994-03-28\",\"email\":\"2524590324@qq.com\",\"idNo\":\"130123199403281549\",\"idType\":\"1\",\"mobile\":\"15076305950\",\"name\":\"常云芳\",\"sex\":\"1\"},\"insureds\":[{\"birthday\":\"1994-03-28\",\"idNo\":\"130123199403281549\",\"idType\":\"1\",\"mobile\":\"15076305950\",\"name\":\"常云芳\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"4000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"1023.15\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"1\"}],\"policy\":{\"acceptDate\":\"2026-06-30 09:33:35\",\"agentCode\":\"KD2056636317577818114\",\"amt\":\"4000000\",\"appntDate\":\"2026-06-30 09:30:34\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601311863301\",\"policyEndDate\":\"2027-06-30 23:59:59\",\"policyNo\":\"12426826601245408613\",\"policyStartDate\":\"2026-07-01 00:00:00\",\"prem\":\"1023.15\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "BM1/TLhHvWA0RL2M+FKIy4pH6SsS1q4R1337Xwle50PQuCVIlOdJXvuWab4bcU3ksOEraQgkcTHUxhhQiIGlDZgMT6B93pzbGIEkpRFWfHxzJx//DsEZ5F8uNbwK0RFyOlQkdrw8oxs6D8l0bgfiBpukQ7nIixgJjE9WWt3i0NMHM7l7SJ/p2coGfXj2HjpkMix9fhIMM7AZTxaIlB3yeeokf04QqK9IHSl21WeS3ecJ7gLq6CHuiSDeENfy9GjBwWZ+8zfNgJOVKkxeL0N+dyJtktdCIhdKQr56M+puA5idAhy+4Y42fHyMa6nKEG8bgnYWhOoyub61R9y7aJ9NMjwXPSJKPogftNK9lwf2+XQWGjmcibJ9KCodCKIz581Ixf77dx+wAMflS8zXk7mXV+zuXLD3mWlX+9+eOgzSGanOQmxh+qxWcpVxN+RFrPw7sYrMf92lO4AWqJIu7C76o63jYf0lD01Rk5Qsl70eWB7b5EEFJGf6CKWUY+IcbE7nho0z4G5d8s8RmfCUOcRALxKADl411ust7o3JjC3flyOaklpkdn9HCBnip4qPRZkPfTWGZPVDA3t3gjtM0a4etpYTbwCHmYuYorPyE05fB0Gz5N4VAZD+MfJOV9Pe6cZkG4NV1imVYoN9gqgT+A3qxOouPtkgZ8D/qFf7FgaQMl52C8QRyRLsUXD47KlWSuZM1haQ0FKDpvy1Z/apJcs71nmMOvh8piCBEkFII8H6kEmBL+8DtE7ZLsnKfqciq9Jly+tOGE8wg4QH57WemIWtB2I3u96xHGxYB1VsGfHs+1NjVNEG5iveDaLI4MjecL7wkOjQQObX/Bu9VGTi5gJsyhIiKsBdCy1TEyRRxnpTOE/88lqrZ947F6uO6CkbAspAw2FykYt/B9RGtHNlRdRkTs7lhyx7rLbfG8kBbHfKBzdPAjI3n2eM92NuFcnQ/5nzbIz2YHftJKzoWwp3olKm0fTQqccgRySpcPkbqk73sEKSsTM+WfoFDvbbtrwHQICNeXDLsF4LKLvRySnXvJ3vM5NXBITdTn3++08WYUZZ94EyPIUX22HKdDy+SQhNAI7FG20so1CCDHGIOcnGW4k4Ul9V7XhJiPvoom3JA/gxfgsURRJrnc57TxLNG2dhBKSH3uGi8xXcpKVOx1V/WIIIkFZFeu/jTEZyxv2M7SOICBE+3XF43k3ovUeiDePIO3qW/yPw9NUNnWgWC9X3GksHICx56hp+o2EfHs33Qw7AGKXJEjAlo77WPaVzOZUzMJTkb6eJ0txh40jm065y3m5hkcZUTiFC05ragtgHMUyNxHDbwd+Ye3LZXQsvpHxgQjVe8X6Ugup1Jj27+gqNonD1XlH64xweuGWVIvbRAcsfMgx77RhgB+X5RmM4VTMEE6BtOZWqlFWTXwEtLyL6YiY4OgzYFHaKIQC4igV/90zi2e2o3bnlWnSABiDWx2fQWqp0PPYS5SF3k1pB82EFvGiHzazqsU3puJ8AU0+3YW9tEEaBDlumkUOqbetFMTelurNy";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
