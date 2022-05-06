- [`@Aspect AOP`](#aspect-aop)
  - [`@Aspect` 프록시 - 적용](#aspect-프록시---적용)

---

# `@Aspect AOP`

## `@Aspect` 프록시 - 적용

- 스프링 애플리케이션에 프록시를 적용하려면, 포인트컷과 어드바이스로 구성되어있는 어드바이저(Advisor)를 만들어서, 스프링 빈으로 등록하면 된다.
- 그러면 나머지는 앞서 배운 자동 프록시 생성기가 모두 자동으로 처리해준다.
- 자동 프록시 생성기는 스프링 빈으로 등록된 어드바이저들을 찾고, 스프링 빈들에 자동으로 프록시를 적용해준다. (물론 포인트컷이 매칭되는 경우에 프록시를 생성한다.)
- 스프링은 `@Aspect` 애노테이션으로 매우 편리하게 포인트컷과 어드바이스로 구성되어있는 어드바이저 생성 기능을 지원한다.
- 지금까지 직접 만들었던 부분을 `@Aspect` 애노테이션을 사용해서 만들어보자.

> 참고

- `@Aspect`는 관점 지향 프로그래밍(AOP)을 가능하게 하는 AspectJ 프로젝트에서 제공하는 어노테이션이다.
- 스프링은 이것을 차용해서 프록시를 통한 AOP를 가능하게 한다.
- AOP와 AspectJ 관련된 자세한 내용은 다음에 설명한다.
- 지금은 프록시에 초점을 맞추자.
- 우선 이 애노테이션을 사용해서 스프링이 편리하게 프록시를 만들어준다고 생각하면 된다.

```java
@Slf4j
@Aspect
public class LogTraceAspect {

    private final LogTrace logTrace;

    public LogTraceAspect(final LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    // 이 자체가 어드바이저
    @Around("execution(* hello.proxy.app..*(..))") // 포인트컷
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable { // 어드바이스(로직)
        TraceStatus status = null;
        try {
            final String message = joinPoint.getSignature().toShortString();
            status = logTrace.begin(message);
            // target 호출 (로직 호출)
            final Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```

- `@Aspect`: 애노테이션 기반 프록시를 적용할 때 필요하다.
- `@Around("execution(* hello.proxy.app..*(..))")`
  - `@Around`의 값에 포인트값 표현식을 넣는다.
  - 표현식은 AspectJ 표현식을 사용한다.
  - `@Around`의 메서드는 어드바이스(`Advice`)가 된다.
- `ProceedingJoinPoint joinPoint`
  - 어드바이스에서 살펴본 `MethodInvocation invocation`과 유사한 기능이다.
  - 내부에 실제 호출 대상, 전달 인자, 그리고 어떤 객체와 어떤 메서드가 호출되었는지 정보가 포함되어 있다.
- `joinPoint.proceed()` : 실제 호출대상(target)을 호출한다.

> Bean 생성

```java
@Configuration
@Import({ AppV1Config.class, AppV2Config.class })
public class AopConfig {

    @Bean
    public LogTraceAspect logTraceAspect(LogTrace logTrace) {
        return new LogTraceAspect(logTrace);
    }
}
```

- 실행 및 결과

> Application main

```java
@Import(AopConfig.class)
@SpringBootApplication(scanBasePackages = "hello.proxy.app") //주의
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	public LogTrace logTrace(){
		return new ThreadLocalLogTrace();
	}
}
```

- Request

```
http://localhost:8080/v3/request?itemId=DolphaGo
```

- Console log
```log
2022-05-06 12:13:24.424  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] OrderControllerV3.request(..)
2022-05-06 12:13:24.424  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] |-->OrderServiceV3.orderItem(..)
2022-05-06 12:13:24.426  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] |   |-->OrderRepositoryV3.save(..)
2022-05-06 12:13:25.431  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] |   |<--OrderRepositoryV3.save(..) time=1005ms
2022-05-06 12:13:25.432  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] |<--OrderServiceV3.orderItem(..) time=1008ms
2022-05-06 12:13:25.432  INFO 42397 --- [nio-8080-exec-4] h.p.trace.logtrace.ThreadLocalLogTrace   : [1fc107ac] OrderControllerV3.request(..) time=1008ms
```