package com.example.trace.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.trace.strategy.code.strategy.ContextV2;
import com.example.trace.strategy.code.strategy.Strategy;
import com.example.trace.strategy.code.strategy.StrategyLogic1;
import com.example.trace.strategy.code.strategy.StrategyLogic2;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextV2Test {

    /**
     * 전략 패턴 적용
     */
    @Test
    void strategyV1() {
        final ContextV2 context = new ContextV2();
        context.execute(new StrategyLogic1());
        context.execute(new StrategyLogic2());
    }

    @DisplayName("익명 내부 클래스 사용")
    @Test
    void strategyV2() {
        final ContextV2 context = new ContextV2();
        context.execute(new Strategy() {
            @Override
            public void call() {
                log.info("비즈니스 로직1 실행");
            }
        });
        context.execute(new Strategy() {
            @Override
            public void call() {
                log.info("비즈니스 로직2 실행");
            }
        });
    }

    @DisplayName("람다 사용")
    @Test
    void strategyV3() {
        final ContextV2 context = new ContextV2();
        context.execute(() -> log.info("비즈니스 로직1 실행"));
        context.execute(() -> log.info("비즈니스 로직2 실행"));
    }
}
