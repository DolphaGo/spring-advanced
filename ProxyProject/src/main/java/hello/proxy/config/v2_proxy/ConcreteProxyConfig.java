package hello.proxy.config.v2_proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.config.v2_proxy.concrete_proxy.OrderControllerConcreteProxy;
import hello.proxy.config.v2_proxy.concrete_proxy.OrderRepositoryConcreteProxy;
import hello.proxy.config.v2_proxy.concrete_proxy.OrderServiceConcreteProxy;
import hello.proxy.trace.logtrace.LogTrace;

@Configuration
public class ConcreteProxyConfig {

    @Bean
    public OrderRepositoryV2 orderRepository(LogTrace logTrace){
        final OrderRepositoryV2 repositoryImpl = new OrderRepositoryV2();
        return new OrderRepositoryConcreteProxy(repositoryImpl, logTrace);
    }

    @Bean
    public OrderServiceV2 orderService(LogTrace logTrace){
        final OrderServiceV2 serviceImpl = new OrderServiceV2(orderRepository(logTrace));
        return new OrderServiceConcreteProxy(serviceImpl, logTrace);
    }

    @Bean
    public OrderControllerV2 orderController(LogTrace logTrace){
        final OrderControllerV2 controllerImpl = new OrderControllerV2(orderService(logTrace));
        return new OrderControllerConcreteProxy(controllerImpl, logTrace);
    }
}
