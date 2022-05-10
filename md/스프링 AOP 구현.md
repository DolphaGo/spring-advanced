- [스프링 AOP 구현](#스프링-aop-구현)
  - [예제 프로젝트 작성](#예제-프로젝트-작성)
  - [스프링 AOP 구현1 - 시작](#스프링-aop-구현1---시작)
  - [스프링 AOP 구현2 - 포인트컷 분리](#스프링-aop-구현2---포인트컷-분리)

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