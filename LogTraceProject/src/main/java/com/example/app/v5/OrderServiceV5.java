package com.example.app.v5;

import org.springframework.stereotype.Service;

import com.example.trace.callback.TraceTemplate;
import com.example.trace.logtrace.LogTrace;

@Service
public class OrderServiceV5 {

    private final OrderRepositoryV5 orderRepository;
    private final TraceTemplate template;

    public OrderServiceV5(final OrderRepositoryV5 orderRepository, final LogTrace trace) {
        this.orderRepository = orderRepository;
        this.template = new TraceTemplate(trace);
    }

    public void orderItem(String itemId) {
        template.execute("OrderServiceV5.orderItem()", () -> {
            orderRepository.save(itemId);
            return null;
        });
    }
}
