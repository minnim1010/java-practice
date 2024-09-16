package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentUtility_ReentrantLockTest {

    @Test
    @DisplayName("ReentrantLock::lock, unlock 테스트")
    public void testLockAndUnlock() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            lock.lock(); // 스레드 1이 락을 획득
            try {
                System.out.println("스레드 1: 락 획득");
                Thread.sleep(1000); // 1초 동안 락을 소유
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock(); // 락 해제
                System.out.println("스레드 1: 락 해제");
            }
        });

        Thread.sleep(100); // 스레드 1이 락을 먼저 획득하게 약간 대기

        executor.submit(() -> {
            lock.lock(); // 스레드 2가 락을 획득
            try {
                System.out.println("스레드 2: 락 획득");
            } finally {
                lock.unlock(); // 락 해제
                System.out.println("스레드 2: 락 해제");
            }
        });

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS)); // 스레드 작업 완료 확인
    }

    @Test
    @DisplayName("ReentrantLock::tryLock 성공 테스트")
    public void testTryLockSuccess() {
        ReentrantLock lock = new ReentrantLock();

        boolean lockAcquired = lock.tryLock(); // 락 시도
        try {
            assertTrue(lockAcquired, "락을 획득해야 합니다."); // 락 획득 확인
        } finally {
            if (lockAcquired) {
                lock.unlock(); // 락 해제
            }
        }
    }

    @Test
    @DisplayName("ReentrantLock::tryLock 실패 테스트")
    public void testTryLockFailure() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        lock.lock(); // 메인 스레드가 락을 획득

        executor.submit(() -> {
            boolean lockAcquired = lock.tryLock(); // 다른 스레드가 락을 시도
            try {
                assertFalse(lockAcquired, "락을 획득할 수 없어야 합니다."); // 락 획득 실패 확인
            } finally {
                if (lockAcquired) {
                    lock.unlock();
                }
            }
        });

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS)); // 스레드 작업 완료 대기
        lock.unlock(); // 메인 스레드 락 해제
    }

    @Test
    @DisplayName("ReentrantLock::lockInterruptibly 테스트")
    public void testLockInterruptibly() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        ExecutorService executor = Executors.newFixedThreadPool(1);

        lock.lock(); // 메인 스레드가 락을 먼저 획득

        Thread interruptingThread = new Thread(() -> {
            try {
                lock.lockInterruptibly(); // 다른 스레드가 락을 시도, 대기 가능
                fail("Interrupt로 인해 락을 획득하지 못해야 합니다.");
            } catch (InterruptedException e) {
                System.out.println("스레드가 인터럽트로 인해 중단되었습니다.");
            }
        });

        interruptingThread.start();
        Thread.sleep(500); // 다른 스레드가 락을 기다리게 약간 대기
        interruptingThread.interrupt(); // 스레드에 인터럽트 발생

        interruptingThread.join(); // 인터럽트 스레드 종료 대기
        lock.unlock(); // 메인 스레드 락 해제
    }

    @Test
    @DisplayName("ReentrantLock::isLocked 테스트")
    public void testIsLocked() {
        ReentrantLock lock = new ReentrantLock();

        assertFalse(lock.isLocked(), "락이 걸려 있지 않아야 합니다.");

        lock.lock(); // 락 획득
        try {
            assertTrue(lock.isLocked(), "락이 걸려 있어야 합니다.");
        } finally {
            lock.unlock(); // 락 해제
        }

        assertFalse(lock.isLocked(), "락이 해제되어 있어야 합니다.");
    }

    @Test
    @DisplayName("ReentrantLock::newCondition 테스트")
    public void testNewCondition() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        var condition = lock.newCondition();
        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(() -> {
            lock.lock();
            try {
                System.out.println("스레드가 조건 대기 중...");
                condition.await(); // 조건을 대기 중
                System.out.println("조건이 충족되었습니다. 스레드가 다시 실행됩니다.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        Thread.sleep(1000); // 스레드가 조건 대기 상태에 들어가게 대기

        lock.lock(); // 조건을 충족시키기 위해 락 획득
        try {
            System.out.println("조건을 충족시킵니다.");
            condition.signal(); // 조건 신호 보내기
        } finally {
            lock.unlock(); // 락 해제
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    }
}
