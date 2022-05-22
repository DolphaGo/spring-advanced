# 스프링 AOP - 실전 예제

## 예제 만들기

지금까지 학습한 내용을 활용해서 유용한 스프링 AOP를 만들어보자.

- `@Trace` 애노테이션으로 로그 출력하기
- `@Retry` 애노테이션으로 예외 발생시 재시도 하기

> Repository

```java
@Repository
public class ExamRepository {

    private static int seq = 0;

    /**
     * 5번에 1번 실패하는 요청
     */
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new IllegalStateException("예외 발생");
        }
        return "ok";
    }
}
```

> Service

```java
@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;

    public void request(String itemId) {
        examRepository.save(itemId);
    }
}
```

> Test

```java
@Slf4j
@SpringBootTest
public class ExamTest {

    @Autowired
    ExamService examService;

    @Test
    void test() {
        for (int i = 0; i < 5; i++) {
            log.info("client request i = {}", i);
            examService.request("data" + i);
        }
    }
}
```

## 로그 출력 AOP

## 재시도 AOP

## 정리