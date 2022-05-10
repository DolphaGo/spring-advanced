- [스프링 AOP 구현](#스프링-aop-구현)
  - [예제 프로젝트 작성](#예제-프로젝트-작성)
  - [스프링 AOP 구현1 - 시작](#스프링-aop-구현1---시작)
  - [스프링 AOP 구현2 - 포인트컷 분리](#스프링-aop-구현2---포인트컷-분리)
  - [스프링 AOP 구현3 - 어드바이스 추가](#스프링-aop-구현3---어드바이스-추가)
  - [스프링 AOP 구현4 - 포인트컷 참조](#스프링-aop-구현4---포인트컷-참조)
  - [스프링 AOP 구현5 - 어드바이스 순서](#스프링-aop-구현5---어드바이스-순서)

---

# 스프링 AOP 구현

## 예제 프로젝트 작성

> Service

```java
@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void orderItem(String itemId) {
        log.info("[orderService] 실행");
        orderRepository.save(itemId);
    }
}
```

> Repository

```java
@Slf4j
@Repository
public class OrderRepository {
    public String save(String itemId) {
        log.info("[orderRepository] 실행"); //저장 로직
        if (itemId.equals("ex")) {
            throw new IllegalStateException("예외 발생!");
        }
        return "ok";
    }
}
```

> Test

```java
@Slf4j
@SpringBootTest
public class AopTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void aopInfo() {
        log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService)); // false
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository)); // false
    }

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Test
    void exception() {
        assertThatThrownBy(() -> orderService.orderItem("ex"))
                .isInstanceOf(IllegalStateException.class);
    }
}

```

## 스프링 AOP 구현1 - 시작


> AspectV1

```java
@Slf4j
@Aspect
public class AspectV1 {

    @Around("execution(* hello.aop.order..*(..))")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature()); // join point signature
        return joinPoint.proceed(); // 실제 타깃이 호출
    }
}
```

- `@Around` 애노테이션 값인 `execution(* hello.aop.order..*(..))` 는 포인트컷
- `@Around` 애노테이션의 메서드인 `doLog` 는 어드바이스(`Advice`)가 된다.
- `execution(* hello.aop.order..*(..))` 는 `hello.aop.order`패키지와 그 하위 패키지(`..`)를 지정하는 AspectJ 포인트컷 표현식이다.
- 이제 OrderService, OrderRepository의 모든 메서드는 AOP 적용의 대상이 된다.
- 참고로, **스프링은 프록시 방식의 AOP를 사용하므로, 프록시를 통하는 메서드만 적용대상이 된다.**

> 참고

- 스프링 AOP는 AspectJ의 문법을 차용하고, 프록시 방식의 AOP를 제공한다.
- AspectJ를 **직접 사용하는 것이 아니다.**
- 스프링 AOP를 사용할 때는 `@Aspect` 애노테이션을 주로 사용하는데, 이 애노테이션도 AspectJ가 제공하는 애노테이션이다.
- `@Aspect`를 포함한 `org.aspectj` 패키지 관련 기능은 `aspectjweaver.jar` 라이브러리가 제공하는 기능이다.
- 앞서 `build.gradle`에 `spring-boot-starter-app`를 포함했는데, 이렇게하면 스프링의 AOP 관련 기능과 함께 `aspectjweaver.jar`도 함께 사용할 수 있게 의존관계에 포함이 된다.
- 그런데 스프링에서는 AspectJ가 제공하는 애노테이션이나 관련 인터페이스만 사용하는 것이고, 실제 AspectJ가 제공하는 컴파일이나 로드타임 위버 등을 사용하는 것은 아니다.
- 스프링은 지금까지 학습한 것처럼, **프록시 방식의 AOP를 사용**한다.


여기서 마무리가 된 것이 아니다. 이 Aspect를 Spring Bean으로 등록해줘야 한다.

```java
@Slf4j
@SpringBootTest
@Import(AspectV1.class) // 빈으로 등록한다.
public class AopTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void aopInfo() {
        log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService)); // true
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository)); // true
    }

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Test
    void exception() {
        assertThatThrownBy(() -> orderService.orderItem("ex"))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

> 실행 결과

- aopInfo

```log
2022-05-10 21:30:55.914  INFO 4777 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderService=true
2022-05-10 21:30:55.914  INFO 4777 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderRepository=true
```

- success

```log
2022-05-10 21:30:55.886  INFO 4777 --- [    Test worker] hello.aop.order.aop.AspectV1             : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-10 21:30:55.898  INFO 4777 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-10 21:30:55.898  INFO 4777 --- [    Test worker] hello.aop.order.aop.AspectV1             : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-10 21:30:55.905  INFO 4777 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
```

- exception

```log
2022-05-10 21:30:55.958  INFO 4777 --- [    Test worker] hello.aop.order.aop.AspectV1             : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-10 21:30:55.958  INFO 4777 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-10 21:30:55.958  INFO 4777 --- [    Test worker] hello.aop.order.aop.AspectV1             : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-10 21:30:55.958  INFO 4777 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
```

![](/images/2022-05-10-21-33-49.png)

<br/>

> 스프링 빈으로 등록하는 방법은 다음과 같다.

- `@Bean`을 사용해서 직접 등록
- `@Component` 컴포넌트 스캔을 사용해서 자동 등록
- `@Import` 주로 설정 파일을 추가할 때 사용(`@Configuration`)

`@Import`는 주로 설정 파일을 추가할 때 사용하지만, 이 기능으로 스프링 빈도 등록할 수 있다. 테스트에서는 버전을 올려가면서 변경할 예정이어서 간단하게 `@Import` 기능을 사용했다.


## 스프링 AOP 구현2 - 포인트컷 분리

`@Around`에 포인트컷 표현식을 직접 넣을 수 있지만, `@Pointcut` 애노테이션을 사용해서 별도로 분리할 수도 있다.

```java
@Slf4j
@Aspect
public class AspectV2 {

    // hello.aop.order 패키지와 하위 패키지
    @Pointcut("execution(* hello.aop.order..*(..))")
    private void allOrder(){} // 포인트컷 시그니처라고 합니다.

    @Around("allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature()); // join point signature
        return joinPoint.proceed(); // 실제 타깃이 호출
    }
}
```

> Test

`@Import(AspectV2.class)` 로 빈으로 등록하여 테스트하면 V1과 같은 결과가 출력된다.

**`@Pointcut`**

- `@Pointcut`에 포인트컷 표현식을 사용한다.
- **메서드 이름과 파라미터를 합쳐서 포인트컷 시그니처(signature)라 한다.**
- **메서드의 반환 타입은 `void` 여야 한다.**
- **코드 내용은 비워둔다.**
- 포인트컷 시그니처는 `allOrder()` 이다. 이름 그대로 주문과 관련된 모든 기능을 대상으로 하는 포인트컷이다.
- `@Around` 어드바이스에서는 포인트컷을 직접 지정해도 되지만, 포인트컷 시그니처를 사용해도 된다. 여기서는 `@Around("allOrder()")` 를 사용한다.
- `private`, `public` 같은 접근 제어자는 내부에서만 사용하면 private를 사용해도 되지만, 다른 애스펙트에서 참고하려면 public을 사용하자.

결과적으로 `AspectV1`과 같은 기능을 수행한다. 이렇게 분리하면 하나의 포인트컷 표현식을 여러 어드바이스에서 함께 사용 가능하다.

## 스프링 AOP 구현3 - 어드바이스 추가

- 앞서 로그를 출력하는 기능에 추가로 트랜잭션을 적용하는 코드도 추가해본다.
- 여기서 진짜 트랜잭션을 실행하는 것은 아니고, 기능이 동작하는 것처럼 로그만 남겨보려 한다.

> 일반적인 트랜잭션 기능 동작 과정

- 핵심 로직 실행 직전에 트랜잭션을 시작
- 핵심 로직 실행
- 핵심 로직 실행에 문제가 없으면 커밋
- 핵심 로직 실행에 예외가 발생하면 롤백

```java
@Slf4j
@Aspect
public class AspectV3 {

    // hello.aop.order 패키지와 하위 패키지
    @Pointcut("execution(* hello.aop.order..*(..))")
    private void allOrder() {} // 포인트컷 시그니처라고 합니다.

    // 클래스 이름 패턴이 *Service 인 것(보통 트랜잭션은 비즈니스 로직 실행할 때 (서비스 계층) 실행하므로)
    @Pointcut("execution(* *..*Service.*(..))")
    private void allService() {}

    @Around("allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature()); // join point signature
        return joinPoint.proceed(); // 실제 타깃이 호출
    }

    // hello.aop.order 패키지와 하위 패키지 이면서, 동시에 클래스 이름 패턴이 *Service인 것
    @Around("allOrder() && allService()")
    public Object doTranscation(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
            final Object result = joinPoint.proceed();
            log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
            return result;
        } catch (Exception e) {
            log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
            throw e;
        } finally {
            log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
        }
    }
}
```

- `allOrder()` 포인트 컷은 `hello.aop.order` 패키지와 하위 패키지를 대상으로 한다.
- `allService()` 포인트 컷은 타입 이름 패턴이 *Service 를 대상으로 하는데, 쉽게 이야기해서 `XxxService` 처럼 `Service`로 끝나는 것을 대상으로 한다. 물론 `*Servi*` 와 같은 패턴도 가능하다.
- 여기서 타입 이름 패턴이라고 한 이유는, **클래스, 인터페이스에 모두 적용되기 때문이다.**

**`@Around("allOrder() && allService()")`**

- 포인트 컷은 위와 같이 조합할 수 있다.
  - `&&(AND)`, `||(OR)`, `!(NOT)` 3가지 조합이 가능하다.
- `hello.aop.order` 패키지와 하위 패키지이면서 타입 이름 패턴이 `*Service` 인 것을 대상으로 한다.
- 결과적으로 `doTransaction()` 어드바이스는 `OrderService`에만 적용된다.
- `doLog()` 어드바이스는 `OrderService`, `OrderRepository` 에 모두 적용된다.

> 테스트 코드

```java

@Slf4j
@SpringBootTest
@Import(AspectV3.class)
public class AopTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void aopInfo() {
        log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService));
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository));
    }

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Test
    void exception() {
        assertThatThrownBy(() -> orderService.orderItem("ex"))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

- AopInfo 실행 결과

```log
2022-05-11 02:44:19.232  INFO 32277 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderService=true
2022-05-11 02:44:19.232  INFO 32277 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderRepository=true
```

- success 실행 결과

```log
2022-05-11 02:44:19.201  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.202  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [트랜잭션 시작] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.214  INFO 32277 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 02:44:19.214  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-11 02:44:19.220  INFO 32277 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 02:44:19.220  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [트랜잭션 커밋] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.221  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [리소스 릴리즈] void hello.aop.order.OrderService.orderItem(String)
```

- exception 실행 결과

```java
2022-05-11 02:44:19.280  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [트랜잭션 시작] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [트랜잭션 롤백] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 02:44:19.281  INFO 32277 --- [    Test worker] hello.aop.order.aop.AspectV3             : [리소스 릴리즈] void hello.aop.order.OrderService.orderItem(String)
```

> 동작 과정

![](/images/2022-05-11-02-50-33.png)

- AOP 적용 전

클라이언트 -> `orderService.orderItem()` -> `orderRepository.save()`

- AOP 적용 후
클라이언트 -> `[ doLog() -> doTransaction()]` -> `orderService.orderItem()` -> 
`[ doLog() ]` -> `orderRepository.save()`

---

OrderService엔 2개의 어드바이스(`doLog(), doTransaction()`), OrderRepository에는 하나의 어드바이스(`doLog()`)가 적용된 것을 확인할 수 있다.


실행 결과를 분석해보면 예외 상황에서는 트랜잭션 커밋 대신 **트랜잭션 롤백**이 호출되는 것을 확인할 수 있다.
그런데 여기에서 로그를 남기는 순서가 `[ doLog() doTransaction() ]` 순서로 작동한다. 

**만약 어드바이스가 적용되는 순서를 변경하고 싶으면 어떻게 하면 될까?** 예를 들어서 실행 시간을 측정해야 하는데 트랜잭션과 관련된 시간을 제외하고 측정하고 싶다면 `[ doTransaction() doLog() ]` 이렇게 트랜잭션 이후에 로그를 남겨야 할 것이다.

그 전에 잠깐 포인트컷을 외부로 빼서 사용하는 방법을 먼저 알아보자.

## 스프링 AOP 구현4 - 포인트컷 참조

다음과 같이 포인트컷을 공용으로 사용하기 위해 별도의 외부 클래스에 모아두어도 된다.
참고로, 외부에서 호출할 때는 포인트컷의 접근 제어자를 `public` 으로 열어두어야 한다.

```java
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
```

```java
@Slf4j
@Aspect
public class AspectV4PointCut {

    @Around("hello.aop.order.aop.PointCuts.allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature()); // join point signature
        return joinPoint.proceed(); // 실제 타깃이 호출
    }

    // hello.aop.order 패키지와 하위 패키지 이면서, 동시에 클래스 이름 패턴이 *Service인 것
    @Around("hello.aop.order.aop.PointCuts.orderAndService()")
    public Object doTranscation(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
            final Object result = joinPoint.proceed();
            log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
            return result;
        } catch (Exception e) {
            log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
            throw e;
        } finally {
            log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
        }
    }
}
```

> 테스트

```java
@Slf4j
@SpringBootTest
@Import(AspectV4PointCut.class)
public class AopTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void aopInfo() {
        log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService));
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository));
    }

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Test
    void exception() {
        assertThatThrownBy(() -> orderService.orderItem("ex"))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

- `패키지명.클래스이름.포인트컷이름` 으로 사용할 수 있다.
- 실행 결과는 V3과 같다.

## 스프링 AOP 구현5 - 어드바이스 순서

- **어드바이스는 기본적으로 순서를 보장하지 않는다.**
- 순서를 지정하고 싶으면, `@Aspect` 적용 단위로 **`org.springframework.core.annotation.@Order`** 애노테이션을 적용해야 한다.
- 문제는 이것을 **어드바이스 단위가 아니라 클래스 단위로 적용할 수 있다는 점**이다.
- 그래서 지금처럼 하나의 애스펙트에 여러 어드바이스가 있으면, 순서를 보장받을 수 없게 된다.
- 따라서 **애스펙트를 별도의 클래스로 분리**해야 한다.

현재 로그를 남기는 순서가 doLog -> doTransaction 으로 실행된다.
반대로 실행해보도록 해보자.

```java
@Slf4j
public class AspectV5Order {

    @Aspect
    @Order(2)
    public static class LogAspect {
        @Around("hello.aop.order.aop.PointCuts.allOrder()")
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[log] {}", joinPoint.getSignature()); // join point signature
            return joinPoint.proceed(); // 실제 타깃이 호출
        }
    }

    @Aspect
    @Order(1)
    public static class TxAspect {
        // hello.aop.order 패키지와 하위 패키지 이면서, 동시에 클래스 이름 패턴이 *Service인 것
        @Around("hello.aop.order.aop.PointCuts.orderAndService()")
        public Object doTranscation(ProceedingJoinPoint joinPoint) throws Throwable {
            try {
                log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
                final Object result = joinPoint.proceed();
                log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
                return result;
            } catch (Exception e) {
                log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
                throw e;
            } finally {
                log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
            }
        }
    }
}
```

> 테스트

```java
@Slf4j
@SpringBootTest
@Import({ AspectV5Order.LogAspect.class, AspectV5Order.TxAspect.class })
public class AopTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void aopInfo() {
        log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService));
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository));
    }

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Test
    void exception() {
        assertThatThrownBy(() -> orderService.orderItem("ex"))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

> 실행 결과

- aopInfo

```log
2022-05-11 03:07:26.550  INFO 37345 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderService=true
2022-05-11 03:07:26.550  INFO 37345 --- [    Test worker] hello.aop.AopTest                        : isAopProxy, orderRepository=true
```

- success

```log
2022-05-11 03:07:26.525  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [트랜잭션 시작] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.526  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.536  INFO 37345 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 03:07:26.536  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-11 03:07:26.540  INFO 37345 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 03:07:26.541  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [트랜잭션 커밋] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.541  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [리소스 릴리즈] void hello.aop.order.OrderService.orderItem(String)
```

- exception

```log
2022-05-11 03:07:26.606  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [트랜잭션 시작] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.606  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [log] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.606  INFO 37345 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 03:07:26.606  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [log] String hello.aop.order.OrderRepository.save(String)
2022-05-11 03:07:26.606  INFO 37345 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 03:07:26.607  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [트랜잭션 롤백] void hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:07:26.607  INFO 37345 --- [    Test worker] hello.aop.order.aop.AspectV5Order        : [리소스 릴리즈] void hello.aop.order.OrderService.orderItem(String)
```

- `@Order`는 클래스 단위로 실행된다. 숫자가 낮을수록 우선순위가 높다.
- 실행 순서는 Order가 낮을수록 먼저 시작되지만, 다시 되돌아 올때는 역순으로 실행된다(Order가 높은 순으로)

![](/images/2022-05-11-03-10-13.png)

