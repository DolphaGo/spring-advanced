package hello.proxy.jdkdynamic;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionTest {

    @Test
    void reflection0() {
        final Hello target = new Hello();

        // 공통 로직1 시작
        log.info("start");
        final String result1 = target.callA(); // 호출하는 메서드가 다름
        log.info("result={}", result1);
        // 공통 로직1 종료

        // 공통 로직2 시작
        log.info("start");
        final String result2 = target.callB(); // 호출하는 메서드가 다름
        log.info("result={}", result2);
        // 공통 로직2 종료
    }

    @Test
    void reflection1() throws Exception {
        // 클래스 정보
        final Class<?> classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

        final Hello target = new Hello();

        //callA의 메서드 정보
        log.info("start");
        final Method methodCallA = classHello.getMethod("callA");
        final Object result1 = methodCallA.invoke(target); // target 인스턴스에 있는 callA를 호출하겠다는 의미
        log.info("result1={}", result1);

        //callB의 메서드 정보
        log.info("start");
        final Method methodCallB = classHello.getMethod("callB");
        final Object result2 = methodCallB.invoke(target); // target 인스턴스에 있는 callB를 호출하겠다는 의미
        log.info("result2={}", result2);
    }

    @Test
    void reflection2() throws Exception {
        // 클래스 정보
        final Class<?> classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

        final Hello target = new Hello();

        //callA의 메서드 정보
        final Method methodCallA = classHello.getMethod("callA");
        dynamicCall(methodCallA, target);

        //callB의 메서드 정보
        final Method methodCallB = classHello.getMethod("callB");
        dynamicCall(methodCallB, target);
    }

    private void dynamicCall(Method method, Object target) throws Exception{
        log.info("start");
        final Object result = method.invoke(target);
        log.info("result1={}", result);
    }

    static class Hello {
        public String callA() {
            log.info("callA");
            return "A";
        }

        public String callB() {
            log.info("callB");
            return "B";
        }
    }
}
