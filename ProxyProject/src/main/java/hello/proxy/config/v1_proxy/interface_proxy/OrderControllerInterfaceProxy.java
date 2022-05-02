package hello.proxy.config.v1_proxy.interface_proxy;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderControllerInterfaceProxy implements OrderControllerV1 {

    private final OrderControllerV1 target;
    private final LogTrace logTrace;

    @Override
    public String request(final String itemId) {
        TraceStatus status = null;
        try {
            status = logTrace.begin("OrderController.request()");
            // target 호출
            final String request = target.request(itemId);
            logTrace.end(status);
            return request;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

    /**
     * 아무런 로그를 찍으면 안되므로 다음과 같이 단순하게 처리하면 된다.
     */
    @Override
    public String noLog() {
//        TraceStatus status = null;
//        try {
//            status = logTrace.begin("OrderController.noLog()");
        // target 호출
        final String result = target.noLog();
//            logTrace.end(status);
        return result;
//        } catch (Exception e) {
//            logTrace.exception(status, e);
//            throw e;
//        }
    }
}
