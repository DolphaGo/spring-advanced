package com.example.app.v5;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.trace.callback.TraceCallback;
import com.example.trace.callback.TraceTemplate;
import com.example.trace.logtrace.LogTrace;

@RestController
public class OrderControllerV5 {

    private final OrderServiceV5 orderService;
    private final TraceTemplate template;

    public OrderControllerV5(final OrderServiceV5 orderService, final LogTrace trace) {
        this.orderService = orderService;
        this.template = new TraceTemplate(trace); // 어차피 템플릿은 한번만 만들면 되기 때문에, OrderControllerV5 가 싱글톤이라서 딱 한번만 생성된다.
    }

    @GetMapping("/v5/request")
    public String request(String itemId) {
        return template.execute("OrderControllerV5.request()", new TraceCallback<>() {
            @Override
            public String call() {
                orderService.orderItem(itemId);
                return "ok";
            }
        });
    }
}
