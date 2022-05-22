package hello.aop.exam.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import hello.aop.exam.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class RetryAspect {

    //    @Around("@annotation(hello.aop.exam.annotation.Retry)") 이렇게 적어도 되긴 하지만, 파라미터로 받고 싶다면 아래와 같이 하면 된다. (엄청 깔끔해진다. 파라미터에서 타입도 지정된 것만 받는다.)
    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        log.info("[retry] {} retry={}", joinPoint.getSignature(), retry);
        final int maxRetry = retry.value();
        Exception exceptionHolder = null;

        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {
                log.info("[retry] try count={}/{}", retryCount, maxRetry);
                return joinPoint.proceed();
            } catch (Exception e) {
                exceptionHolder = e;
            }
        }
        throw exceptionHolder;
    }
}
