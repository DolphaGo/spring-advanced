package hello.proxy.pureproxy.decorator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hello.proxy.pureproxy.decorator.code.Component;
import hello.proxy.pureproxy.decorator.code.DecoratorPatternClient;
import hello.proxy.pureproxy.decorator.code.RealComponent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecoratorPatternTest {

    @DisplayName("데코레이터 패턴 적용 전")
    @Test
    void noDecorator() {
        Component realComponent = new RealComponent();
        DecoratorPatternClient client = new DecoratorPatternClient(realComponent);
        client.execute();
    }
}
