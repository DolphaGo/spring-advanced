package hello.proxy.pureproxy.concreteproxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeProxy extends ConcreteLogic {
    private ConcreteLogic target;

    public TimeProxy(final ConcreteLogic target) {
        this.target = target;
    }

    @Override // Override를 통한 다형성 적용
    public String operation() {
        log.info("TimeDecorator 실행");
        final long startTime = System.currentTimeMillis();

        final String result = target.operation();

        final long endTime = System.currentTimeMillis();

        final long resultTime = endTime - startTime;

        log.info("TimeDecorator 종료. resultTime = {}ms", resultTime);

        return result;
    }
}
