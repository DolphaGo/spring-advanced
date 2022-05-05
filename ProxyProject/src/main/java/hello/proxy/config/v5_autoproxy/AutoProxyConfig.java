package hello.proxy.config.v5_autoproxy;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import hello.proxy.config.AppV1Config;
import hello.proxy.config.AppV2Config;
import hello.proxy.config.v3_proxyfactory.advice.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;

@Configuration
@Import({ AppV1Config.class, AppV2Config.class })
public class AutoProxyConfig {

    /**
     * 자동 프록시 생성기가 이미 스프링에 자동으로 등록이 되어있으니, 그 BeanPostProcssor이 어드바이저를 찾는다고 했다.
     * 따라서 어드바이저만 등록해주면 끝이다.
     */
    @Bean
    public Advisor getAdvisor1(final LogTrace logTrace) {
        // pointcut
        final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedNames("request*", "order*", "save*");

        // advice
        final LogTraceAdvice advice = new LogTraceAdvice(logTrace);

        return new DefaultPointcutAdvisor(pointcut, advice);
    }
}
