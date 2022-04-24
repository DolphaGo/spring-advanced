package com.example.trace.treadlocal;

import org.junit.jupiter.api.Test;

import com.example.trace.treadlocal.code.FieldService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldServiceTest {

    private FieldService fieldService = new FieldService();

    @Test
    void field() {
        log.info("main start");
        Runnable userA = () -> fieldService.logic("userA");
        Runnable userB = () -> fieldService.logic("userB");

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start();
//        sleep(2000); // 동시성 문제 발생 X
        sleep(100); // 동시성 문제 발생 O
        threadB.start(); // 여기서 끝내버리면 threadB의 조회 로그가 나오지 않음. 그 이유는 메인 쓰레드가 threadB가 돌고 있는데, 테스트를 종료시켰기 때문

        sleep(3000); // 메인 쓰레드 종료 대기
        log.info("main exit");
    }

    private void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
