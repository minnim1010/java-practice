package concurrency.version8;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelStreamTest {

    @Test
    @DisplayName("병렬 스트림을 사용하여 각 숫자의 제곱을 계산한다")
    void testParallelStreamSquares() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> squares = numbers.parallelStream()
                                       .map(n -> n * n)
                                       .collect(Collectors.toList());

        // 각 요소가 제곱되었는지 확인
        List<Integer> expectedSquares = Arrays.asList(1, 4, 9, 16, 25, 36, 49, 64, 81, 100);
        assertEquals(expectedSquares, squares);
    }

    // 복잡한 연산을 수행하는 메서드
    long complexComputation(Integer n) {
        int largePrime = 999_999_937;  // 큰 소수
        long result = 0;
        for (int i = 0; i < 1_000_000; i++) {
            result += n % largePrime;  // 복잡한 나눗셈 작업
        }
        return result;
    }

    @Test
    @DisplayName("병렬 스트림의 처리 시간이 순차 스트림보다 더 짧아야 한다")
    void testParallelStreamPerformance() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

        // 순차 스트림의 실행 시간 측정
        long sequentialStart = System.nanoTime();
        numbers.stream()
                .map(n -> complexComputation(n)) // 복잡한 연산 적용
                .collect(Collectors.toList());
        long sequentialEnd = System.nanoTime();
        long sequentialDuration = sequentialEnd - sequentialStart;

        // 병렬 스트림의 실행 시간 측정
        long parallelStart = System.nanoTime();
        numbers.parallelStream()
                .map(n -> complexComputation(n)) // 동일한 복잡한 연산 적용
                .collect(Collectors.toList());
        long parallelEnd = System.nanoTime();
        long parallelDuration = parallelEnd - parallelStart;

        // 실행 시간 출력
        System.out.println("순차 스트림 실행 시간: " + sequentialDuration);
        System.out.println("병렬 스트림 실행 시간: " + parallelDuration);

        // 병렬 스트림의 시간이 더 짧은지 확인
        assertTrue(parallelDuration < sequentialDuration, "병렬 스트림의 실행 시간이 더 짧아야 합니다.");
    }

    @Test
    @DisplayName("병렬 스트림에서 데이터 일관성 문제가 발생하는지 테스트한다 (대량 연산)")
    void testParallelStreamDataConsistency() {
        // 1부터 100,000까지의 숫자 리스트 생성
        List<Integer> numbers = IntStream.rangeClosed(1, 100_00).boxed().toList();

        // 상태가 공유된 int[] 배열을 사용 (스레드 안전하지 않음)
        int[] sum = {0};

        // 병렬 스트림을 사용하여 sum 계산 (비동기적으로 상태를 변경)
        numbers.parallelStream().forEach(n -> sum[0] += n);

        // 병렬 스트림으로 인해 잘못된 결과가 나올 가능성 확인
        int expectedSum = numbers.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Expected Sum: " + expectedSum);
        System.out.println("Actual Sum (with int[]): " + sum[0]);

        // 병렬 스트림이 상태 공유 문제를 일으켜 잘못된 결과가 나오는지 확인
        assertNotEquals(sum[0], expectedSum, "병렬 스트림에서 상태 공유로 인해 문제가 발생해야 합니다.");
    }
}