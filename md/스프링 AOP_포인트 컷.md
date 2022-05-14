- [스프링 AOP - 포인트컷](#스프링-aop---포인트컷)
  - [포인트컷 지시자](#포인트컷-지시자)
  - [예제 만들기](#예제-만들기)
  - [excecution - 1](#excecution---1)
  - [excecution - 2](#excecution---2)
  - [within](#within)
  - [args](#args)
  - [@target, @within](#target-within)
  - [@annotation, @args](#annotation-args)
  - [bean](#bean)
  - [매개변수 전달](#매개변수-전달)
  - [this, target](#this-target)
  - [정리](#정리)

---

# 스프링 AOP - 포인트컷

## 포인트컷 지시자

AspectJ는 포인트컷을 편리하게 표현하기 위한 특별한 표현식을 제공한다.
ex) `@Pointcut("execution(* hello.aop.order..*(..))")`

포인트컷 표현식은 AspectJ pointcut expression 즉, 애스펙트 J가 제공하는 포인트컷 표현식을 줄여서 말하는 것이다.

**포인트컷 지시자**

- 포인트컷 표현식은 `excecution` 같은 포인트컷 지시자(Pointcut Designator)로 시작한다. 줄여서 PCD라고 한다.

> **포인트컷 지시자의 종류**

- `execution` : 메소드 실행 조인 포인트를 매칭한다. 스프링 AOP에서 가장 많이 사용하고, 기능도 복잡하다.
- `within` : 특정 타입 내의 조인 포인트를 매칭한다.
- `args` : 인자가 주어진 타입의 인스턴스인 조인 포인트
- `this` : 스프링 빈 객체(스프링 AOP 프록시)를 대상으로 하는 조인 포인트
- `target` : Target 객체(스프링 AOP 프록시가 가르키는 실제 대상)를 대상으로 하는 조인 포인트
- `@target` : 실행 객체의 클래스에 주어진 타입의 애노테이션이 있는 조인 포인트
- `@within` : 주어진 애노테이션이 있는 타입 내 조인 포인트
- `@annotation` : 메서드가 주어진 애노테이션을 가지고 있는 조인 포인트를 매칭
- `@args` : 전달된 실제 인수의 런타임 타입이 주어진 타입의 애노테이션을 갖는 조인 포인트
- `bean` : 스프링 전용 포인트컷 지시자, 빈의 이름으로 포인트컷을 지정한다.

포인트컷 지시자가 무엇을 뜻하는지, 사실 글로만 보면 이해하기가 쉽지 않기에, 예제를 통해 하나씩 이해해보자.

execution은 가장 많이 사용하고, 나머지는 자주 사용하지는 않는다. 따라서 execution을 중점적으로 이해하자.

## 예제 만들기

- ClassAop

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) // 실행할 때까지 어노테이션이 살아있는 것
public @interface ClassAop {
}
```

- MethodAop

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 참고로 Compile 같은 것을 하면, 실행시점엔 사라지게 됨
public @interface MethodAop {
    String value();
}
```

- MemberService

```java
public interface MemberService {
    String hello(String param);
}
```

- MemberServiceImpl

```java
@ClassAop
@Component // AOP를 쓰려면 스프링 빈으로 등록되어야 하므로, 자동 컴포넌트 스캔의 대상이 되도록 한다.
public class MemberServiceImpl implements MemberService {

    @Override
    @MethodAop("test value")
    public String hello(final String param) {
        return "ok";
    }

    public String internal(String param) {
        return "ok";
    }
}
```

> Test

```java
@Slf4j
public class ExecutionTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }

    @Test
    void printMethod() {
        // helloMethod=public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
        log.info("helloMethod={}", helloMethod);
    }
}
```

- `AspectJExpressionPointcut`이 바로, 포인트컷 표현식을 처리해주는 클래스.
- 여기에 포인트컷 표현식을 지정해주면 된다.
- `AspectJExpressionPointcut`는 상위에 `Pointcut` 인터페이스를 가진다.
![](/images/2022-05-12-03-20-15.png)
- 위에서 출력한 printMethod() 테스트는, MemberServiceImpl을 reflection으로 가져와서 메서드의 정보를 출력해준다.
- 여기에서 출력된 것에 주목해보자.

```
public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
```

- 이번에 알아볼 `execution` 으로 시작하는 포인트컷 표현식은 이 메서드 정보를 매칭해서 포인트컷 대상을 찾아내는 것이다!

## excecution - 1

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)

execution(접근제어자? 반환타입 선언타입?메서드이름(파라미터) 예외?)
```

- 메소드 실행 조인 포인트를 매칭한다.
- `?`는 생략할 수 있다.
- `*`같은 패턴을 지정할 수 있다.

```java
@Slf4j
public class ExecutionTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }

    @Test
    void printMethod() {
        // helloMethod=public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
        log.info("helloMethod={}", helloMethod);
    }

    @Test
    void exactMatch() {
        // helloMethod=public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
        pointcut.setExpression("execution(public String hello.aop.member.MemberServiceImpl.hello(String))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }
}
```

- `AspectJExpressionPointcut` 에 `pointcut.setExpression` 을 통해서 포인트컷 표현식을 적용할 수 있다.

- `pointcut.matches(메서드,대상 클래스`를 실행하면 지정한 포인트컷 표현식의 매칭여부를 `true`, `false` 로 반환한다.

> 매칭 조건

- 접근제어자?: public
- 반환타입: String
- 선언타입?: hello.aop.member.MemberServiceImpl
- 메서드이름: hello
- 파라미터: (String)
- 예외?: 생략

`MemberServiceImpl.hello(String)` 메서드와 포인트컷 표현식의 모든 내용이 정확하게 일치한다. 
따라서 `true` 를 반환한다.

> 가장 많이 생략한 포인트 컷 형태

```java
@DisplayName("가장 많이 생략한 포인트 컷")
@Test
void allMatch() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

> 매칭 조건

- 접근제어자?: 생략 
- 반환타입: * 
- 선언타입?: 생략 
- 메서드이름: * 
- 파라미터: (..)
- 예외?: 없음

`*` 은 아무 값이 들어와도 된다는 뜻이다.

파라미터에서 `..` 은 파라미터의 타입과 파라미터 수가 상관없다는 뜻이다.
`( 0..* )` 파라미터는 뒤에 자세히 정리하겠다.
## excecution - 2

## within
## args

## @target, @within

## @annotation, @args

## bean

## 매개변수 전달

## this, target

## 정리