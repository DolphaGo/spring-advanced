package hello.aop.proxyvs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import hello.aop.member.MemberService;
import hello.aop.member.MemberServiceImpl;
import hello.aop.proxyvs.code.ProxyDIAspect;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(ProxyDIAspect.class)
@SpringBootTest
//@SpringBootTest(properties = "spring.aop.proxy-target-class=true") // CGLIB 프록시, 성공
//@SpringBootTest(properties = "spring.aop.proxy-target-class=false") // JDK 동적프록시, DI 예외 발생
public class ProxyDITest {

    @Autowired
    MemberService memberService; // 인터페이스로 주입

    @Autowired
    MemberServiceImpl memberServiceImpl; // 구체 타입으로 주입

    @Test
    void go() {
        log.info("memberService class = {}", memberService.getClass());
        memberService.hello("hello");
        log.info("memberServiceImpl class = {}", memberServiceImpl.getClass());
        memberServiceImpl.hello("hello");
    }
}
