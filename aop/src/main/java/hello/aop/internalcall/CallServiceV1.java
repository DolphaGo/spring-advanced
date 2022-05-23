package hello.aop.internalcall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CallServiceV1 {

    private CallServiceV1 callServiceV1;

    // 생성자 주입을 쓰면 문제가 생긴다 (순환 참조 문제) : 자기 자신을 또 자기 자신한테 주입받으려고 하기 때문이다.(자기 자신이 생성도 안됐는데)

    @Autowired
    public void setCallServiceV1(final CallServiceV1 callServiceV1) {
        log.info("callServiceV1 setter={}", callServiceV1.getClass()); // 프록시가 주입이 된다.
        this.callServiceV1 = callServiceV1; // 생성이 다 끝나고 세터로 자기 자신을 호출하도록 하면 됩니다.
    }

    // 외부에서 호출
    public void external() {
        log.info("call external");
        callServiceV1.internal();
    }

    public void internal() {
        log.info("call internal");
    }
}
