package hello.proxy.jdkdynamic;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

import hello.proxy.jdkdynamic.code.AImpl;
import hello.proxy.jdkdynamic.code.AInterface;
import hello.proxy.jdkdynamic.code.BImpl;
import hello.proxy.jdkdynamic.code.BInterface;
import hello.proxy.jdkdynamic.code.TimeInvocationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdkDynamicProxyTest {

    @Test
    void dynamicA() {
        final AInterface target = new AImpl();
        final TimeInvocationHandler handler = new TimeInvocationHandler(target);

        // 어떤 클래스 로더에, 어떤 인터페이스 타입으로, 어떤 동작을 할 지 정의
    //        final Object proxy = Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[] { AInterface.class }, handler);
        final AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[] { AInterface.class },
                                                                     handler); // AInterface 타입이기 때문에 이렇게 캐스팅을 해줘도 된다.

        // 위의 프록시는 handler의 로직을 수행한다. 내부에 invoke 로직이 있다.

        proxy.call();
        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }

    @Test
    void dynamicB() {
        final BInterface target = new BImpl();
        final TimeInvocationHandler handler = new TimeInvocationHandler(target);

        // 어떤 클래스 로더에, 어떤 인터페이스 타입으로, 어떤 동작을 할 지 정의. 이것이 바로 동적 프록시
        final BInterface proxy = (BInterface) Proxy.newProxyInstance(BInterface.class.getClassLoader(),
                                                                     new Class[] { BInterface.class },
                                                                     handler);

        // 위의 프록시는 handler의 로직을 수행한다. 내부에 invoke 로직이 있다.

        proxy.call();
        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }

}
