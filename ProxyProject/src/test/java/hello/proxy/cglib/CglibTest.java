package hello.proxy.cglib;

import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;

import hello.proxy.cglib.code.TimeMethodInterceptor;
import hello.proxy.common.service.ConcreteService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CglibTest {
    @Test
    void cglib() {
        final ConcreteService target = new ConcreteService();

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ConcreteService.class);
        enhancer.setCallback(new TimeMethodInterceptor(target));

        // proxy의 부모타입(superClass)이 ConcreteService라서 캐스팅이 가능합니다.
        final ConcreteService proxy = (ConcreteService) enhancer.create();// 프록시 생성

        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());

        proxy.call();
    }
}
