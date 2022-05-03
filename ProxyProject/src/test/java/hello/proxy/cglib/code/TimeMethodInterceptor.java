package hello.proxy.cglib.code;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeMethodInterceptor implements MethodInterceptor { // org.springframework.cglib.proxy.MethodInterceptor; 임에 주의한다.

    // 항상 프록시는 내가 호출해야할 대상이 필요하다고 했죠.
    private final Object target;

    public TimeMethodInterceptor(final Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
        log.info("TimeProxy 실행");
        final long startTime = System.currentTimeMillis();

//        final Object result = method.invoke(target, args); // 메서드를 호출하는 부분이 동적이기 때문
        // 위와 같이 해도 되지만, CGLIB에서는 methodProxy를 사용하면 더 빠르다고 합니다(권장)
        final Object result = methodProxy.invoke(target, args);

        final long endTime = System.currentTimeMillis();
        final long resultTime = endTime - startTime;
        log.info("TimeProxy 종료. resultTime = {}", resultTime);
        return result;
    }
}
