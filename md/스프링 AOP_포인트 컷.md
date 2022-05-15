- [스프링 AOP - 포인트컷](#스프링-aop---포인트컷)
  - [포인트컷 지시자](#포인트컷-지시자)
  - [예제 만들기](#예제-만들기)
  - [execution - 1](#execution---1)
  - [execution - 2](#execution---2)
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

## execution - 1

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

다음 테스트 코드를 확인해보자.

> **패턴 매칭과 매키지 매칭**

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

    @DisplayName("가장 정확한 포인트 컷")
    @Test
    void exactMatch() {
        // helloMethod=public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
        pointcut.setExpression("execution(public String hello.aop.member.MemberServiceImpl.hello(String))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("가장 많이 생략한 포인트 컷")
    @Test
    void allMatch() {
        pointcut.setExpression("execution(* *(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("이름만 매치하는 포인트컷")
    @Test
    void nameMatch() {
        pointcut.setExpression("execution(* hello(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("패턴 네임 매칭 포인트컷")
    @Test
    void patternMatch() {
        pointcut.setExpression("execution(* hel*(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("패턴 네임 매칭 포인트컷")
    @Test
    void patternMatch2() {
        pointcut.setExpression("execution(* *el*(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("패턴 네임 매칭 포인트컷 실패하는 경우")
    @Test
    void nameMatchFalse() {
        pointcut.setExpression("execution(* nono(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
    }

    @DisplayName("패키지 매칭 정확한 포인트컷")
    @Test
    void packageMatch() {
        pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.hello(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("패키지 매칭에서 생략 가능한 형태")
    @Test
    void packageMatch2() {
        pointcut.setExpression("execution(* hello.aop.member.*.*(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("패키지 매칭이 실패하는 경우")
    @Test
    void packageMatch3() {
        pointcut.setExpression("execution(* hello.aop.*.*(..))"); // member Package인지 지정하지 않았기 때문에 false가 나온다.
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
    }

    @DisplayName("서브 패키지 매칭")
    @Test
    void packageMatchSubPackage1() {
        pointcut.setExpression("execution(* hello.aop.member..*.*(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @DisplayName("서브 패키지 매칭")
    @Test
    void packageMatchSubPackage2() {
        pointcut.setExpression("execution(* hello.aop..*.*(..))");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }
}
```

- `hello.aop.member.*(1).*(2)`
  - (1): 타입
  - (2): 메서드 이름

- 패키지에서 `.`과, `..`의 차이를 이해해야 한다.
  - `.`: 정확하게 해당 위치의 패키지
  - `..`: 해당 위치의 패키지와 그 하위 패키지도 포함

## execution - 2

> **타입 매칭**

```java
@Test
void typeExactMatch() {
    pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test
void typeMatchSuperType() {
    pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))"); // 인터페이스를 넣어도, 매치가 된다. (자식 타입에 있는 것에 대해 부모 타입으로 해도 성공함)
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- `typeExactMatch()` 는 타입 정보가 정확하게 일치하기 때문에 매칭된다.
- `typeMatchSuperType()` 을 주의해서 보아야 한다.
  - `execution`에서는 `MemberService` 처럼 부모 타입을 선언해도, 그 자식 타입은 매칭된다.
  - 다형성에서 `부모타입 = 자식타입`이 할당 가능하다는 점을 떠올려보면 된다.

> 하지만, 주의해야 할 점이 있다.

- **타입 매칭은 부모 타입에 있는 메서드만 허용한다**
  - `typeMatchInternal()` 의 경우 `MemberServiceImpl`를 표현식에 선언했기 때문에 그 안에 있는 internal(String) 

```java
@Test
void typeMatchInternal() throws NoSuchMethodException {
    pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.*(..))");
    final Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isTrue();
}

@Test
void typeMatchNoSuperTypeMethodFalse() throws NoSuchMethodException {
    pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))"); // 이 인터페이스에 매칭하는 걸 다 매칭시킬 것이다.
    final Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class); // 자식 타입에 해당하는 다른 메서드들도 매칭시키게 할 것이냐?
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isFalse(); // 아니다. 부모 타입에 선언한 메서드만 매칭이 된다. (인터페이스에 선언한 것만 매칭이 된다)
}
```

- `typeMatchInternal()` 의 경우 `MemberServiceImpl`를 표현식에 선언했기 때문에 그 안에 있는 `internal(String)` 메서드도 매칭 대상이 된다.
- `typeMatchNoSuperTypeMethodFalse()` 를 주의해서 보아야 한다.
이 경우 표현식에 부모 타입인 `MemberService` 를 선언했다.
- 그런데 자식 타입인 `MemberServiceImpl`의 `internal(String)` 메서드를 매칭하려 한다. 이 경우 매칭에 실패한다. `MemberService` 에는 `internal(String)` 메서드가 없다!
- **부모 타입을 표현식에 선언한 경우 부모 타입에서 선언한 메서드가 자식 타입에 있어야 매칭에 성공**한다. 그래서 부모 타입에 있는 `hello(String)` 메서드는 매칭에 성공하지만, 부모 타입에 없는 `internal(String)` 는 매칭에 실패한다.

> **파라미터 매칭**

```java
@DisplayName("String 타입의 파라미터 허용 (String)")
@Test
void argsMatch() {
    pointcut.setExpression("execution(* *(String))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@DisplayName("파라미터가 없어야함")
@Test
void noArgsMatch() {
    pointcut.setExpression("execution(* *())");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}

@DisplayName("정확히 하나의 파라미터 허용, 모든 타입 허용")
@Test
void argsMatchStar() {
    pointcut.setExpression("execution(* *(*))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@DisplayName("개수와 무관하게 모든 파라미터, 모든 타입 허용")
@Test
void argsMatchAll() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@DisplayName("String으로 시작하고, 이후는 개수와 타입 무관하게 허용, (String), (String, Xxx), (String Xxx, Xxx)")
@Test
void argsMatchComplex() {
    pointcut.setExpression("execution(* *(String, ..))"); // 참고로 ..에는 파라미터가 없어도 됩니다.
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@DisplayName("String으로 시작하고, 이후는 개수는 지정되어 있고 타입은 상관 없음")
@Test
void argsMatchComplex2() {
    pointcut.setExpression("execution(* *(String, *))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}
```

> **execution 파라미터 매칭 규칙은 다음과 같다.**

- `(String)` : 정확하게 String 타입 파라미터
- `()` : 파라미터가 없어야 한다.
- `(*)` : 정확히 하나의 파라미터, 단 모든 타입을 허용한다.
- `(*, *)` : 정확히 두 개의 파라미터, 단 모든 타입을 허용한다.
- `(..)` : 숫자와 무관하게 모든 파라미터, 모든 타입을 허용한다. 참고로 파라미터가 없어도 된다. 0..* 로 이해하면 된다.
- `(String, ..)` : String 타입으로 시작해야 한다. 숫자와 무관하게 모든 파라미터, 모든 타입을 허용한다.
예) (String) , (String, Xxx) , (String, Xxx, Xxx) 허용

## within

- within 지시자는 특정 타입 내의 조인 포인트에 대한 매칭을 제한한다.
- 쉽게 이야기해서, **해당 타입이 매칭되면, 그 안의 메서드(조인 포인트)들이 자동으로 매칭된다.**
- 문법은 단순한데, `execution` 에서 타입 부분만 사용한다고 생각하면 된다.

> WithinTest

```java
public class WithinTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }

    @Test
    void withinExact() {
        pointcut.setExpression("within(hello.aop.member.MemberServiceImpl)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @Test
    void withinStar() {
        pointcut.setExpression("within(hello.aop.member.*Service*)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    @Test
    void withinSubPackage() {
        pointcut.setExpression("within(hello.aop..*)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }
}
```

> *주의*

- 그런데 `within` 사용시 주의해야 할 점이 있다.
- 표현식에 부모 타입을 지정하면 안된다는 점이다.
- 정확하게 타입이 맞아야 한다. 이 부분에서 `execution`과 차이가 난다.

> WithinTest 주의할 점

```java
@Test
@DisplayName("타켓의 타입에만 직접 적용, 인터페이스를 선정하면 안된다.")
void withinSuperTypeFalse() {
    pointcut.setExpression("within(hello.aop.member.MemberService)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}

@Test
@DisplayName("execution은 타입 기반, 인터페이스를 선정 가능.")
void executionSuperTypeTrue() {
    pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- 부모 타입(여기서는 MemberService 인터페이스) 지정시 within 은 실패하고, execution 은 성공하는 것을 확인할 수 있다.
- within은 딱 하나만 지정할 수 있게 되어서, 잘 사용하지 않긴하다.

## args

- `args` : 인자가 주어진 타입의 인스턴스인 조인 포인트로 매칭
- 기본 문법은 `execution` 의 `args` 부분과 같다.
- 메서드들에 있는 파라미터만 보고 매칭하는 것이다.

> *execution과 args의 차이점*

- `execution` 은 파라미터 타입이 정확하게 매칭되어야 한다.
- `execution` 은 클래스에 선언된 정보를 기반으로 판단한다.
- `args` 는 부모 타입을 허용한다.
- `args` 는 실제 넘어온 파라미터 객체 인스턴스를 보고 판단한다.

> ArgsTest

```java
public class ArgsTest {

    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }

    private AspectJExpressionPointcut pointcut(String expression) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);
        return pointcut;
    }

    @Test
    void args() {
        //hello(String)과 매칭
        assertThat(pointcut("args(String)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args(Object)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args()").matches(helloMethod, MemberServiceImpl.class)).isFalse();
        assertThat(pointcut("args(..)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args(*)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args(String,..)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    /**
     * execution(* *(java.io.Serializable)): 메서드의 시그니처로 판단 (정적)
     * args(java.io.Serializable): 런타임에 전달된 인수로 판단 (동적)
     */
    @Test
    void argsVsExecution() {
        //Args
        assertThat(pointcut("args(String)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args(java.io.Serializable)").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("args(Object)").matches(helloMethod, MemberServiceImpl.class)).isTrue();

        //Execution
        assertThat(pointcut("execution(* *(String))").matches(helloMethod, MemberServiceImpl.class)).isTrue();
        assertThat(pointcut("execution(* *(java.io.Serializable))").matches(helloMethod, MemberServiceImpl.class)).isFalse(); //매칭 실패
        assertThat(pointcut("execution(* *(Object))").matches(helloMethod, MemberServiceImpl.class)).isFalse(); //매칭 실패
    }
}
```

- `pointcut()` : `AspectJExpressionPointcut`에 포인트컷은 한번만 지정할 수 있다. 이번 테스트에서는 테스트를 편리하게 진행하기 위해 포인트컷을 여러번 지정하기 위해 포인트컷 자체를 생성하는 메서드를 만들었다.
- 자바가 기본으로 제공하는 `String` 은 `Object` , `java.io.Serializable` 의 하위 타입이다.
- 정적으로 클래스에 선언된 정보만 보고 판단하는 `execution(* *(Object))` 는 매칭에 실패한다.
- 동적으로 실제 파라미터로 넘어온 객체 인스턴스로 판단하는 `args(Object)` 는 매칭에 성공한다. **(부모 타입 허용)**

*참고로, `args` 지시자는, 단독으로 사용되기 보다는 뒤에서 설명할 파라미터 바인딩에서 주로 사용된다.*

## @target, @within

## @annotation, @args

## bean

## 매개변수 전달

## this, target

## 정리
