package hello.aop.exam;

import org.springframework.stereotype.Repository;

import hello.aop.exam.annotation.Retry;
import hello.aop.exam.annotation.Trace;

@Repository
public class ExamRepository {

    private static int seq = 0;

    /**
     * 5번에 1번 실패하는 요청
     */
    @Trace
    @Retry(4) // 횟수 제한은 반드시 있어야 한다.. 셀프 디도스를 만들수도 있음.
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new IllegalStateException("예외 발생");
        }
        return "ok";
    }
}
