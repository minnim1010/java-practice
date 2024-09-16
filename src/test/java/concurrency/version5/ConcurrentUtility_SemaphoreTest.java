package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentUtility_SemaphoreTest {

    @Test
    @DisplayName("Semaphore 허가 수 테스트")
    public void testAvailablePermits() throws InterruptedException {
        Semaphore semaphore = new Semaphore(3);  // 동시에 3개의 스레드가 접근 가능

        // 허가 수는 3으로 시작
        assertEquals(3, semaphore.availablePermits(), "초기 허가 수는 3이어야 함");

        semaphore.acquire();
        semaphore.acquire();

        // 2개의 허가를 획득했으므로, 남은 허가 수는 1
        assertEquals(1, semaphore.availablePermits(), "2개의 허가를 획득 후 남은 허가 수는 1이어야 함");

        semaphore.release();
        semaphore.release();

        // 다시 2개의 허가를 해제했으므로, 허가 수는 원래대로 3
        assertEquals(3, semaphore.availablePermits(), "허가를 해제한 후 남은 허가 수는 3이어야 함");
    }


    @Test
    @DisplayName("Semaphore 기본 acquire() 및 release() 테스트")
    public void testAcquireAndRelease() throws InterruptedException {
        Semaphore semaphore = new Semaphore(2); // 동시에 2개의 스레드만 자원에 접근 가능

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable task = () -> {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " 자원 사용 중...");
                Thread.sleep(1000);  // 자원 사용 중
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " 자원 해제");
            }
        };

        // 3개의 스레드에서 작업 수행
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 테스트 완료 후 남아있는 허가 수 확인 (자원 해제 후 permit은 초기 값 2로 복구되어야 함)
        assertEquals(2, semaphore.availablePermits(), "모든 스레드가 자원을 해제한 후 permit은 2여야 함");
    }

    @Test
    @DisplayName("Semaphore tryAcquire() 테스트")
    public void testTryAcquire() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);  // 동시에 1개의 스레드만 자원에 접근 가능

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 첫 번째 스레드가 자원을 획득하고 2초간 사용
        executor.submit(() -> {
            try {
                semaphore.acquire();
                System.out.println("첫 번째 스레드 자원 사용 중...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                System.out.println("첫 번째 스레드 자원 해제");
            }
        });

        // 두 번째 스레드는 1초 동안 자원을 얻을 수 있는지 시도
        executor.submit(() -> {
            try {
                if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                    System.out.println("두 번째 스레드 자원 사용 중...");
                    semaphore.release();
                } else {
                    System.out.println("두 번째 스레드 자원을 얻지 못함");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 자원이 다시 해제된 후 permit이 1이어야 함
        assertEquals(1, semaphore.availablePermits(), "자원 해제 후 permit은 1이어야 함");
    }

    @Test
    @DisplayName("Semaphore 대기 중인 스레드 수 테스트")
    public void testGetQueueLength() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);  // 동시에 1개의 스레드만 자원에 접근 가능

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 첫 번째 스레드가 자원 획득 후 3초간 사용
        executor.submit(() -> {
            try {
                semaphore.acquire();
                System.out.println("첫 번째 스레드 자원 사용 중...");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                System.out.println("첫 번째 스레드 자원 해제");
            }
        });

        // 두 번째, 세 번째 스레드는 자원을 기다리며 대기 중
        executor.submit(() -> {
            try {
                Thread.sleep(500); // 대기 시작 전에 잠시 지연
                semaphore.acquire();
                System.out.println("두 번째 스레드 자원 사용 중...");
                semaphore.release();
                System.out.println("두 번째 스레드 자원 해제");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(500); // 대기 시작 전에 잠시 지연
                semaphore.acquire();
                System.out.println("세 번째 스레드 자원 사용 중...");
                semaphore.release();
                System.out.println(" 번째 스레드 자원 해제");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 대기 중인 스레드의 수를 확인
        Thread.sleep(1000);  // 대기 시간이 필요하므로 잠시 대기
        assertEquals(2, semaphore.getQueueLength(), "현재 대기 중인 스레드는 2명이어야 함");

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
