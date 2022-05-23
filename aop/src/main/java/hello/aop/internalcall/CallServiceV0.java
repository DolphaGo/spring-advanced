package hello.aop.internalcall;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CallServiceV0 {

    // 외부에서 호출
    public void external() {
        log.info("call external");
        internal(); // 내부 메서드 호출(this.internal()) 참고로 자바에서 this를 생략하면, 자기 자신의 인스턴스를 의미한다.
    }

    public void internal() {
        log.info("call internal");
    }
}
