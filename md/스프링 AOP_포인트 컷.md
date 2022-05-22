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

**정의**

- `@target` : 실행 객체의 클래스에 주어진 타입의 애노테이션이 있는 조인 포인트
- `@within` : 주어진 애노테이션이 있는 타입 내 조인 포인트

**설명**

- `@target` , `@within` 은 다음과 같이 타입에 있는 애노테이션으로 AOP 적용 여부를 판단한다. 
- `@target(hello.aop.member.annotation.ClassAop)`
- `@within(hello.aop.member.annotation.ClassAop)`

```java
@ClassAop
class Target{}
```

**`@target` vs `@within`**

- `@target` 은 인스턴스의 **모든 메서드**를 조인 포인트로 적용한다.
- `@within` 은 **해당 타입 내에 있는 메서드만** 조인 포인트로 적용한다.
- **쉽게 이야기해서 `@target` 은 부모 클래스의 메서드까지 어드바이스를 다 적용하고, `@within` 은 자기 자신의 클래스에 정의된 메서드에만 어드바이스를 적용한다.**

![](/images/2022-05-16-02-29-21.png)

> `@target`, `@within` Test

```java
@Slf4j
@Import({ AtTargetAtWithinTest.Config.class })
@SpringBootTest
public class AtTargetAtWithinTest {
    @Autowired
    Child child;

    @Test
    void success() {
        log.info("child Proxy={}", child.getClass());
        child.childMethod(); //부모, 자식 모두 있는 메서드
        child.parentMethod(); //부모 클래스만 있는 메서드
    }

    static class Config {
        @Bean
        public Child child() {
            return new Child();
        }

        @Bean
        public AtTargetAtWithinAspect atTargetAtWithinAspect() {
            return new AtTargetAtWithinAspect();
        }
    }

    static class Parent {
        public void parentMethod() {} //부모에만 있는 메서드 }
    }

    @ClassAop
    static class Child extends Parent {
        public void childMethod() {}
    }

    @Slf4j
    @Aspect
    static class AtTargetAtWithinAspect {
        //@target: 인스턴스 기준으로 모든 메서드의 조인 포인트를 선정, 부모 타입의 메서드도 적용
        @Around("execution(* hello.aop..*(..)) && @target(hello.aop.member.annotation.ClassAop)")
        public Object atTarget(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[@target] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        //@within: 선택된 클래스 내부에 있는 메서드만 조인 포인트로 선정, 부모 타입의 메서드는 적용되지 않음
        @Around("execution(* hello.aop..*(..)) && @within(hello.aop.member.annotation.ClassAop)")
        public Object atWithin(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[@within] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
}
```

- 실행 결과

```log
2022-05-16 02:35:39.287  INFO 13495 --- [    Test worker] hello.aop.pointcut.AtTargetAtWithinTest  : child Proxy=class hello.aop.pointcut.AtTargetAtWithinTest$Child$$EnhancerBySpringCGLIB$$265bc3fe
2022-05-16 02:35:39.290  INFO 13495 --- [    Test worker] argetAtWithinTest$AtTargetAtWithinAspect : [@target] void hello.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
2022-05-16 02:35:39.291  INFO 13495 --- [    Test worker] argetAtWithinTest$AtTargetAtWithinAspect : [@within] void hello.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
2022-05-16 02:35:39.294  INFO 13495 --- [    Test worker] argetAtWithinTest$AtTargetAtWithinAspect : [@target] void hello.aop.pointcut.AtTargetAtWithinTest$Parent.parentMethod()
```

- `parentMethod()` 는 `Parent` 클래스에만 정의되어 있고, `Child` 클래스에 정의되어 있지 않기 때문에 `@within` 에서 AOP 적용 대상이 되지 않는다.
- 실행 결과를 보면 `child.parentMethod()` 를 호출 했을 때 `[@within]` 이 호출되지 않은 것을 확인할 수 있다.

> 참고

`@target` , `@within` 지시자는 뒤에서 설명할 파라미터 바인딩에서 함께 사용된다.

> 주의

- 다음 포인트컷 지시자는 단독으로 사용하면 안된다.
  - `args`, `@args`, `@target`
