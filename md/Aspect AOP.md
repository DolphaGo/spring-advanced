- [`@Aspect AOP`](#aspect-aop)
  - [`@Aspect` 프록시 - 적용](#aspect-프록시---적용)
  - [`@Aspect` 프록시 - 설명](#aspect-프록시---설명)

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

## `@Aspect` 프록시 - 설명

- 앞서 자동 프록시 생성기를 학습할 때 자동 프록시 생성기(`AnnotationAwareAspectJAutoProxyCreator`)는 `Advisor`를 자동으로 찾아와서 필요한 곳에 프록시를 생성하고 적용해준다고 했다.
- 자동 프록시 생성기는 여기에 추가로 하나의 역할을 더 하는데, 바로 `@Aspect`를 찾아서 이것을 `Advisor`로 만들어준다.
- 쉽게 이야기해서 지금까지 학습한 기능에 `@Aspect`를 `Advisor`로 변환해서 저장하는 기능도 한다.
- 그래서 이름 앞에 `AnnotationAware` 가 붙어 있는 것이다.

![](/images/2022-05-06-12-18-49.png)

**자동 프록시 생성기는 2가지 일을 한다**
1. `@Aspect`를 보고 어드바이저(`Advisor`)로 변환해서 저장한다.
2. 어드바이저를 기반으로 프록시를 생성한다.

> `@Aspect`를 보고 어드바이저(`Advisor`)로 변환해서 저장하는 과정

![](/images/2022-05-06-13-17-47.png)

1. 실행: 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출한다.
2. 모든 `@Aspect` 빈 조회: 자동 프록시 생성기는 스프링 컨테이너에서 `@Aspect` 애노테이션이 붙은 스프링 빈을 모두 조회한다.
3. 어드바이저 생성: `@Aspect` 어드바이저 빌더를 통해 `@Aspect` 애노테이션 정보를 기반으로 어드바이저를 생성한다.
4. `@Aspect 기반 어드바이저 저장`: 생성한 어드바이저를 `@Aspect` 어드바이저 빌더 내부에 저장한다.

**`@Aspect` 어드바이저 빌더**

- `BeanFactoryAspectJAdvisorsBuilder` 클래스
- `@Aspect`의 정보를 기반으로 포인트컷, 어드바이스, 어드바이저를 생성하고 보관하는 것을 담당
- `@Aspect`의 정보를 기반으로 어드바이저를 만들고, `@Aspect` 어드바이저 빌더 내부 저장소에 캐시한다.
- 캐시에 어드바이저가 이미 만들어져 있는 경우 캐시에 저장된 어드바이저를 반환한다.

> 어드바이저를 기반으로 프록시를 생성

![](/images/2022-05-06-13-20-56.png)

**자동 프록시 생성기의 작동 과정을 알아보자**

1. **생성**: 스프링 빈 대상이 되는 객체를 생성한다.(`@Bean`, 컴포넌트 스캔 모두 포함)
2. **전달**: 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.
3. **Advisor 조회**
   1. Advisor 빈 조회: 스프링 컨테이너에서 Advisor빈을 모두 조회한다.
   2. `@Aspect` Advisor 조회: `@Aspect` 어드바이저 빌더 내부에 저장된 Advisor를 모두 조회한다.
4. **프록시 적용 대상 체크**: 앞서 3-1, 3-2에서 조회한 Advisor에 포함되어 있는 포인트컷을 사용해서 해당 객체가 프록시를 적용할 대상인지 아닌지 판단한다. 이때 객체의 클래스 정보는 물론이고, 해당 객체의 모든 메서드를 포인트컷 하나하나 모두 매칭해본다. 그래서 조건이 하나라도 만족한다면 프록시 적용 대상이 된다. 예를 들어 메서드 하나만 포인트컷 조건에 만족해도 프록시 적용대상이 된다.
5. **프록시 생성**: 프록시 적용 대상이면 프록시를 생성하고 프록시를 반환한다. 그래서 프록시를 스프링 빈으로 등록한다. 만약 프록시 적용 대상이 아니라면 원본 객체를 반환해서 원본 객체를 스프링 빈으로 등록한다.
6. **빈 등록**: 반환된 객체는 스프링 빈으로 등록된다.