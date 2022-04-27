package hello.proxy.app.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping // 스프링은 @Controller 또는 @RequestMapping 이 있어야 스프링 컨트롤러로 인식한다.
@ResponseBody
public interface OrderControllerV1 {

    // Java 버전에 따라서 컨트롤러 클래스 단에서는 없어도 되는 어노테이션인 @RequestParam이 인터페이스 내부에는 @RequestParam 같은 것을 인식 못할 때가 있다.
    // 그래서 꼭 명시해주자.
    @GetMapping("/v1/request")
    String request(@RequestParam("itemId") String itemId);

    @GetMapping("/v1/no-log")
    String noLog();
}
