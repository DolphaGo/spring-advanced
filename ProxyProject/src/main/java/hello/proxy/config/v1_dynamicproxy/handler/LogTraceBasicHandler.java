package hello.proxy.config.v1_dynamicproxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class LogTraceBasicHandler implements InvocationHandler { //  java.lang.reflect.InvocationHandler 임에 주의한다.

    private final Object target;
    private final LogTrace logTrace;

    public LogTraceBasicHandler(final Object target, final LogTrace logTrace) {
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        TraceStatus status = null;
        try {
            final String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()"; // 메서드를 선언한 클래스를 SimpleName을 가져오고, 메서드의 이름을 가져오면 된다.
            status = logTrace.begin(message);
            // target 호출 (로직 호출)
            final Object result = method.invoke(target, args);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
