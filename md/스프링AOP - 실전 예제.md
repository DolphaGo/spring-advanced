# 스프링 AOP - 실전 예제

## 예제 만들기

지금까지 학습한 내용을 활용해서 유용한 스프링 AOP를 만들어보자.

- `@Trace` 애노테이션으로 로그 출력하기
- `@Retry` 애노테이션으로 예외 발생시 재시도 하기

> Repository

```java
@Repository
public class ExamRepository {

    private static int seq = 0;

    /**
     * 5번에 1번 실패하는 요청
     */
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new IllegalStateException("예외 발생");
        }
        return "ok";
    }
}
```

> Service

```java
@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;

    public void request(String itemId) {
        examRepository.save(itemId);
    }
}
```

> Test

```java
@Slf4j
@SpringBootTest
public class ExamTest {

    @Autowired
    ExamService examService;

    @Test
    void test() {
        for (int i = 0; i < 5; i++) {
            log.info("client request i = {}", i);
            examService.request("data" + i);
        }
    }
}
```

## 로그 출력 AOP

- 먼저 로그 출력용 AOP를 만들어보자.
- `@Trace` 가 메서드에 붙어 있으면 호출 정보가 출력되는 편리한 기능이다.

> Trace Annotation

- 메서드를 타겟으로 잡았다.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {
}
```

> Aspect

- 간단히 trace를 남겨두는 용도로 시그니처와 joinPoint에서 argument를 가져와보자.

```java
@Slf4j
@Aspect
public class TraceAspect {

    @Before("@annotation(hello.aop.exam.annotation.Trace)")
    public void doTrace(JoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        log.info("[trace] {} args={}", joinPoint.getSignature(), args);
    }
}
```

> 메서드에 적용

- 간단히 로그를 남길 메서드에 어노테이션을 붙여주면 된다.

Repository

```java
@Repository
public class ExamRepository {

    private static int seq = 0;

    /**
     * 5번에 1번 실패하는 요청
     */
    @Trace
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new IllegalStateException("예외 발생");
        }
        return "ok";
    }
}
```

Service

```java
@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;

    @Trace
    public void request(String itemId) {
        examRepository.save(itemId);
    }
}
```

> 테스트

- 이제 Aspect를 Spring Bean으로 등록해주고, 테스트를 실행해보자.

```java
@Import(TraceAspect.class)
@Slf4j
@SpringBootTest
public class ExamTest {

    @Autowired
    ExamService examService;

    @Test
    void test() {
        for (int i = 0; i < 5; i++) {
            log.info("client request i = {}", i);
            examService.request("data" + i);
        }
    }
}
```

- 실행결과

```log
2022-05-23 02:46:38.502  INFO 20006 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 0
2022-05-23 02:46:38.509  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data0]
2022-05-23 02:46:38.518  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data0]
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 1
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data1]
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data1]
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 2
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data2]
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data2]
2022-05-23 02:46:38.522  INFO 20006 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 3
2022-05-23 02:46:38.523  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data3]
2022-05-23 02:46:38.523  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data3]
2022-05-23 02:46:38.523  INFO 20006 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 4
2022-05-23 02:46:38.523  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data4]
2022-05-23 02:46:38.523  INFO 20006 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data4]

예외 발생
java.lang.IllegalStateException: 예외 발생
```

## 재시도 AOP

- 이번에는 좀 더 의미있는 재시도 AOP를 만들어보자.
- `@Retry` 애노테이션이 있으면 예외가 발생했을 때 다시 시도해서 문제를 복구한다.

> Retry annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    int value() default 3;
}
```

- default 값으로 3을 지정해줬다.
- 메서드 단에 적용하도록 했다.

> Aspect

```java
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
```

- `retry.value()`로 최대 재시도 횟수를 지정할 수 있다.
- 현재 Repository에서는 5번에 1번씩 Exception이 나도록 고의적으로 해놓은 상태이다.
- 그러므로, 만약 Exception이 났다면, 우선 해당 Exception은 ExceptionHolder에 담아두고, 다시 재시도를 한다.
- 이 때 6번 째에서는 에러가 나지 않을 것이므로, 재시도 이후 성공할 것을 예측해볼 수 있다.

> 적용

- Repository에 적용해본다.

```java
@Repository
public class ExamRepository {

    private static int seq = 0;

    /**
     * 5번에 1번 실패하는 요청
     */
    @Trace
    @Retry(4) // 횟수 제한은 반드시 있어야 한다.. 셀프 디도스를 만들수도 있음.
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new IllegalStateException("예외 발생");
        }
        return "ok";
    }
}
```

- `@Retry(value = 4)` 처럼 써도 되고, `value =` 없이 그냥 적어도 된다.
- 이 메서드(save)에 대해서는 최대 4번 재시도 하도록 설정했다.

> 테스트

```java
@Import({ TraceAspect.class, RetryAspect.class })
@Slf4j
@SpringBootTest
public class ExamTest {

    @Autowired
    ExamService examService;

    @Test
    void test() {
        for (int i = 0; i < 5; i++) {
            log.info("client request i = {}", i);
            examService.request("data" + i);
        }
    }
}
```

> 실행 결과

```log
2022-05-23 03:04:04.987  INFO 23571 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 0
2022-05-23 03:04:04.995  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data0]
2022-05-23 03:04:05.003  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data0]
2022-05-23 03:04:05.004  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] String hello.aop.exam.ExamRepository.save(String) retry=@hello.aop.exam.annotation.Retry(value=4)
2022-05-23 03:04:05.004  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=1/4
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 1
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data1]
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data1]
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] String hello.aop.exam.ExamRepository.save(String) retry=@hello.aop.exam.annotation.Retry(value=4)
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=1/4
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 2
2022-05-23 03:04:05.008  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data2]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data2]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] String hello.aop.exam.ExamRepository.save(String) retry=@hello.aop.exam.annotation.Retry(value=4)
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=1/4
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 3
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data3]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data3]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] String hello.aop.exam.ExamRepository.save(String) retry=@hello.aop.exam.annotation.Retry(value=4)
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=1/4
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.ExamTest                  : client request i = 4
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] void hello.aop.exam.ExamService.request(String) args=[data4]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.TraceAspect           : [trace] String hello.aop.exam.ExamRepository.save(String) args=[data4]
2022-05-23 03:04:05.009  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] String hello.aop.exam.ExamRepository.save(String) retry=@hello.aop.exam.annotation.Retry(value=4)
2022-05-23 03:04:05.010  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=1/4
2022-05-23 03:04:05.010  INFO 23571 --- [    Test worker] hello.aop.exam.aop.RetryAspect           : [retry] try count=2/4
```

- 마지막 실행 결과를 보면, 5번째 요청 때 한 번 에러가 나고 재시도 처리해서 2번만에(2/4) 성공한 것을 확인할 수 있다.

## 정리