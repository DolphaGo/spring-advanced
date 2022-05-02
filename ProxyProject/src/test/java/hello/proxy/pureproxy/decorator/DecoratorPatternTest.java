package hello.proxy.pureproxy.decorator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hello.proxy.pureproxy.decorator.code.Component;
import hello.proxy.pureproxy.decorator.code.DecoratorPatternClient;
import hello.proxy.pureproxy.decorator.code.MessageDecorator;
import hello.proxy.pureproxy.decorator.code.RealComponent;
import hello.proxy.pureproxy.decorator.code.TimeDecorator;
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

    @DisplayName("데코레이터 패턴 적용 1 - MessageDecorator 적용")
    @Test
    void decorator1() {
        final RealComponent realComponent = new RealComponent();
        final MessageDecorator messageDecorator = new MessageDecorator(realComponent);
        final DecoratorPatternClient client = new DecoratorPatternClient(messageDecorator);
        client.execute();
    }

    @DisplayName("데코레이터 패턴 적용 2 - TimeDecorator 추가 적용")
    @Test
    void decorator2() {
        final RealComponent realComponent = new RealComponent();
        final MessageDecorator messageDecorator = new MessageDecorator(realComponent);
        final TimeDecorator timeDecorator = new TimeDecorator(messageDecorator);
        final DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator);
        client.execute();
    }
}
