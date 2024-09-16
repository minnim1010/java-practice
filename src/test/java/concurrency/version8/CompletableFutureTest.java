package concurrency.version8;

import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompletableFutureTest {

    @Test
    @DisplayName("비동기적으로 값을 반환하는 CompletableFuture 테스트")
    void testSupplyAsync() throws ExecutionException, InterruptedException {
        // 비동기적으로 10을 반환
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10);

        // future.get()을 통해 비동기 작업이 완료되면 결과를 얻음
        assertEquals(10, future.get());
    }

    @Test
    @DisplayName("thenApply()를 사용하여 비동기 작업의 결과를 처리")
    void testThenApply() throws ExecutionException, InterruptedException {
        // 비동기적으로 값을 반환하고, thenApply로 2배로 변환
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10)
                .thenApply(result -> result * 2);  // 10 * 2 = 20

        // 비동기 작업의 결과가 20인지 확인
        assertEquals(20, future.get());
    }

    @Test
    @DisplayName("thenCombine()을 사용하여 두 개의 비동기 작업 결합")
    void testThenCombine() throws ExecutionException, InterruptedException {
        // 두 개의 비동기 작업 실행
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 20);

        // 두 결과를 결합하여 더하는 작업 수행
        CompletableFuture<Integer> combinedFuture = future1.thenCombine(future2, Integer::sum); // 10 + 20 = 30

        // 결합된 결과가 30인지 확인
        assertEquals(30, combinedFuture.get());
    }

    @Test
    @DisplayName("exceptionally()를 사용하여 비동기 작업의 예외 처리")
    void testExceptionally() throws ExecutionException, InterruptedException {
        // 비동기 작업에서 예외 발생
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            if (true) {
                throw new RuntimeException("비동기 작업 실패!");
            }
            return 10;
        }).exceptionally(ex -> {
            // 예외가 발생하면 기본값으로 0을 반환
            System.out.println("Exception occurred: " + ex.getMessage());
            return 0;
        });

        // 예외가 발생하였지만, exceptionally 블록으로 인해 0이 반환됨
        assertEquals(0, future.get());
    }

    @Test
    @DisplayName("비동기 작업에서 시간 지연 처리 테스트")
    void testDelayedAsync() throws ExecutionException, InterruptedException, TimeoutException {
        // 2초 후에 결과를 반환하는 비동기 작업
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);  // 2초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;
        });

        // 작업이 완료될 때까지 최대 3초 기다리며 결과 확인
        assertEquals(10, future.get(3, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("join()을 사용한 비동기 작업 결과 즉시 확인")
    void testJoin() {
        // 비동기 작업 실행
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10);

        // join()을 사용하여 예외 없이 결과를 바로 반환 (Blocking)
        assertEquals(10, future.join());
    }

    @Test
    @DisplayName("thenAccept()를 사용하여 결과를 소비하는 비동기 작업 테스트")
    void testThenAccept() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> 10)
                .thenAccept(result -> {
                    // 결과를 받아서 출력 (결과를 소비)
                    System.out.println("Result: " + result);
                    assertEquals(10, result);
                });

        // 비동기 작업의 완료를 대기
        future.get();
    }

    @Test
    @DisplayName("allOf()를 사용하여 여러 비동기 작업을 기다림")
    void testAllOf() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> 30);

        // 모든 비동기 작업이 완료될 때까지 기다림
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);

        // allOf가 완료된 후 각 결과를 확인
        allOf.get();  // 모든 작업이 완료되었는지 확인

        assertEquals(10, future1.get());
        assertEquals(20, future2.get());
        assertEquals(30, future3.get());
    }

    @Test
    @DisplayName("anyOf()를 사용하여 가장 먼저 완료된 비동기 작업을 기다림")
    void testAnyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);  // 3초 지연
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);  // 1초 지연
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 20;
        });
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> 30);  // 즉시 반환

        // 가장 먼저 완료된 비동기 작업을 기다림
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future1, future2, future3);

        // 가장 빨리 완료된 작업의 결과 확인 (future3이 즉시 반환되므로 30)
        assertEquals(30, anyOf.get());
    }
}
