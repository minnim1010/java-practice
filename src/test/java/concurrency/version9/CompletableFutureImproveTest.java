package concurrency.version9;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CompletableFutureImproveTest {

    @Test
    @DisplayName("completeOnTimeout() 테스트")
    void testCompleteOnTimeout() throws Exception {
        // 2초 후에 기본값 0으로 완료되는 CompletableFuture
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);  // 3초 지연
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;  // 기본적으로는 10을 반환
        }).completeOnTimeout(0, 2, TimeUnit.SECONDS);  // 2초 후에 기본값으로 완료

        // 타임아웃이 발생하여 기본값 0이 반환됨
        assertEquals(0, future.get());
    }

    @Test
    @DisplayName("orTimeout() 테스트")
    void testOrTimeout() {
        // 3초 후에 10을 반환하는 CompletableFuture
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);  // 3초 지연
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;
        }).orTimeout(1, TimeUnit.SECONDS);  // 1초 내에 완료되지 않으면 TimeoutException 발생

        // TimeoutException이 발생해야 함
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    @DisplayName("completeExceptionally() 테스트")
    void testCompleteExceptionally() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("예외 발생"));

        // 예외가 발생했는지 확인
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    @DisplayName("whenComplete() 테스트")
    void testWhenComplete() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        System.out.println("작업 성공: " + result);
                    } else {
                        System.out.println("작업 실패: " + ex.getMessage());
                    }
                });

        assertEquals(10, future.get());
    }

    @Test
    @DisplayName("커스텀 스레드 풀 지원 테스트")
    void testCustomExecutor() throws Exception {
        ExecutorService customExecutor = Executors.newFixedThreadPool(2);

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10, customExecutor)
                .thenApplyAsync(result -> result * 2, customExecutor);

        assertEquals(20, future.get());
        customExecutor.shutdown();
    }
}
