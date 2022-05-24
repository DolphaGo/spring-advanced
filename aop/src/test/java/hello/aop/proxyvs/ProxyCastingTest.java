package hello.aop.proxyvs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import hello.aop.member.MemberService;
import hello.aop.member.MemberServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyCastingTest {
    @Test
    void jdkProxy() {
        final MemberServiceImpl target = new MemberServiceImpl();
        final ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(false); // JDK 동적 프록시를 사용. 기본이 false이기에 ProxyFactory에선 이 옵션을 명시하지 않으면 JDK 동적 프록시로 생성된다.

        assertThat(AopUtils.isJdkDynamicProxy(proxyFactory.getProxy())).isTrue();

        // 프록시를 인터페이스로 캐스팅 성공
        final MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();

        // JDK 동적 프록시를 구현 클래스를 캐스팅 시도하면 실패합니다. ClassCastException 예외가 발생합니다.
        assertThatThrownBy(() -> {
            final MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;
        }).isInstanceOf(ClassCastException.class);
    }

    @Test
    void cglibProxy() {
        final MemberServiceImpl target = new MemberServiceImpl();
        final ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true); // CGLIB
        assertThat(AopUtils.isCglibProxy(proxyFactory.getProxy())).isTrue();

        // 프록시를 인터페이스로 캐스팅 성공
        final MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();

        // 프록시를 구체 클래스로 캐스팅 성공
        final MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;
    }
}
