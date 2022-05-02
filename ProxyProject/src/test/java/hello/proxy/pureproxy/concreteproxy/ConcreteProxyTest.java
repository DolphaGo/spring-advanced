package hello.proxy.pureproxy.concreteproxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hello.proxy.pureproxy.concreteproxy.code.ConcreteClient;
import hello.proxy.pureproxy.concreteproxy.code.ConcreteLogic;

public class ConcreteProxyTest {

    @DisplayName("프록시 적용 전")
    @Test
    void noProxy() {
        final ConcreteLogic concreteLogic = new ConcreteLogic();
        final ConcreteClient client = new ConcreteClient(concreteLogic);
        client.execute();
    }
}
