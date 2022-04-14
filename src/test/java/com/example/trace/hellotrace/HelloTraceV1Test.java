package com.example.trace.hellotrace;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.trace.TraceStatus;

class HelloTraceV1Test {

    /**
     * 02:54:17.967 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV1 - [09506b7d] DolphaGo
     * 02:54:17.971 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV1 - [09506b7d] DolphaGo time=5ms
     */
    @Test
    void begin_end() {
        final HelloTraceV1 trace = new HelloTraceV1();
        final TraceStatus status = trace.begin("DolphaGo");
        trace.end(status);
    }

    /**
     * 02:55:09.838 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV1 - [665e982f] DolphaGo
     * 02:55:09.840 [Test worker] INFO com.example.trace.hellotrace.HelloTraceV1 - [665e982f] DolphaGo time=4ms ex=java.lang.IllegalStateException: 어쩌구 저쩌구 에러가 발생했습니다.
     */
    @Test
    void begin_exception() {
        final HelloTraceV1 trace = new HelloTraceV1();
        final TraceStatus status = trace.begin("DolphaGo");
        trace.exception(status, new IllegalStateException("어쩌구 저쩌구 에러가 발생했습니다."));
    }

}
