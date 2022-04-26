package com.example.app.v5;

import org.springframework.stereotype.Repository;

import com.example.trace.callback.TraceTemplate;
import com.example.trace.logtrace.LogTrace;

@Repository
public class OrderRepositoryV5 {

    private TraceTemplate template;

    public OrderRepositoryV5(final LogTrace trace) {
        this.template = new TraceTemplate(trace);
    }

    public void save(String itemId) {
        template.execute("OrderRepositoryV5.save()", () -> {
            if (itemId.equals("ex")) {
                throw new IllegalStateException("예외 발생!");
            }

            // 상품을 저장하는데 1초정도 걸린다고 가정하자.
            sleep(1000);
            return null;
        });
    }

    private void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
