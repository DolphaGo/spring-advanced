package hello.aop.order.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class AspectV6Advice {

    // hello.aop.order 패키지와 하위 패키지 이면서, 동시에 클래스 이름 패턴이 *Service인 것
//    @Around("hello.aop.order.aop.PointCuts.orderAndService()")
//    public Object doTranscation(ProceedingJoinPoint joinPoint) throws Throwable {
//        try {
//            // @Before
//            log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
//            final Object result = joinPoint.proceed();
//            // @AfterReturning
//            log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
//            return result;
//        } catch (Exception e) {
//            // @AfterThrowing
//            log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
//            throw e;
//        } finally {
//            // @After
//            log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
//        }
//    }

//    @Before("hello.aop.order.aop.PointCuts.orderAndService()")
//    public void doBefore() { // JoinPoint 라는 매개변수를 받지 않아도 된다.
//        log.info("[Before] {}", "hello");
//    }

    @Before("hello.aop.order.aop.PointCuts.orderAndService()")
    public void doBefore(JoinPoint joinPoint) { // Before는 joinPoint.proceed 처럼 따로 실행하지 않아도 실행해줍니다.
        log.info("[Before] {}", joinPoint.getSignature());
    }

    @AfterReturning(value = "hello.aop.order.aop.PointCuts.orderAndService()", returning = "result")
    public void doReturn(JoinPoint joinPoint, Object result) { // 매개변수의 result와 @AfterReturning에 정의한 result와 이름 매칭이 되어, 결과값을 받아올 수 있다.
        // return을 쓸 수는 있지만, 리턴하지 않기 때문에 이 결과값을 바꿀 수가 없습니다.
        log.info("[AfterReturning] {} return = {}", joinPoint.getSignature(), result);
    }

    @AfterReturning(value = "hello.aop.order.aop.PointCuts.allOrder()", returning = "result")
    public void doReturn(JoinPoint joinPoint, Integer result) {
        // 여기서 result에 매핑될 수 있는 값이 없기 때문에, 이 메서드가 호출 자체가 안됩니다.
        log.info("[AfterReturning - TypeTest] {} return = {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(value = "hello.aop.order.aop.PointCuts.orderAndService()", throwing = "ex")
    public void doThrowing(JoinPoint joinPoint, Exception ex) { // 매개변수의 result와 @AfterReturning에 정의한 result와 이름 매칭이 되어, 결과값을 받아올 수 있다.
        log.info("[AfterThrowing] {}", joinPoint.getSignature(), ex);
    }

    @After("hello.aop.order.aop.PointCuts.orderAndService()")
    public void doAfter(JoinPoint joinPoint) {
        log.info("[After] {} ", joinPoint.getSignature());
    }
}