- 이번 예제를 보면 `execution(* hello.aop..*(..))`를 통해 적용 대상을 줄여준 것을 확인할 수 있다.
- `args` , `@args` , `@target` 은 **실제 객체 인스턴스가 생성되고 실행될 때 어드바이스 적용 여부를 확인할 수 있다.**
- 실행 시점에 일어나는 포인트컷 적용 여부도 결국 프록시가 있어야 실행 시점에 판단할 수 있다. **프록시가 없다면 판단 자체가 불가능**하다. 
- **그런데 스프링 컨테이너가 프록시를 생성하는 시점은 스프링 컨테이너가 만들어지는 애플리케이션 로딩 시점에 적용할 수 있다.** 
- 따라서 `args` , `@args` , `@target` 같은 포인트컷 지시자가 있으면 스프링은 **모든 스프링 빈에 AOP를 적용하려고 시도**한다. 
- **앞서 설명한 것 처럼 프록시가 없으면 실행 시점에 판단 자체가 불가능하기 때문이다.**

***문제는 이렇게 모든 스프링 빈에 AOP 프록시를 적용하려고 하면 스프링이 내부에서 사용하는 빈 중에는 final 로 지정된 빈들도 있기 때문에 오류가 발생할 수 있다.***

- 따라서 이러한 표현식은 최대한 프록시 적용 대상을 축소하는 표현식과 함께 사용해야 한다.

내가 만약 다음과 같이 execution을 빼고나서 다시 테스트를 돌리면 어떻게 될까?

```java
@Slf4j
@Aspect
static class AtTargetAtWithinAspect {
    //@target: 인스턴스 기준으로 모든 메서드의 조인 포인트를 선정, 부모 타입의 메서드도 적용
    @Around("@target(hello.aop.member.annotation.ClassAop)")
    public Object atTarget(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[@target] {}", joinPoint.getSignature());
        return joinPoint.proceed();
    }

    //@within: 선택된 클래스 내부에 있는 메서드만 조인 포인트로 선정, 부모 타입의 메서드는 적용되지 않음
    @Around("@within(hello.aop.member.annotation.ClassAop)")
    public Object atWithin(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[@within] {}", joinPoint.getSignature());
        return joinPoint.proceed();
    }
}
```

- 다음과 같은 에러가 발생한다.

![](/images/2022-05-16-02-41-42.png)

![](/images/2022-05-16-02-42-01.png)

## @annotation, @args

**정의**

`@annotation` : 메서드가 주어진 애노테이션을 가지고 있는 조인 포인트를 매칭

- 다음과 같이 메서드(조인 포인트)에 애노테이션이 있으면 매칭한다.

```java
public class MemberServiceImpl {
      @MethodAop("test value")
      public String hello(String param) {
          return "ok";
      }
}
```

> 테스트

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 참고로 Compile 같은 것을 하면, 실행시점엔 사라지게 됨
public @interface MethodAop {
    String value();
}
```

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

```java
@Import(AtAnnotationTest.AtAnnotationAspect.class)
@Slf4j
@SpringBootTest
public class AtAnnotationTest {
    @Autowired
    MemberService memberService;

    @Test
    void success() {
        log.info("memberService Proxy={}", memberService.getClass());
        memberService.hello("helloA");
    }

    @Slf4j
    @Aspect
    static class AtAnnotationAspect {
        @Around("@annotation(hello.aop.member.annotation.MethodAop)")
        public Object ObjectDoAtAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[@annotation] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
}
```

- 실행 결과

```log
2022-05-16 02:46:42.066  INFO 16106 --- [    Test worker] hello.aop.pointcut.AtAnnotationTest      : memberService Proxy=class hello.aop.member.MemberServiceImpl$$EnhancerBySpringCGLIB$$145adb44
2022-05-16 02:46:42.071  INFO 16106 --- [    Test worker] .a.p.AtAnnotationTest$AtAnnotationAspect : [@annotation] String hello.aop.member.MemberServiceImpl.hello(String)
```

**`@args`**

- 전달된 실제 인수의 런타임 타입이 주어진 타입의 애노테이션을 갖는 조인 포인트 설명
- 전달된 인수의 런타임 타입에 `@Check` 애노테이션이 있는 경우에 매칭한다.
- `@args(test.Check)`
- 잘 안씀.

## bean

- 스프링 전용 포인트컷 지시자, 빈의 이름으로 지정한다.
- 스프링 빈의 이름으로 AOP 적용 여부를 지정한다. 이것은 스프리에서만 사용할 수 있는 특별한 지시자이다.(AspectJ 문법은 아님)
- `bean(orderService) || bean(*Repository)`
- `*` 와 같은 패턴을 사용할 수 있다.

```java
@Import(BeanTest.BeanAspect.class)
@Slf4j
@SpringBootTest
public class BeanTest {

