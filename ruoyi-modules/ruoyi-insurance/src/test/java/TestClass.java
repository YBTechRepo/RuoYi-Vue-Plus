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
        String str = "{\"appnt\":{\"birthday\":\"1971-11-18\",\"email\":\"2524590324@qq.com\",\"idNo\":\"13012319711118211X\",\"idType\":\"1\",\"mobile\":\"13930457161\",\"name\":\"冯庆军\",\"sex\":\"0\"},\"insureds\":[{\"birthday\":\"1971-11-18\",\"idNo\":\"13012319711118211X\",\"idType\":\"1\",\"mobile\":\"13930457161\",\"name\":\"冯庆军\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"2000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"652\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"0\"}],\"policy\":{\"acceptDate\":\"2026-06-21 19:16:41\",\"agentCode\":\"KD2056636317577818114\",\"amt\":\"2000000\",\"appntDate\":\"2026-06-21 15:32:05\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601299522353\",\"policyEndDate\":\"2027-06-21 23:59:59\",\"policyNo\":\"12426826601233997196\",\"policyStartDate\":\"2026-06-22 00:00:00\",\"prem\":\"652\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "KcUDUPeizttqxxYQfgj9N5jnlVCxSq9F6RPVWegrgM5TAMQSkHtYF8EuLnoylsUJzVbhjSDqoe9sCl9YE/xDXPKjqp5BfSaLLxto0VdJ7yLZKaX2Qpt6baHiaNuzWppqfkrW68cDpvB7tmZbCm/v6ttv57iynBNVGEpo20iBHkUr8MZnKnPI4k64jluJwOoxe+cC2L4bUYQ0Aq31UuhydtjToopW4GFi4VCDO2ZHPn0xSCPGsLpW/bQxrBcAulkHM3FFU+CXKmGtG3T+ESGpHOjaK6fjzK53lDS+p8AevV99+sluOMj7GS6+rRcY9FFN1tmn4vGCpydnRd+npALE0oler5iUo0XQTouNlRyxNrw/8q5aUIT/bUS/lWloRa/q1vqlUalnDrbO463chYcikSAOKHGTLlOHCNOobTafk48ycEBg+Enxuy+krECrYDbgTQDnxULXtxnC0gV+9t3b9nihEc84fkI0M20fXe0ltbZpHeUnDvdZkA4yqmlKvSsPTyKNE8K68D/BJemBpD29CqVN0SJjXvwcQRrJpgYtpWwqoI8+WaYnXjk9Cy51/gmwTpq+btO49fGUtohR86TpTS2KnnmWbBmPvKweDIl+a/fGMm2DzRIkfleZtH5FnJDJ3ZZG3NWVJHITaRpgr0zSjgcpLBbeiKVaSFtdSsmTYn5739RT9JW/jz1hXtzPcFD3n2Bw7S8N/X/b1rc7EeSjXTQ1yL49jk0c/7Vk+RjjhDvqx1ylcyzu3fgkzfhfBkV9W/l16HTFZtId7DXCXjqfxZZtXpurRbwxnNKznvPwYk8KWDx43tJnVQ928ikNRU0YH5WI9J6esMQ4jGMAP0Y0rxgW5rvL/A9Ngtt5FzhtFmgz4Undvfq7UD/7wyp/l6QWr1vpllqVLShLz3wkWfA0wOQxZHe/s1vcEzqYrm2VQqsa8rVmSEgXn5jhjiaP4fu8fIIadOV0ZjhJq+XHjbBOY64F7SCaRe7XoeDuxHuS+fder/R2rf1gI7aO63qunp4ALaUzcFalys8lUJgZRg3zGGz1NMgz436786y6wNNlNzTrqzHV3zd62euHFweHFEhawWJJKbNioi4RW4OtlTzEozcjtWQwm0T9NeQM9ByjJSkD8Xi1ajeOmp93FJh6M85i3KBD/uk6UZo5JHMKj4rv3V6mMMWcZViCUO0P/gXekukoM+unzzwL7mU+/wzREjXylGmqu1MwyeDULKyVfHt1pj8TlLHw5IM9MFNAX0mz4punsgMe8MINyxIPtWM3pgSTAEDYxn8gyPuqwI/lYwDWORIxiG0JJ8qSU4yoVUG8c5wd9+hIb8jiInCKDUrQ2roM2BIYjKW7I58G5gUnWluccSpUO3pkdNuzEiH5AKEtCx0lJOqDF49SvT4fI2QvimrY+MWM6JnB2nS69TpAUNtys1YJ10jrSJ+O/gz6Ai8XtTAkrWftbhlevPWjDrYkeKArnblAC/MJ/5yWDbRnRAGsZ9bgYZvbzPvoXdrIHMJlU9xswsXHzwtREJZW9fWE4oUs";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
