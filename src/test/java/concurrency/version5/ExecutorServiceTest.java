package concurrency.version5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceTest {

    @Test
    @DisplayName("ExecutorService를 사용한 스레드 풀 예제")
    public void testExecutorService() {
        // ExecutorService 생성
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Runnable 작업 생성
        Runnable task1 = () -> {
            System.out.println("작업 1 실행 중 - 스레드: " + Thread.currentThread().getName());
        };

        Runnable task2 = () -> {
            System.out.println("작업 2 실행 중 - 스레드: " + Thread.currentThread().getName());
        };

        // 작업 실행
        executorService.execute(task1);
        executorService.execute(task2);

        // ExecutorService 종료
        executorService.shutdown();
    }

    @Test
    @DisplayName("Callable과 ExecutorService를 사용한 작업 완료 후 결과 획득")
    public void testCallableWithExecutorService() throws ExecutionException, InterruptedException {
        // ExecutorService 생성
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Callable 작업 생성
        Callable<String> callableTask = () -> {
            TimeUnit.SECONDS.sleep(1);
            return "작업 완료";
        };

        // 작업 제출 및 Future 획득
        Future<String> future = executorService.submit(callableTask);

        // Future에서 결과 가져오기
        String result = future.get(); // 작업이 완료될 때까지 대기

        // 결과 검증
        assertEquals("작업 완료", result);

        // ExecutorService 종료
        executorService.shutdown();
    }

    @DisplayName("ExecutorService, invokeAll()을 사용한 모든 작업 완료 후 결과 획득, 완료될 때까지 블록된다")
    @Test
    public void testInvokeAllBlocksUntilAllTasksComplete() throws InterruptedException {
        // ExecutorService 생성
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Callable 작업 리스트 생성 (각기 다른 실행 시간을 갖도록 설정)
        Callable<String> task1 = () -> {
            System.out.println("작업 1 시작");
            TimeUnit.SECONDS.sleep(2);
            System.out.println("작업 1 완료");
            return "작업 1 결과";
        };

        Callable<String> task2 = () -> {
            System.out.println("작업 2 시작");
            TimeUnit.SECONDS.sleep(3);
            System.out.println("작업 2 완료");
            return "작업 2 결과";
        };

        Callable<String> task3 = () -> {
            System.out.println("작업 3 시작");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("작업 3 완료");
            return "작업 3 결과";
        };

        List<Callable<String>> tasks = Arrays.asList(task1, task2, task3);

        // invokeAll 호출 전 시간 기록
        Instant start = Instant.now();

        // invokeAll 호출 (모든 작업이 완료될 때까지 블록됨)
        List<Future<String>> futures = executorService.invokeAll(tasks);

        // invokeAll 호출 후 시간 기록
        Instant end = Instant.now();

        // 총 실행 시간 계산
        Duration duration = Duration.between(start, end);
        long durationSeconds = duration.getSeconds();

        System.out.println("invokeAll() 메서드 실행 시간: " + durationSeconds + "초");

        // 가장 오래 걸리는 작업 시간 확인 (작업 2: 3초)
        assertTrue(durationSeconds >= 3, "invokeAll()은 가장 오래 걸리는 작업 시간 이상 블록되어야 합니다.");

        // 각 Future의 완료 여부 확인
        for (Future<String> future : futures) {
            assertTrue(future.isDone(), "모든 작업이 완료되어야 합니다.");
        }

        // ExecutorService 종료
        executorService.shutdown();
    }

    @DisplayName("ExecutorService, invokeAny()를 사용한 하나의 작업 완료 후 결과 획득")
    @Test
    public void testInvokeAnySuccess() throws InterruptedException, ExecutionException {
        // ExecutorService 생성
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Callable 작업 리스트 생성
        Callable<String> task1 = () -> {
            TimeUnit.SECONDS.sleep(2);
            return "작업 1 완료";
        };

        Callable<String> task2 = () -> {
            TimeUnit.SECONDS.sleep(1);
            return "작업 2 완료";
        };

        Callable<String> task3 = () -> {
            TimeUnit.SECONDS.sleep(3);
            return "작업 3 완료";
        };

        List<Callable<String>> tasks = Arrays.asList(task1, task2, task3);

        // invokeAny 호출
        String result = executorService.invokeAny(tasks);

        // 결과 검증
        assertEquals("작업 2 완료", result);

        // ExecutorService 종료
        executorService.shutdown();

        // 모든 작업이 종료될 때까지 대기
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Nested
    @DisplayName("Callable과 Future를 사용한 비동기 작업 테스트")
    class Callable_Future_Test {

        @Test
        @DisplayName("Callable과 Future를 사용하여 정상적으로 작업 완료 후 결과 획득")
        void testCallableAndFuture() throws ExecutionException, InterruptedException {
            // ExecutorService 생성
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            // Callable 작업 생성
            Callable<String> callableTask = () -> {
                TimeUnit.SECONDS.sleep(2);
                return "작업 완료";
            };

            // 작업 제출 및 Future 획득
            Future<String> future = executorService.submit(callableTask);

            // 작업이 완료되기 전에 isDone() 확인
            assertFalse(future.isDone(), "작업은 아직 완료되지 않아야 합니다.");

            // Future에서 결과 가져오기 (작업이 완료될 때까지 대기)
            String result = future.get();

            // 결과 검증
            assertEquals("작업 완료", result);

            // 작업 완료 후 isDone() 확인
            assertTrue(future.isDone(), "작업은 완료되어야 합니다.");

            // ExecutorService 종료
            executorService.shutdown();
        }

        @Test
        @DisplayName("Future.cancel()을 사용한 작업 취소")
        public void testFutureCancellation() throws InterruptedException {
            // ExecutorService 생성
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            // Callable 작업 생성 (긴 시간 동안 실행되는 작업)
            Callable<String> longRunningTask = () -> {
                TimeUnit.SECONDS.sleep(5);
                return "긴 작업 완료";
            };

            // 작업 제출 및 Future 획득
            Future<String> future = executorService.submit(longRunningTask);

            // 1초 대기 후 작업 취소 시도
            TimeUnit.SECONDS.sleep(1);
            boolean cancelResult = future.cancel(true);

            // 취소 결과 확인
            assertTrue(cancelResult, "작업 취소에 성공해야 합니다.");

            // 작업이 취소되었는지 확인
            assertTrue(future.isCancelled(), "작업은 취소되어야 합니다.");

            // 작업이 완료되지 않았는지 확인
            assertTrue(future.isDone(), "작업은 완료된 것으로 간주됩니다.");

            // 취소된 작업에서 get() 호출 시 예외 확인
            assertThrows(CancellationException.class, () -> {
                future.get();
            });

            // ExecutorService 종료
            executorService.shutdown();
        }
    }
}