    @Autowired
    OrderService orderService;

    @Test
    void success() {
        orderService.orderItem("itemA");
    }

    @Aspect
    static class BeanAspect {

        @Around("bean(orderService) || bean(*Repository)")
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[bean] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
}
```

- 실행결과

```log
2022-05-22 23:46:19.674  INFO 84559 --- [    Test worker] hello.aop.pointcut.BeanTest              : [bean] String hello.aop.order.OrderService.orderItem(String)
2022-05-22 23:46:19.685  INFO 84559 --- [    Test worker] hello.aop.order.OrderService             : [orderService] 실행
2022-05-22 23:46:19.685  INFO 84559 --- [    Test worker] hello.aop.pointcut.BeanTest              : [bean] String hello.aop.order.OrderRepository.save(String)
2022-05-22 23:46:19.690  INFO 84559 --- [    Test worker] hello.aop.order.OrderRepository          : [orderRepository] 실행
```

- `OrderService`, `*Repository(OrderRepository)` 의 메서드에 AOP가 적용된다.

## 매개변수 전달

- 다음은 포인트컷 표현식을 사용해서 어드바이스에 매개변수를 전달할 수 있다.

**`this, target, args, @target, @within, @annotation, @args`**

다음과 같이 사용한다.

```java
@Before("allMember() && args(arg, ..)")
public void logArgs3(String arg){
    log.info("[logArgs3] arg={}", arg);
}
```

- 포인트컷의 이름과 매개변수의 이름을 맞추어야 한다. 여기서는 `arg`로 맞추었다.
- 추가로, 타입이 메서드에 지정한 타입으로 제한된다.
- 여기서는 메서드 타입이 `String`으로 되어있기 때문에 다음과 같이 정의되는 것으로 이해하면 된다.
  - `args(arg, ...) -> args(String, ..)`

```java
@Import(ParameterTest.ParameterAspect.class)
@Slf4j
@SpringBootTest
public class ParameterTest {

    @Autowired
    MemberService memberService;

    @Test
    void success() {
        log.info("memberService Proxy = {}", memberService.getClass());
        memberService.hello("helloA");
    }

    @Slf4j
    @Aspect
    static class ParameterAspect {

        @Pointcut("execution(* hello.aop.member..*.*(..))")
        private void allMember() {}

        // "가독성이 그렇게 좋지는 않다."
        @Around("allMember()")
        public Object logArgs1(ProceedingJoinPoint joinPoint) throws Throwable {
            final Object arg1 = joinPoint.getArgs()[0];
            log.info("[logArgs1] {}, arg={}", joinPoint.getSignature(), arg1);
            return joinPoint.proceed();
        }

        @Around("allMember() && args(arg, ..)")
        public Object logArgs2(ProceedingJoinPoint joinPoint, Object arg) throws Throwable {
            log.info("[logArgs2] {}, arg={}", joinPoint.getSignature(), arg);
            return joinPoint.proceed();
        }

        @Before("allMember() && args(arg, ..)")
        public void logArgs3(String arg) {
            log.info("[logArgs3] arg={}", arg);
        }

        // this : 스프링 컨테이너 안에 있는 것
        @Before("allMember() && this(obj)")
        public void thisArgs(JoinPoint joinPoint, MemberService obj) {
            log.info("[this] {}, obj={}", joinPoint.getSignature(), obj.getClass());
        }

        // target은 호출하는 실제 대상(프록시를 호출하는 실제 대상을 원할 때는 target)
        @Before("allMember() && target(obj)")
        public void targetArgs(JoinPoint joinPoint, MemberService obj) {
            log.info("[target] {}, obj={}", joinPoint.getSignature(), obj.getClass()); // 프록시 객체
        }

