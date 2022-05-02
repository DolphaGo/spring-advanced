package hello.proxy.pureproxy.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hello.proxy.pureproxy.proxy.code.CacheProxy;
import hello.proxy.pureproxy.proxy.code.ProxyPatternClient;
import hello.proxy.pureproxy.proxy.code.RealSubject;

public class ProxyPatternTest {

    @DisplayName("아직 프록시를 적용하지 않은 상태")
    @Test
    void noProxyTest() {
        final RealSubject realSubject = new RealSubject();
        final ProxyPatternClient client = new ProxyPatternClient(realSubject);
        client.execute();
        client.execute();
        client.execute();
    }

    @DisplayName("캐시 프록시 적용한 테스트")
    @Test
    void cacheProxyTest() {
        final RealSubject realSubject = new RealSubject();
        final CacheProxy cacheProxy = new CacheProxy(realSubject);
        final ProxyPatternClient client = new ProxyPatternClient(cacheProxy);
        client.execute();
        client.execute();
        client.execute();
    }
}
