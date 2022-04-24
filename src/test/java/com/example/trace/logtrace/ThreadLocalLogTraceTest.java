package com.example.trace.logtrace;

import org.junit.jupiter.api.Test;

import com.example.trace.TraceStatus;

class ThreadLocalLogTraceTest {

    ThreadLocalLogTrace trace = new ThreadLocalLogTrace();

    /**
     * 03:01:11.559 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [67dde42c] hello1
     * 03:01:11.565 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [67dde42c] |-->hello2
     * 03:01:11.565 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [67dde42c] |<--hello2 time=3ms
     * 03:01:11.565 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [67dde42c] hello1 time=7ms
     */
    @Test
    void begin_end_level2() {
        TraceStatus status1 = trace.begin("hello1");
        TraceStatus status2 = trace.begin("hello2");
        trace.end(status2);
        trace.end(status1);
    }

    /**
     * 03:01:46.192 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [e858d68f] hello1
     * 03:01:46.196 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [e858d68f] |-->hello2
     * 03:01:46.196 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [e858d68f] |<X-hello2 time=1ms ex=java.lang.IllegalStateException
     * 03:01:46.197 [Test worker] INFO com.example.trace.logtrace.FieldLogTrace - [e858d68f] hello1 time=6ms ex=java.lang.IllegalStateException
     */
    @Test
    void begin_exception_level2() {
        TraceStatus status1 = trace.begin("hello1");
        TraceStatus status2 = trace.begin("hello2");
        trace.exception(status2, new IllegalStateException());
        trace.exception(status1, new IllegalStateException());
    }
}
