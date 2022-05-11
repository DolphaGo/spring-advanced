package hello.aop.member.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 참고로 Compile 같은 것을 하면, 실행시점엔 사라지게 됨
public @interface MethodAop {
    String value();
}
