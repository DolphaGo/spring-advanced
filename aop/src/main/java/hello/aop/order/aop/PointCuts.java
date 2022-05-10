package hello.aop.order.aop;

import org.aspectj.lang.annotation.Pointcut;

public class PointCuts {

    // hello.aop.order 패키지와 하위 패키지
    @Pointcut("execution(* hello.aop.order..*(..))")
    public void allOrder() {} // 포인트컷 시그니처라고 합니다.

    // 클래스 이름 패턴이 *Service 인 것(보통 트랜잭션은 비즈니스 로직 실행할 때 (서비스 계층) 실행하므로)
    @Pointcut("execution(* *..*Service.*(..))")
    public void allService() {}

    // allOrder AND allService
    @Pointcut("allOrder() && allService()")
    public void orderAndService() {}
}
