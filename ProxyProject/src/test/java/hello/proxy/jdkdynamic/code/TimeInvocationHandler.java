package hello.proxy.jdkdynamic.code;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeInvocationHandler implements InvocationHandler {

    // 프록시가 호출해야 할 target이 필요하다.
    private final Object target;

    public TimeInvocationHandler(final Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        log.info("TimeProxy 실행");
        final long startTime = System.currentTimeMillis();
        final long endTime = System.currentTimeMillis();
        final Object result = method.invoke(target, args); // 메서드를 호출하는 부분이 동적이기 때문
        final long resultTime = endTime - startTime;
        log.info("TimeProxy 종료. resultTime = {}", resultTime);
        return result;
    }
}
