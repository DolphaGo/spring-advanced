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

## 정리