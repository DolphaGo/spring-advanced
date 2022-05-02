package hello.proxy.config.v2_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderControllerConcreteProxy extends OrderControllerV2 {

    private final OrderControllerV2 target;
    private final LogTrace logTrace;

    public OrderControllerConcreteProxy(final OrderControllerV2 target, final LogTrace logTrace) {
        super(null); // 프록시 객체에서 부모 클래스의 기능을 쓸 것은 아니기 때문에 null로 처리함
        this.target = target;
        this.logTrace = logTrace;
    }

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

    @Override
    public String noLog() {
        return target.noLog();
    }
}
