package hello.proxy.config.v1_dynamicproxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.util.PatternMatchUtils;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class LogTraceFilterHandler implements InvocationHandler { //  java.lang.reflect.InvocationHandler 임에 주의한다.

    private final Object target;
    private final LogTrace logTrace;
    private final String[] pattern; // 이 패턴일 때만, 로그를 남기도록 할 것이다.

    public LogTraceFilterHandler(final Object target, final LogTrace logTrace, final String[] pattern) {
        this.target = target;
        this.logTrace = logTrace;
        this.pattern = pattern;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        // 메서드 이름 필터
        final String methodName = method.getName();
        // save, request, reque*, *est 와 같은 패턴 적용
        if (!PatternMatchUtils.simpleMatch(pattern, methodName)) {
            return method.invoke(target, args); // 다른 로직을 처리하지 않고, 실제 메서드를 호출하도록 한다.
        }

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