        @Before("allMember() && @target(annotation)")
        public void atTarget(JoinPoint joinPoint, ClassAop annotation) {
            log.info("[@target] {}, obj={}", joinPoint.getSignature(), annotation);
        }

        @Before("allMember() && @within(annotation)")
        public void atWithin(JoinPoint joinPoint, ClassAop annotation) {
            log.info("[@within] {}, obj={}", joinPoint.getSignature(), annotation);
        }

        @Before("allMember() && @annotation(annotation)")
        public void atAnnotation(JoinPoint joinPoint, MethodAop annotation) {
            log.info("[@annotation] {}, annotationValue={}", joinPoint.getSignature(), annotation.value());
        }

    }
}
```

- 실행 결과

```log
2022-05-23 00:30:51.249  INFO 93098 --- [    Test worker] hello.aop.pointcut.ParameterTest         : memberService Proxy = class hello.aop.member.MemberServiceImpl$$EnhancerBySpringCGLIB$$92941749
2022-05-23 00:30:51.254  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [logArgs1] String hello.aop.member.MemberServiceImpl.hello(String), arg=helloA
2022-05-23 00:30:51.255  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [logArgs2] String hello.aop.member.MemberServiceImpl.hello(String), arg=helloA
2022-05-23 00:30:51.256  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [logArgs3] arg=helloA
2022-05-23 00:30:51.256  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [this] String hello.aop.member.MemberServiceImpl.hello(String), obj=class hello.aop.member.MemberServiceImpl$$EnhancerBySpringCGLIB$$92941749
2022-05-23 00:30:51.256  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [target] String hello.aop.member.MemberServiceImpl.hello(String), obj=class hello.aop.member.MemberServiceImpl
2022-05-23 00:30:51.255  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [@target] String hello.aop.member.MemberServiceImpl.hello(String), obj=@hello.aop.member.annotation.ClassAop()
2022-05-23 00:30:51.256  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [@within] String hello.aop.member.MemberServiceImpl.hello(String), obj=@hello.aop.member.annotation.ClassAop()
2022-05-23 00:30:51.255  INFO 93098 --- [    Test worker] h.a.p.ParameterTest$ParameterAspect      : [@annotation] String hello.aop.member.MemberServiceImpl.hello(String), annotationValue=test value
```

- `logArgs1` : `joinPoint.getArgs()[0]` 과 같이 매개변수를 전달 받는다.
- `logArgs2` : `args(arg, ..)` 와 같이 매개변수를 전달 받는다.
- `logArgs3` : `@Before` 를 사용한 축약버전이다. 추가로 타입을 `String`으로 제한했다.
- `this`: **프록시 객체** 를 전달 받는다.
- `target`: **실제 대상 객체**를 전달 받는다.
- `@target, @within` : 타입의 애노테이션을 전달 받는다.
- `@annotation`: 메서드의 애노테이션을 전달 받는다.
  - 여기서는 `annotation.value()`로 해당 애노테이션 값을 출력하는 모습을 확인할 수 있다.

위의 실행 결과에 대해 이해를 돕고자, 기존에 적용되어 있던 애노테이션 클래스는 다음과 같다.

> MemberService의 구체 클래스인 MemberServiceImpl은 다음과 같다.

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

그리고 어노테이션 정보는 다음과 같다.

> ClassAop

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) // 실행할 때까지 어노테이션이 살아있는 것
public @interface ClassAop {
}
```

> MethodAop

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 참고로 Compile 같은 것을 하면, 실행시점엔 사라지게 됨
public @interface MethodAop {
    String value();
}
```


## this, target

- `this`: 스프링 빈 객체(스프링 AOP 프록시)를 대상으로 하는 조인 포인트
  - 프록시 자체가 스프링 빈으로 올라갔을 때
- `target`: Target 객체(스프링 AOP 프록시가 가르키는 실제 대상)를 대상으로 하는 조인 포인트
  - 스프링 빈에 등록되는 것은 아니지만, 객체의 실체(프록시가 가르키는 대상)

> this, target 은 다음과 같이 적용 타입 하나를 정확하게 지정해야 한다.

```java
this(hello.aop.member.MemberService)
target(hello.aop.member.MemberService)
```

- `*`와 같은 패턴을 사용할 수 **없다.**
- 부모 타입을 허용한다.

> **this vs target**

- 단순히 타입 하나를 정하면 되는데, this와 target은 어떤 차이가 있을까.

스프링에서 AOP를 적용하면, 실제 target 객체 대신에 프록시 객체가 스프링 빈으로 등록된다.

- this 는 스프링 빈으로 등록되어 있는 프록시 객체를 대상으로 포인트컷을 매칭한다.
- target 은 실제 target 객체를 대상으로 포인트컷을 매칭한다.

> **프록시 생성 방식에 따른 차이**

- 스프링은 프록시를 생성할 때, JDK 동적 프록시와 CGLIB를 선택할 수 있따.
- 둘의 프록시를 생성하는 방식이 다르기 때문에 차이가 발생한다.
  - JDK 동적 프록시: 인터페이스가 필수이고, 인터페이스를 구현한 프록시 객체를 생성한다.
  - CGLIB: 인터페이스가 있어도 구체 클래스를 상속받아서 프록시 객체를 생성한다.

> **JDK 동적 프록시**

![](/images/2022-05-23-00-44-09.png)

먼저 JDK 동적 프록시를 적용할 때 this, target을 보자.

**MemberService 인터페이스 지정**

- `this(hello.aop.member.MemberService)`
  - 프록시 객체를 보고 판단한다.
  - `this` 는 부모타입을 허용하기 때문에 AOP가 적용된다.
- `target(hello.aop.member.MemberService)`
  - `target` 객체를 보고 판단한다.
  - `target` 은 부모 타입을 허용하기 때문에 AOP가 적용된다.

**MemberServiceImpl 구체 클래스 지정**

- `this(hello.aop.member.MemberServiceImpl)`
  - 프록시 객체를 보고 판단한다.
  - JDK 동적 프록시로 만들어진 proxy 객체는 `MemberService` 인터페이스 기반으로 구현된 새로운 클래스이다.
  - 따라서 `MemberServiceImpl` 를 전혀 알지 못하므로, **AOP 적용 대상이 안된다.**

- `target(hello.aop.member.MemberServiceImpl)`
  - target 객체를 보고 판단한다.
  - target 객체가 `MemberServiceImpl` 타입이므로 AOP 적용 대상이다.

> 테스트

```java
@Import(ThisTargetTest.ThisTargetAspect.class)
@Slf4j
@SpringBootTest(properties = "spring.aop.proxy-target-class=false") // JDK 동적 프록시
public class ThisTargetTest {

