package hello.proxy.config.v2_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderServiceConcreteProxy extends OrderServiceV2 {

    private final OrderServiceV2 target;
    private final LogTrace logTrace;

    /**
     * 부모 타입의 생성자를 호출해야 한다.
     * 그러나 현재 부모 타입에는 기본 생성자가 없다.
     */
    public OrderServiceConcreteProxy(final OrderServiceV2 target, final LogTrace logTrace) {
        super(null); // 부모의 기능을 쓰지 않을 것이기 때문에 null로 넣는다. 자바 문법상 부모 생성자를 호출해야 하기 때문에 들어가 있는 것이다.
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public void orderItem(final String itemId) {
        TraceStatus status = null;
        try {
            status = logTrace.begin("OrderRepository.request()");
            // target 호출
            target.orderItem(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
