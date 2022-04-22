package com.example.trace.template.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTemplate {

    public void execute() {
        final long startTime = System.currentTimeMillis();
        // 비즈니스 로직 실행
        call(); // 상속 : 자식 클래스에 따라 달라지는 부분이 된다.
        // 비즈니스 로직 종료
        final long endTime = System.currentTimeMillis();
        final long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }

    protected abstract void call();
}
