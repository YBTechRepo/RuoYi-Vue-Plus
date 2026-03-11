import org.dromara.insurance.utils.RSAUtil;
import org.junit.jupiter.api.Test;

public class TestClass {

    @Test
    public void test01() throws Exception {
        System.out.println(RSAUtil.generateAppCode());
        System.out.println(RSAUtil.generateKeyPair());
    }


}
