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
        String str = "{\"appnt\":{\"birthday\":\"1977-10-19\",\"email\":\"2524590324@qq.com\",\"idNo\":\"132329197710191027\",\"idType\":\"1\",\"mobile\":\"13483199042\",\"name\":\"陈彩娟\",\"sex\":\"1\"},\"insureds\":[{\"birthday\":\"1977-10-19\",\"idNo\":\"132329197710191027\",\"idType\":\"1\",\"mobile\":\"13483199042\",\"name\":\"陈彩娟\",\"relationToAppnt\":\"0\",\"riskList\":[{\"amt\":\"4000000\",\"insuYear\":\"1\",\"insuYearFlag\":\"Y\",\"isMainRisk\":\"0\",\"payEndYear\":\"1\",\"payEndYearFlag\":\"Y\",\"prem\":\"1385.1\",\"riskCode\":\"MP03002216\",\"riskName\":\"平安医无忧城乡居民就医保\"}],\"sex\":\"1\"}],\"policy\":{\"acceptDate\":\"2026-07-21 16:19:42\",\"agentCode\":\"KD2056636317577818114\",\"amt\":\"4000000\",\"appntDate\":\"2026-07-21 16:05:26\",\"companyName\":\"平安财险\",\"companyType\":\"pingancaixian\",\"orderNo\":\"52426826601347107792\",\"policyEndDate\":\"2027-07-21 23:59:59\",\"policyNo\":\"12426826601277753052\",\"policyStartDate\":\"2026-07-22 00:00:00\",\"prem\":\"1385.1\",\"productPlanCode\":\"MP03002216\",\"productPlanName\":\"平安医无忧城乡居民就医保\",\"renewalType\":\"0\"}}";
        String encryptContent = RSAUtil.encryptByPublicKey(str, publicKey, "UTF-8");
        map.put("content",encryptContent);
        map.put("sign",RSAUtil.generateSign(encryptContent,appCode));
        System.out.println(map);

        for (int i = 0; i < 5; i++) {
            System.out.println(RSAUtil.generateSign(encryptContent,appCode));
        }

        String encryptData = "jZ3pUJbkuksxnIdM3tEmuJcp6wMTQjmTgbyDgWC0jrfW+L/BSqfzFyTtEfd/C3gjI1AgbXtrTiwnv0z8H9RmWmBc2kQxEzH7cD/9Xoa0trwvuU1WAd79QFJnybpZ3h53Yto2WiwDm7u2gG3lpW/woxyDdpn1161DDz8uR8Lhbqt9/9oCszcTGUxd6DD7HEI+ZClbZRDJjfJh0LAC3Nj+jH8o+xBMz+MGJMhVXaojmm+EL/4jjntTK3pa1A/qLS/Ir0cvovCfVsW2nme6Wz0uLEH1VNggpoa2fne2nY2SVVS0lkNF8FVQ8xStnE0WZXLuCjoLDAxh43c8xmHMPTJzjRw3MMUtzup6wH7qfO8U6BEy+RT1DntW3ajmaMyunE9G/6p9pAT+BAajkMC23q8trNKWopSNISfA8Xjf9g5bMO41NZQ2GyHl9BaFMZUOb6Qs6G9OdVByfPpSWQ0DSUSE5gpKjEMchR4nnbdLJBsbw1ltuv2W5oYRAEQjQM9ctrX7OzPmlJbDeVZiPdtaoCQ14AcKdRpC6VzB5s6rrbBJSYQCak15ByQKvCSUOGRbP8Qd6QycfDulHVor9B/pHn2tBhvtGnDNnNrCvywRrW3W/sMoEJfPTPQaHZP8KSLkndM3xPXhIDrWukmz9NEEUnj0M6cyW64Xmqq8fqeHPSUtzL8cx4kalvTeYV/cTSNU2lYqdDEvbrYyQweYuuqqTs/2BzxIfIZ2eMf7k1TLgHZHFJ8LJAlj7BBdE2/wNdL4MzaR6yA6cMGRllyx+53s7ghm7Rr5oCoTprDwjlpfLZQO3ifIhsyyoVpGqQEmWSMuj2rPzC9z4KQozez4K6UINBx3FBtQ/f1RoJSZ6XSEIFx+wMsjeJGTyOly7RIPF9MtQsiA+l86RWsJG/ExBx2IKnZRPS6jZMLvnAHq+88Q+IodxI92ivwowpIuCZ0Ak/eiYidsi9TEKn59GB1yM5lUNlTTki9+fSDmf+AgzDsZ2uz+vcGJk2RD8gnWDYTdZkvC82zSVZ1INOPz/gCopQ8+iprQvSPtZC3nz4wqB2l2H0roDWrrrQZbWSmWwj5X29fz3VtEzANVNoq2Py9BN45hxboSczjOvF6LuCHvK4Qv8NAGWLfhq/oxoo+SyQ/8i0wpt4smppDjoPXinwx3Zw0yHMzFpYQ3+bM+nTelrRibkR27gVQKWtj1vru2XVZL8H5FZwyJrl9roVTDLNMktAz3/7A+CQJzDFWp3yZt6Yu1AXlNBMt7jPPHjQiQrM6Qvm+SIhglAcA6/jFBSopWkx3LxAzumDwSG3Ts15NDB4md195+POxahCdCpOwSfBOMMRpXZ6I/hvs5TNprcWqTXRWP0Vu8hBtL7TJ5BQT6a2hOJkqT5TdVWsh1L+Q5nZN1BYsXXZewmE5qKxq07AEri6DDIbyH3ajGOQ2TwSSJYWHs1Eu2RqP18eai9JSVTl/n2UHD64BVBHr9ZpZDMoYffe464y6gb9I03znxJy0YIlB3dw7CZBgzMTLEpeo33CSaf7tQaVSv";
        System.out.println(RSAUtil.decryptByPrivateKey(encryptData, privateKey, "UTF-8"));
    }


}
