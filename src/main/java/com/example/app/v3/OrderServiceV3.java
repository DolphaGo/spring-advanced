package com.example.app.v3;

import org.springframework.stereotype.Service;

import com.example.trace.TraceId;
import com.example.trace.TraceStatus;
import com.example.trace.hellotrace.HelloTraceV2;
import com.example.trace.logtrace.LogTrace;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceV3 {

    private final OrderRepositoryV3 orderRepository;
    private final LogTrace trace;

    public void orderItem(String itemId) {
        final TraceStatus status = trace.begin("OrderServiceV3.orderItem()");
        try {
            orderRepository.save(itemId);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e; // 예외를 꼭 다시 던져주어야 한다.
        }
    }
}
