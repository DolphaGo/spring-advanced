package com.example.app.v1;

import org.springframework.stereotype.Service;

import com.example.trace.TraceStatus;
import com.example.trace.hellotrace.HelloTraceV1;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceV1 {

    private final OrderRepositoryV1 orderRepository;
    private final HelloTraceV1 trace;

    public void orderItem(String itemId) {
        final TraceStatus status = trace.begin("OrderServiceV1.orderItem()");
        try {
            orderRepository.save(itemId);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e; // 예외를 꼭 다시 던져주어야 한다.
        }
    }
}
