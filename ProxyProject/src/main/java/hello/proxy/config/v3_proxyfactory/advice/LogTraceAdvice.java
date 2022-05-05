package hello.proxy.config.v3_proxyfactory.advice;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class LogTraceAdvice implements MethodInterceptor {

    private final LogTrace logTrace;

    public LogTraceAdvice(final LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TraceStatus status = null;
        try {
            final Method method = invocation.getMethod();
            final String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()"; // 메서드를 선언한 클래스를 SimpleName을 가져오고, 메서드의 이름을 가져오면 된다.
            status = logTrace.begin(message);
            // target 호출 (로직 호출)
            final Object result = invocation.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
