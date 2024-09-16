package concurrency.version5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentUtility_CyclicBarrierTest {

    @Test
    @DisplayName("CyclicBarrier await 및 통과 가능 여부 테스트")
    public void testCyclicBarrierBasicFunctionality() throws Exception {
        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, () -> System.out.println("모든 스레드가 도착했습니다. 실행을 재개합니다."));

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 3개의 스레드에서 동시에 실행
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    System.out.println("스레드 " + threadId + " 작업 중...");
                    Thread.sleep(1000);
                    System.out.println("스레드 " + threadId + " 바리어 대기 중...");
                    barrier.await();  // 모든 스레드가 도착할 때까지 대기
                    System.out.println("스레드 " + threadId + " 바리어 통과 후 실행");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("CyclicBarrier 타임아웃 테스트")
    public void testCyclicBarrierTimeout() throws Exception {
        int numberOfThreads = 4;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 2개의 스레드는 도착하지만, 마지막 스레드는 지연됨
        for (int i = 0; i < numberOfThreads - 2; i++) {
            executor.submit(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 바리어 대기 중...");
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // 마지막 스레드는 도착하지 않음 -> 타임아웃 발생 유도
        Future<?> timeoutTest = executor.submit(() -> {
            try {
                barrier.await(2, TimeUnit.SECONDS); // 여기서 타임아웃을 기대
            } catch (TimeoutException e) {
                throw new RuntimeException(e); // 타임아웃 예외 발생 시
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 타임아웃이 발생하는지 확인
        assertThrows(ExecutionException.class, timeoutTest::get); // 타임아웃이 발생하면 ExecutionException에 래핑되어 발생

        executor.shutdown();
    }

    @Test
    @DisplayName("CyclicBarrier::getNumberWaiting 테스트")
    public void testGetNumberWaiting() throws Exception {
        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 2개의 스레드는 도착한 상태로 대기
        for (int i = 0; i < numberOfThreads - 1; i++) {
            executor.submit(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // 현재 대기 중인 스레드 수 확인
        Thread.sleep(1000);  // 스레드가 대기 상태에 들어갈 시간을 줌
        assertEquals(2, barrier.getNumberWaiting(), "현재 대기 중인 스레드 수는 2여야 합니다.");
        assertEquals(3, barrier.getParties(), "동기화에 참여하는 총 스레드 수는 3여야 합니다.");

        executor.shutdown();
    }

    @Test
    @DisplayName("CyclicBarrier::isBroken 테스트")
    public void testIsBroken() throws Exception {
        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 하나의 스레드는 예외를 발생시켜 바리어가 중단됨
        executor.submit(() -> {
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            try {
                throw new RuntimeException("중단된 스레드");
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                // 다른 스레드에서 예외 발생 후 Barier가 중단됨
                assertTrue(barrier.isBroken(), "바리어는 중단된 상태여야 합니다.");
            }
        });

        executor.shutdown();
    }

    @Test
    @DisplayName("CyclicBarrier 재사용 테스트")
    public void testCyclicBarrierReuse() throws Exception {
        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, () -> System.out.println("모든 스레드가 도착했습니다. 다음 작업을 시작합니다."));

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < 2; i++) {  // 2번의 사이클 실행
            for (int j = 0; j < numberOfThreads; j++) {
                executor.submit(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " 작업 중...");
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + " 바리어 대기 중...");
                        barrier.await();  // 모든 스레드가 도착할 때까지 대기
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // 모든 스레드가 재사용된 바리어에서 다시 동기화됨
        }

        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
    }
}
