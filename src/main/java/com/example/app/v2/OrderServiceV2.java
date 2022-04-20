package com.example.app.v2;

import org.springframework.stereotype.Service;

import com.example.trace.TraceId;
import com.example.trace.TraceStatus;
import com.example.trace.hellotrace.HelloTraceV1;
import com.example.trace.hellotrace.HelloTraceV2;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceV2 {

    private final OrderRepositoryV2 orderRepository;
    private final HelloTraceV2 trace;

    public void orderItem(final TraceId traceId, String itemId) {
        final TraceStatus status = trace.beginSync(traceId, "OrderServiceV2.orderItem()");
        try {
            orderRepository.save(status.getTraceId(), itemId);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e; // 예외를 꼭 다시 던져주어야 한다.
        }
    }
}
