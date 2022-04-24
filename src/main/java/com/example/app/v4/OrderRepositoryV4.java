package com.example.app.v4;

import org.springframework.stereotype.Repository;

import com.example.trace.logtrace.LogTrace;
import com.example.trace.template.AbstractTemplate;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV4 {

    private final LogTrace trace;

    public void save(String itemId) {

        AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {
            @Override
            protected Void call() {
                // 저장 로직
                if (itemId.equals("ex")) {
                    throw new IllegalStateException("예외 발생!");
                }

                // 상품을 저장하는데 1초정도 걸린다고 가정하자.
                sleep(1000);
                return null;
            }
        };
        template.execute("OrderRepositoryV4.save()");
    }

    private void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
