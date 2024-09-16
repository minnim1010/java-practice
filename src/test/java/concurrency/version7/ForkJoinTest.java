package concurrency.version7;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ForkJoinTest {

    // RecursiveTask를 사용한 병렬 합계 계산 테스트
    @Test
    @DisplayName("ForkJoinPool을 사용한 병렬 합계 계산")
    public void testParallelSum() {
        int[] array = new int[100];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }

        ForkJoinPool pool = new ForkJoinPool();
        SumTask task = new SumTask(array, 0, array.length);
        int result = pool.invoke(task);

        assertEquals(5050, result, "배열의 합계가 정확해야 합니다.");
    }

    // RecursiveAction을 사용한 병렬 배열 처리 테스트
    @Test
    @DisplayName("ForkJoinPool을 사용한 병렬 배열 처리")
    public void testParallelArrayAction() throws InterruptedException {
        int[] array = new int[100];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }

        ForkJoinPool pool = new ForkJoinPool();
        MultiplyTask task = new MultiplyTask(array, 0, array.length, 2);
        pool.invoke(task);

        // 모든 요소가 2배로 곱해졌는지 확인
        for (int i = 0; i < array.length; i++) {
            assertEquals((i + 1) * 2, array[i], "배열의 각 요소가 2배로 곱해져야 합니다.");
        }
    }

    // 워크 스틸링을 사용한 병렬 작업 테스트
    @Test
    @DisplayName("ForkJoinPool에서 워크 스틸링 테스트")
    public void testWorkStealing() throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(4);

        int[] array = new int[100];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }

        // 4개의 스레드를 사용하여 작업 분할 처리
        SumTask task = new SumTask(array, 0, array.length);
        pool.submit(task);

        // 대기 및 완료 확인
        pool.awaitTermination(1, TimeUnit.SECONDS);
        assertEquals(5050, task.join(), "작업이 성공적으로 완료되고 정확한 결과를 반환해야 합니다.");
    }

    // RecursiveTask에서 오류 발생 테스트
    @Test
    @DisplayName("ForkJoinTask에서 오류 발생 시 처리 테스트")
    public void testExceptionHandling() {
        ForkJoinPool pool = new ForkJoinPool();
        RecursiveTask<Integer> faultyTask = new RecursiveTask<Integer>() {
            @Override
            protected Integer compute() {
                throw new RuntimeException("의도적인 오류 발생");
            }
        };

        assertThrows(RuntimeException.class, () -> pool.invoke(faultyTask), "예외가 발생해야 합니다.");
    }

    // RecursiveTask 구현: 배열의 합계를 계산하는 작업
    static class SumTask extends RecursiveTask<Integer> {
        private final int[] array;
        private final int start, end;
        private static final int THRESHOLD = 10;

        public SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= THRESHOLD) {
                int sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                SumTask leftTask = new SumTask(array, start, mid);
                SumTask rightTask = new SumTask(array, mid, end);
                invokeAll(leftTask, rightTask);
                return leftTask.join() + rightTask.join();
            }
        }
    }

    // RecursiveAction 구현: 배열의 요소를 특정 값으로 곱하는 작업
    static class MultiplyTask extends RecursiveAction {
        private final int[] array;
        private final int start, end;
        private final int factor;
        private static final int THRESHOLD = 10;

        public MultiplyTask(int[] array, int start, int end, int factor) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.factor = factor;
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    array[i] *= factor;
                }
            } else {
                int mid = (start + end) / 2;
                MultiplyTask leftTask = new MultiplyTask(array, start, mid, factor);
                MultiplyTask rightTask = new MultiplyTask(array, mid, end, factor);
                invokeAll(leftTask, rightTask);
            }
        }
    }
}
