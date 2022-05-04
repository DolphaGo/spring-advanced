package hello.proxy.common.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeAdvice implements MethodInterceptor {
    // 이제 타겟을 넣어주지 않아도 됩니다. 프록시 팩토리에서 만들 때 타겟을 이미 만들기 때문입니다.

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        log.info("TimeProxy 실행");
        final long startTime = System.currentTimeMillis();
        final long endTime = System.currentTimeMillis();

        // invocation.proceed()에서 타겟을 찾아서, 매개변수(args)도 넘겨주고 실행을 하게 됩니다.
        final Object result = invocation.proceed();

        final long resultTime = endTime - startTime;
        log.info("TimeProxy 종료. resultTime = {}", resultTime);
        return result;
    }
}
