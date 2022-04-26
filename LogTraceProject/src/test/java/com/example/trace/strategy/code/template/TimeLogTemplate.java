package com.example.trace.strategy.code.template;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeLogTemplate {

    public void execute(Callback callback) {
        final long startTime = System.currentTimeMillis();
        // 비즈니스 로직 실행
        callback.call(); // 위임
        // 비즈니스 로직 종료
        final long endTime = System.currentTimeMillis();
        final long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }
}
