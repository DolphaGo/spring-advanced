package com.example.trace;

/**
 * 로그의 상태 정보를 나타내기 위한 용도
 */
public class TraceStatus {

    private TraceId traceId;
    private Long startTimeMs; //로그를 시작할 때의 상태 정보를 갖고 있다. 이 상태 정보는 로그를 종료할 때 사용된다.
    private String message;

    public TraceStatus(final TraceId traceId, final Long startTimeMs, final String message) {
        this.traceId = traceId;
        this.startTimeMs = startTimeMs;
        this.message = message;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public Long getStartTimeMs() {
        return startTimeMs;
    }

    public String getMessage() {
        return message;
    }
}
