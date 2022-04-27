package hello.proxy.app.v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

//@Controller 쓰면 되지 않나요? : 내부에 @Controller가 있기 때문에 자동으로 컴포넌트 스캔의 대상이 됩니다. 그래서 쓰지 않아요.
@Slf4j
@RequestMapping // 컴포넌트 스캔의 대상이 되지 않습니다. 그래서 괜찮습니다.
@ResponseBody
public class OrderControllerV2 {

    private final OrderServiceV2 orderService;

    public OrderControllerV2(final OrderServiceV2 orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/v2/request")
    public String request(final String itemId) {
        orderService.orderItem(itemId);
        return "ok";
    }

    @GetMapping("/v2/no-log")
    public String noLog() {
        return "ok";
    }
}
