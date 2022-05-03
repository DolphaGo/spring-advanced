package hello.proxy.config.v1_dynamicproxy;

import java.lang.reflect.Proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.app.v1.OrderControllerV1Impl;
import hello.proxy.app.v1.OrderRepositoryV1;
import hello.proxy.app.v1.OrderRepositoryV1Impl;
import hello.proxy.app.v1.OrderServiceV1;
import hello.proxy.app.v1.OrderServiceV1Impl;
import hello.proxy.config.v1_dynamicproxy.handler.LogTraceFilterHandler;
import hello.proxy.trace.logtrace.LogTrace;

@Configuration
public class DynamicProxyFilterConfig {

    private static final String[] PATTERNS = { "request*", "order*", "save*" };

    @Bean
    public OrderControllerV1 orderControllerV1(LogTrace logTrace) {
        final OrderControllerV1 orderController = new OrderControllerV1Impl(orderServiceV1(logTrace));
        final OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(OrderControllerV1.class.getClassLoader(),
                                                                                   new Class[] { OrderControllerV1.class },
                                                                                   new LogTraceFilterHandler(orderController, logTrace, PATTERNS));
        return proxy;
    }

    @Bean
    public OrderServiceV1 orderServiceV1(LogTrace logTrace) {
        final OrderServiceV1 orderService = new OrderServiceV1Impl(orderRepositoryV1(logTrace));
        final OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(OrderServiceV1.class.getClassLoader(),
                                                                             new Class[] { OrderServiceV1.class },
                                                                             new LogTraceFilterHandler(orderService, logTrace, PATTERNS));
        return proxy;
    }

    @Bean
    public OrderRepositoryV1 orderRepositoryV1(LogTrace logTrace) {
        final OrderRepositoryV1Impl orderRepository = new OrderRepositoryV1Impl();
        // 인터페이스 기반으로 프록시 타입이 정해지기 때문에 캐스팅을 하면 된다.
        final OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(OrderRepositoryV1.class.getClassLoader(),
                                                                                   new Class[] { OrderRepositoryV1.class },
                                                                                   new LogTraceFilterHandler(orderRepository, logTrace, PATTERNS));
        return proxy;
    }
}
