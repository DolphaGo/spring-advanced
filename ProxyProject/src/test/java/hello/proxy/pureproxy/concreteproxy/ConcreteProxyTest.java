package hello.proxy.pureproxy.concreteproxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hello.proxy.pureproxy.concreteproxy.code.ConcreteClient;
import hello.proxy.pureproxy.concreteproxy.code.ConcreteLogic;
import hello.proxy.pureproxy.concreteproxy.code.TimeProxy;

public class ConcreteProxyTest {

    @DisplayName("프록시 적용 전")
    @Test
    void noProxy() {
        final ConcreteLogic concreteLogic = new ConcreteLogic();
        final ConcreteClient client = new ConcreteClient(concreteLogic);
        client.execute();
    }

    @DisplayName("TimeProxy 추가")
    @Test
    void addProxy() {
        final ConcreteLogic concreteLogic = new ConcreteLogic();
        final TimeProxy timeProxy = new TimeProxy(concreteLogic);
        final ConcreteClient client = new ConcreteClient(timeProxy);
        client.execute();
    }
}
