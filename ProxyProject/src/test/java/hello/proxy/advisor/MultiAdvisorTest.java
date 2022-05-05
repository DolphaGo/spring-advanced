package hello.proxy.advisor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import hello.proxy.common.service.ServiceInterface;
import hello.proxy.common.service.ServiceInterfaceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiAdvisorTest {

    @DisplayName("여러 프록시")
    @Test
    void multiAdvisorTest1() {
        // client -> proxy2(advisor2) -> proxy1(advisor1) -> target

        // 프록시1 생성
        final ServiceInterface target = new ServiceInterfaceImpl();
        final ProxyFactory proxyFactory1 = new ProxyFactory(target);
        final DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
        proxyFactory1.addAdvisor(advisor1);
        final ServiceInterface proxy1 = (ServiceInterface) proxyFactory1.getProxy();

        // 프록시2 생성, target -> proxy1 입력
        final ProxyFactory proxyFactory2 = new ProxyFactory(proxy1);
        final DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());
        proxyFactory2.addAdvisor(advisor2);

        final ServiceInterface proxy2 = (ServiceInterface) proxyFactory2.getProxy();

        proxy2.save();
    }


    @DisplayName("하나의 프록시, 여러 어드바이저")
    @Test
    void multiAdvisorTest2() {
        // client -> proxy2(advisor2) -> proxy1(advisor1) -> target

        final DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
        final DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());

        // 프록시1 생성
        final ServiceInterface target = new ServiceInterfaceImpl();
        final ProxyFactory proxyFactory = new ProxyFactory(target);

        proxyFactory.addAdvisor(advisor2);
        proxyFactory.addAdvisor(advisor1);

        final ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

        // 실행
        proxy.save();
    }

    static class Advice1 implements MethodInterceptor {

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            log.info("advice1 호출");
            return invocation.proceed();
        }
    }

    static class Advice2 implements MethodInterceptor {

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            log.info("advice2 호출");
            return invocation.proceed();
        }
    }
}