    @Autowired
    MemberService memberService;

    @Test
    void success() {
        log.info("memberService Proxy={}", memberService.getClass());
        memberService.hello("helloA");
    }

    @Slf4j
    @Aspect
    static class ThisTargetAspect { // this와 target은 부모타입 허용한다.

        @Around("this(hello.aop.member.MemberService)")
        public Object doThisInterface(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[this-interface] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("target(hello.aop.member.MemberService)")
        public Object doTargetInterface(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[target-interface] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("this(hello.aop.member.MemberServiceImpl)")
        public Object doThisConcrete(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[this-concrete] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("target(hello.aop.member.MemberServiceImpl)")
        public Object doTargetConcrete(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[target-concrete] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
}
```

```log
2022-05-23 01:00:18.942  INFO 99153 --- [    Test worker] hello.aop.pointcut.ThisTargetTest        : memberService Proxy=class com.sun.proxy.$Proxy52
2022-05-23 01:00:18.954  INFO 99153 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [target-concrete] String hello.aop.member.MemberService.hello(String)
2022-05-23 01:00:18.954  INFO 99153 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [target-interface] String hello.aop.member.MemberService.hello(String)
2022-05-23 01:00:18.954  INFO 99153 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [this-interface] String hello.aop.member.MemberService.hello(String)
```

- **결과를 보면, JDK 동적 프록시로 생성이 되었고, this의 구체클래스로는 aop가 먹히지 않는 것을 확인할 수 있다.**

> **CGLIB**

![](/images/2022-05-23-00-43-58.png)

**MemberService 인터페이스 지정**

- `this(hello.aop.member.MemberService)` 
  - proxy 객체를 보고 판단한다. 
  - this 는 부모 타입을 허용하기 때문에 AOP가 적용된다.

- `target(hello.aop.member.MemberService)` 
  - target 객체를 보고 판단한다. 
  - target 은 부모 타입을 허용하기 때문에 AOP가 적용된다.

**MemberServiceImpl 구체 클래스 지정**

- `this(hello.aop.member.MemberServiceImpl)`
  - proxy 객체를 보고 판단한다.
  - CGLIB로 만들어진 proxy 객체는 `MemberServiceImpl` 를 상속 받아서 만들었기 때문에 AOP가 적용된다.
  - this 가 부모 타입을 허용하기 때문에 포인트컷의 대상이 된다.

- `target(hello.aop.member.MemberServiceImpl)`
  - target 객체를 보고 판단한다.
  - target 객체가 `MemberServiceImpl` 타입이므로 AOP 적용 대상이다.

> 테스트

```java
@Import(ThisTargetTest.ThisTargetAspect.class)
@Slf4j
@SpringBootTest(properties = "spring.aop.proxy-target-class=true") // CGLIB 프록시, default가 CGLIB
public class ThisTargetTest {

    @Autowired
    MemberService memberService;

    @Test
    void success() {
        log.info("memberService Proxy={}", memberService.getClass());
        memberService.hello("helloA");
    }

    @Slf4j
    @Aspect
    static class ThisTargetAspect { // this와 target은 부모타입 허용한다.

        @Around("this(hello.aop.member.MemberService)")
        public Object doThisInterface(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[this-interface] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("target(hello.aop.member.MemberService)")
        public Object doTargetInterface(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[target-interface] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("this(hello.aop.member.MemberServiceImpl)")
        public Object doThisConcrete(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[this-concrete] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        @Around("target(hello.aop.member.MemberServiceImpl)")
        public Object doTargetConcrete(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[target-concrete] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
}
```

- 실행 결과

```log
05-23 02:24:39.880  INFO 14661 --- [    Test worker] hello.aop.pointcut.ThisTargetTest        : memberService Proxy=class hello.aop.member.MemberServiceImpl$$EnhancerBySpringCGLIB$$b16ba34e
2022-05-23 02:24:39.884  INFO 14661 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [target-concrete] String hello.aop.member.MemberServiceImpl.hello(String)
2022-05-23 02:24:39.884  INFO 14661 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [target-interface] String hello.aop.member.MemberServiceImpl.hello(String)
2022-05-23 02:24:39.884  INFO 14661 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [this-concrete] String hello.aop.member.MemberServiceImpl.hello(String)
2022-05-23 02:24:39.884  INFO 14661 --- [    Test worker] h.a.p.ThisTargetTest$ThisTargetAspect    : [this-interface] String hello.aop.member.MemberServiceImpl.hello(String)
```

- 결과를 보면, CGLIB 프록시로 생성이 되고, JDK 동적프록시로 동작하지 않았던, this의 구체 클래스에도 AOP 적용이 된 것을 확인할 수 있다.

**`properties = {"spring.aop.proxy-target-class=false"}`**

- `application.properties` 에 설정하는 대신에 해당 테스트에서만 설정을 임시로 적용한다. 이렇게 하면 각 테스트마다 다른 설정을 손쉽게 적용할 수 있다.
- `spring.aop.proxy-target-class=false`
  - 스프링이 AOP 프록시를 생성할 때 JDK 동적 프록시를 우선 생성한다.
  - 물론 인터페이스가 없다면 CGLIB를 사용한다.

- `spring.aop.proxy-target-class=true`
  - 스프링이 AOP 프록시를 생성할 때 CGLIB 프록시를 생성한다.
  - 참고로 이 설정을 생략하면 스프링 부트에서 기본으로 CGLIB를 사용한다.


**프록시를 대상으로 하는 this 의 경우 구체 클래스를 지정하면 프록시 생성 전략에 따라서 다른 결과가 나올 수 있다는 점을 알아두자.**

> *참고로, this , target 지시자는 단독으로 사용되기 보다는 파라미터 바인딩에서 주로 사용된다,*

## 정리
