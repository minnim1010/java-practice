package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentUtility_CountDownLatchTest {

    @Test
    @DisplayName("CountDownLatch를 사용한 스레드 간 작업 조율")
    public void testCountDownLatch() throws InterruptedException {
        int numberOfThreads = 3;
        CountDownLatch latch = new CountDownLatch(numberOfThreads); // 3개의 스레드를 기다림

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 3개의 스레드에서 작업 수행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    // 각 스레드에서 수행할 작업
                    System.out.println("작업 수행 중...");
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // 작업이 완료되면 카운트 다운
                }
            });
        }

        // 메인 스레드는 모든 스레드가 완료될 때까지 대기
        latch.await();
        System.out.println("모든 작업 완료!");

        executor.shutdown();

        // 테스트 완료 확인
        assertEquals(0, latch.getCount(), "Latch 카운트는 0이어야 합니다.");
    }

    @Test
    @DisplayName("CountDownLatch를 사용한 순차적 작업 수행")
    public void testSequentialExecutionWithCountDownLatch() throws InterruptedException {
        CountDownLatch firstTaskLatch = new CountDownLatch(1); // 첫 번째 작업이 완료될 때까지 대기
        CountDownLatch secondTaskLatch = new CountDownLatch(1); // 두 번째 작업이 완료될 때까지 대기

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 첫 번째 작업
        executor.submit(() -> {
            try {
                System.out.println("첫 번째 작업 수행 중...");
                Thread.sleep(1000); // 작업 수행
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                firstTaskLatch.countDown(); // 첫 번째 작업 완료
            }
        });

        // 두 번째 작업 (첫 번째 작업이 완료되면 시작)
        executor.submit(() -> {
            try {
                firstTaskLatch.await(); // 첫 번째 작업이 완료될 때까지 대기
                System.out.println("두 번째 작업 수행 중...");
                Thread.sleep(1000); // 작업 수행
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                secondTaskLatch.countDown(); // 두 번째 작업 완료
            }
        });

        // 세 번째 작업 (두 번째 작업이 완료되면 시작)
        executor.submit(() -> {
            try {
                secondTaskLatch.await(); // 두 번째 작업이 완료될 때까지 대기
                System.out.println("세 번째 작업 수행 중...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("CountDownLatch를 사용한 동시 작업 시작")
    public void testConcurrentStart() throws InterruptedException {
        int numberOfThreads = 3;
        CountDownLatch startSignal = new CountDownLatch(1); // 시작 신호
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 3개의 스레드를 동시에 시작
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 준비 중...");
                    startSignal.await(); // 시작 신호를 기다림
                    System.out.println(Thread.currentThread().getName() + " 시작!");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 모든 스레드가 준비되었을 때 신호를 보냄
        System.out.println("모든 스레드 준비 완료, 시작 신호 발사!");
        Thread.sleep(1000); // 1초 대기 후 신호 전송
        startSignal.countDown(); // 신호 전송

        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}
