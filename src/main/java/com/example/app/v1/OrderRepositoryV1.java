package com.example.app.v1;

import org.springframework.stereotype.Repository;

import com.example.trace.TraceStatus;
import com.example.trace.hellotrace.HelloTraceV1;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV1 {

    private final HelloTraceV1 trace;

    public void save(String itemId) {
        final TraceStatus status = trace.begin("OrderRepositoryV1.save()");
        try {
            // 저장 로직
            if (itemId.equals("ex")) {
                throw new IllegalStateException("예외 발생!");
            }

            // 상품을 저장하는데 1초정도 걸린다고 가정하자.
            sleep(1000);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e; // 예외를 꼭 다시 던져주어야 한다.
        }

    }

    private void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
