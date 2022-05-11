package hello.aop.member;

import org.springframework.stereotype.Component;

import hello.aop.member.annotation.ClassAop;
import hello.aop.member.annotation.MethodAop;

@ClassAop
@Component // AOP를 쓰려면 스프링 빈으로 등록되어야 하므로, 자동 컴포넌트 스캔의 대상이 되도록 한다.
public class MemberServiceImpl implements MemberService {

    @Override
    @MethodAop("test value")
    public String hello(final String param) {
        return "ok";
    }

    public String internal(String param) {
        return "ok";
    }
}
