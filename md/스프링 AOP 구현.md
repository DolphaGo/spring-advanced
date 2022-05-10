- [스프링 AOP 구현](#스프링-aop-구현)
  - [예제 프로젝트 작성](#예제-프로젝트-작성)
  - [스프링 AOP 구현1 - 시작](#스프링-aop-구현1---시작)
  - [스프링 AOP 구현2 - 포인트컷 분리](#스프링-aop-구현2---포인트컷-분리)
  - [스프링 AOP 구현3 - 어드바이스 추가](#스프링-aop-구현3---어드바이스-추가)
  - [스프링 AOP 구현4 - 포인트컷 참조](#스프링-aop-구현4---포인트컷-참조)
  - [스프링 AOP 구현5 - 어드바이스 순서](#스프링-aop-구현5---어드바이스-순서)
  - [스프링 AOP 구현6- 어드바이스 종류](#스프링-aop-구현6--어드바이스-종류)
    - [@Before](#before)
    - [@AfterReturning](#afterreturning)
    - [@AfterThrowing](#afterthrowing)
    - [@After](#after)
    - [@Around](#around)

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

    public String orderItem(String itemId) {
        log.info("[orderService] 실행");
        return orderRepository.save(itemId);
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


## 스프링 AOP 구현6- 어드바이스 종류

- 어드바이스는 앞서 살펴본 `@Around` 외에도 여러가지 종류가 있다.

**어드바이스 종류**

- `@Around` : 메서드 호출 전후에 수행, 가장 강력한 어드바이스, 조인 포인트 실행 여부 선택, 반환 값 변환, 예외 변환 등이 가능
- `@Before` : 조인 포인트 실행 이전에 실행
- `@AfterReturning` : 조인 포인트가 정상 완료후 실행
- `@AfterThrowing` : 메서드가 예외를 던지는 경우 실행
- `@After`: 조인 포인트가 정상 또는 예외에 관계 없이 실행(finally)

```java
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

    @AfterThrowing(value = "hello.aop.order.aop.PointCuts.orderAndService()", throwing = "ex")
    public void doThrowing(JoinPoint joinPoint, Exception ex) { // 매개변수의 result와 @AfterReturning에 정의한 result와 이름 매칭이 되어, 결과값을 받아올 수 있다.
        log.info("[AfterThrowing] {}", joinPoint.getSignature(), ex);
    }

    @After("hello.aop.order.aop.PointCuts.orderAndService()")
    public void doAfter(JoinPoint joinPoint) {
        log.info("[doAfter] {} ", joinPoint.getSignature());
    }
}
```

> 테스트

```java
@Slf4j
@SpringBootTest
@Import({ AspectV6Advice.class })
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

- success 실행 결과

```log
2022-05-11 03:30:34.376  INFO 42193 --- [    Test worker] hello.aop.order.aop.AspectV6Advice       : [Before] String hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:30:34.385  INFO 42193 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 03:30:34.385  INFO 42193 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 03:30:34.386  INFO 42193 --- [    Test worker] hello.aop.order.aop.AspectV6Advice       : [AfterReturning] String hello.aop.order.OrderService.orderItem(String) return = ok
2022-05-11 03:30:34.386  INFO 42193 --- [    Test worker] hello.aop.order.aop.AspectV6Advice       : [doAfter] String hello.aop.order.OrderService.orderItem(String) 
```

- exception 실행결과

```log
2022-05-11 03:31:18.970  INFO 42351 --- [    Test worker] hello.aop.order.aop.AspectV6Advice       : [Before] String hello.aop.order.OrderService.orderItem(String)
2022-05-11 03:31:18.970  INFO 42351 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-11 03:31:18.970  INFO 42351 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
2022-05-11 03:31:18.975  INFO 42351 --- [    Test worker] hello.aop.order.aop.AspectV6Advice       : [AfterThrowing] String hello.aop.order.OrderService.orderItem(String)

java.lang.IllegalStateException: 예외 발생!
	at hello.aop.order.OrderRepository.save(OrderRepository.java:13) ~[main/:na]
	at hello.aop.order.OrderService.orderItem(OrderService.java:18) ~[main/:na]
```

> ### 정리

- 복잡해보이지만, 사실 `@Around`를 제외한 나머지 어드바이스들은 `@Around`가 할 수 있는 일의 일부만 제공할 뿐이다.
- 따라서 `@Around` 어드바이스만 사용해도, 필요한 기능을 모두 수행할 수 있다.

**참고 정보 획득**

- 모든 어드바이스는 `org.aspectj.lang.JoinPoint`를 첫 번째 파라미터에 사용할 수 있다(생략 가능하다)
- 단, `@Around`는 `ProceedingJoinPoint`를 사용해야 한다.

참고로, `ProceedingJoinPoint`는 `org.aspectj.lang.JoinPoint` 의 하위 타입이다.

> **JoinPoint 인터페이스의 주요 기능**

- `getArgs()` : 메서드 인수를 반환
- `getThis()` : 프록시 객체를 반환
- `getTarget()` : 대상 객체를 반환
- `getSignature()` : 조언되는 메서드에 대한 설명을 반환
- `toString()` : 조언되는 방법에 대한 유용한 설명을 인쇄

> **ProceedingJoinPoint 인터페이스의 주요 기능**

- **`proceed()`: 다음 어드바이스나 타겟을 호출한다.**

추가로 호출시 전달한 매개변수를 파라미터를 통해서도 전달 받을 수도 있는데, 이 부분은 뒤에서 설명한다.

### @Before

```java
@Before("hello.aop.order.aop.PointCuts.orderAndService()")
public void doBefore(JoinPoint joinPoint) { // Before는 joinPoint.proceed 처럼 따로 실행하지 않아도 실행해줍니다.
    log.info("[Before] {}", joinPoint.getSignature());
}
```

- 조인 포인트 실행 전에 실행된다.
- `@Around`와 다르게 작업 흐름을 변경할 수 없다.
- `@Around`는 `ProceedingJoinPoint.proceed()` 를 호출해야, 다음 대상이 호출된다.
- 만약 호출하지 않으면 다음 대상이 호출되지 않는다.
- 반면에, `@Before` 는 `ProceedingJoinPoint.proceed()` 자체를 사용하지 않는다.
- 메서드 종료시 자동으로 다음 타겟이 호출된다.
- 물론 예외가 발생하면, 다음 코드가 호출되지는 않는다.

### @AfterReturning

```java
@AfterReturning(value = "hello.aop.order.aop.PointCuts.orderAndService()", returning = "result")
public void doReturn(JoinPoint joinPoint, Object result) { // 매개변수의 result와 @AfterReturning에 정의한 result와 이름 매칭이 되어, 결과값을 받아올 수 있다.
    // return을 쓸 수는 있지만, 리턴하지 않기 때문에 이 결과값을 바꿀 수가 없습니다.
    log.info("[AfterReturning] {} return = {}", joinPoint.getSignature(), result);
}
```

- 메서드 실행이 정상적으로 반환될 때 실행
- `returning` 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 한다.
- `returning` 절에 지정된 타입의 값을 반환하는 메서드만 대상으로 실행한다. (부모 타입을 지정하면 모든 자식 타입은 인정된다.)

```java
@AfterReturning(value = "hello.aop.order.aop.PointCuts.allOrder()", returning = "result")
public void doReturn(JoinPoint joinPoint, Integer result) {
    // 여기서 result에 매핑될 수 있는 값이 없기 때문에, 이 메서드가 호출 자체가 안됩니다.
    log.info("[AfterReturning - TypeTest] {} return = {}", joinPoint.getSignature(), result);
}
```

- `@Around`와 다르게 반환되는 객체를 변경할 수 없다.
  - 반환 객체를 변경하려면 `@Around`를 사용해야 한다. 참고로 반환 객체를 조작할 수는 있다.

### @AfterThrowing

```java
@AfterThrowing(value = "hello.aop.order.aop.PointCuts.orderAndService()", throwing = "ex")
public void doThrowing(JoinPoint joinPoint, Exception ex) { // 매개변수의 result와 @AfterReturning에 정의한 result와 이름 매칭이 되어, 결과값을 받아올 수 있다.
    log.info("[AfterThrowing] {}", joinPoint.getSignature(), ex);
}
```

- 메서드 실행이 예외를 던져서 종료될 때 실행
- `throwing` 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 한다.
- `throwing` 절에 지정된 타입과 맞은 예외를 대상으로 실행한다.(부모 타입을 지정하면 모든 자식 타입은 인정된다.)

### @After

```java
@After("hello.aop.order.aop.PointCuts.orderAndService()")
public void doAfter(JoinPoint joinPoint) {
    log.info("[After] {} ", joinPoint.getSignature());
}
```

- 메서드 실행이 종료되면 실행된다(`finally`를 생각하면 된다.)
- 정상 및 예외 반환 조건을 모두 처리한다.
- 일반적으로 리소스를 해제하는 데 사용한다.

### @Around

```java
@Around("hello.aop.order.aop.PointCuts.orderAndService()")
public Object doTranscation(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
        // @Before
        log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
        final Object result = joinPoint.proceed();
        // @AfterReturning
        log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
        return result;
    } catch (Exception e) {
        // @AfterThrowing
        log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
        throw e;
    } finally {
        // @After
        log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
    }
}
```

- 메서드 실행 주변에서 실행된다. 메서드 실행 전후에 작업을 수행한다.
- 가장 강력한 어드바이스
  - 조인 포인트 실행 여부 선택 : `joinPoint.proceed()`
  - 전달 값 변환: `joinPoint.proceed(args[])`
  - 반환 값 변환
  - 예외 변환
  - 트랜잭션처럼 `try~catch~finally` 모두 들어가는 구분 처리 가능
- 어드바이스의 첫 번째 파라미터는 `ProceedingJoinPoint`를 사용해야 한다.
- `proceed()` 를 통해 대상을 실행한다.
- `proceed()` 를 여러번 실행할 수도 있음(재시도)

![](/images/2022-05-11-04-14-31.png)

**순서**

- 스프링은 `5.2.7` 버전부터 `동일한 @Aspect` 안에서 동일한 조인포인트의 우선순위를 정했다.
- 실행 순서: `@Around`, `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`
- 어드바이스가 적용되는 순서는 이렇게 적용되지만, 호출 순서와 리턴 순서는 반대라는 점을 알아두자. (After의 의미와, `@After`가 가장 마지막인 것을 생각할 것)
- **물론 `@Aspect` 안에 동일한 종류의 어드바이스가 2개 있으면 순서가 보장되지 않는다.** 이 경우 앞서 배운 것 처럼 `@Aspect`를 분리하고 `@Order` 를 적용하자.


> **@Around 외에 다른 어드바이스가 존재하는 이유**

- `@Around` 만 있어도 모든 기능을 수행할 수 있는데... 다른 어드바이스들이 존재하는 이유는 무엇일까?

```java
@Around("hello.aop.order.aop.Pointcuts.orderAndService()")
public void doBefore(ProceedingJoinPoint joinPoint) {
    log.info("[before] {}", joinPoint.getSignature());
}
```

- 위 코드는 타겟을 호출하지 않는 문제가 있다.
- 이 코드를 개발한 의도는 타겟 실행 전에 로그를 출력하는 것이다.
- 그런데 **`@Around` 는 항상 `joinPoint.proceed()`를 호출해야한다.**
- 만약 실수로 호출하지 않으면 치명적인 버그가 발생할 수 있다.

이번엔 다음 코드를 보자.

```java
@Before("hello.aop.order.aop.Pointcuts.orderAndService()")
public void doBefore(JoinPoint joinPoint) {
    log.info("[before] {}", joinPoint.getSignature());
}
```

- `@Before`는 `joinPoint.proceed()`를 호출하는 고민을 하지 않아도 된다. (프레임워크에서 자체적으로 proceed를 해주므로)

**`@Around는` 가장 넓은 기능을 제공하는 것은 맞지만, 실수할 가능성이 있다. 반면에 `@Before`, `@Around` 같은 어드바이스는 기능은 적지만, 실수할 가능성이 낮고, 코드도 단순하다. 그리고 가장 중요한 점은 코드를 작성한 의도가 명확하게 드러난다는 점이다. `@Before` 라는 애노테이션을 보는 순간, 아 이 코드는 타겟 실행 전에 한정해서 어떤 일을 하는 코드구나 라는 것을 비교적 쉽게 알아차릴 수 있다.**

> **좋은 설계는 제약이 있는 것이다.**

- `@Around`만 있으면 되는데 왜, 이렇게 제약을 두는가?
- 제약은 실수를 미연에 방지한다.
  - 일종의 가이드 역할을 한다.
- 만약 `@Around` 를 사용했는데, 중간에 다른 개발자가 해당 코드를 수정해서 호출하지 않았다면? 큰 장애가 발생할 것이다.
- 처음부터 `@Before를` 사용했다면 이런 문제 자체가 발생하지 않는다.
- 제약 덕분에 역할이 명확해지고, 다른 개발자도 이 코드를 보고 고민해야 하는 범위가 줄어들고 코드의 의도도 파악하기가 쉬워진다.