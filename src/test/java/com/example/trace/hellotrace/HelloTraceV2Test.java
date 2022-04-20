package com.example.trace.hellotrace;

import org.junit.jupiter.api.Test;

import com.example.trace.TraceStatus;

class HelloTraceV2Test {

    /**
     * 23:17:09.049 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [6b349b6d] DolphaGo
     * 23:17:09.055 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [6b349b6d] |-->DolphaGo2
     * 23:17:09.055 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [6b349b6d] |<--DolphaGo2 time=3ms
     * 23:17:09.055 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [6b349b6d] DolphaGo time=7ms
     */
    @Test
    void begin_end() {
        final HelloTraceV2 trace = new HelloTraceV2();
        final TraceStatus status1 = trace.begin("DolphaGo");
        final TraceStatus status2 = trace.beginSync(status1.getTraceId(), "DolphaGo2");
        trace.end(status2);
        trace.end(status1);
    }

    /**
     * 23:17:47.725 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [5aa90c22] DolphaGo
     * 23:17:47.730 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [5aa90c22] |-->DolphaGo2
     * 23:17:47.730 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [5aa90c22] |<X-DolphaGo2 time=2ms ex=java.lang.IllegalStateException: 어쩌구 저쩌구 에러가 발생했습니다.
     * 23:17:47.730 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV2 - [5aa90c22] DolphaGo time=6ms ex=java.lang.IllegalStateException: 어쩌구 저쩌구 에러가 발생했습니다.
     */
    @Test
    void begin_exception() {
        final HelloTraceV2 trace = new HelloTraceV2();
        final TraceStatus status1 = trace.begin("DolphaGo");
        final TraceStatus status2 = trace.beginSync(status1.getTraceId(), "DolphaGo2");
        trace.exception(status2, new IllegalStateException("어쩌구 저쩌구 에러가 발생했습니다."));
        trace.exception(status1, new IllegalStateException("어쩌구 저쩌구 에러가 발생했습니다."));
    }
}
