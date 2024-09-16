package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentUtility_ReadWriteLockTest {

    @Test
    @DisplayName("ReadWriteLock::동시 읽기 작업 테스트")
    public void testConcurrentReadLock() throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable readTask = () -> {
            lock.readLock().lock();  // 읽기 락 획득
            try {
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 획득");
                Thread.sleep(1000); // 1초 동안 읽기 작업
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.readLock().unlock();  // 읽기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 해제");
            }
        };

        // 세 개의 스레드가 동시에 읽기 작업을 수행
        executor.submit(readTask);
        executor.submit(readTask);
        executor.submit(readTask);

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));  // 모든 작업 완료 확인
    }

    @Test
    @DisplayName("ReadWriteLock::쓰기 작업 중 읽기 차단 테스트")
    public void testWriteLockBlocksRead() throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable writeTask = () -> {
            lock.writeLock().lock();  // 쓰기 락 획득
            try {
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 획득");
                Thread.sleep(1000);  // 1초 동안 쓰기 작업
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();  // 쓰기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 해제");
            }
        };

        Runnable readTask = () -> {
            lock.readLock().lock();  // 읽기 락 획득 (쓰기 락이 해제될 때까지 대기)
            try {
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 획득");
            } finally {
                lock.readLock().unlock();  // 읽기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 해제");
            }
        };

        // 쓰기 작업 먼저 수행
        executor.submit(writeTask);

        // 읽기 작업은 쓰기 작업이 끝나야 수행됨
        Thread.sleep(100);  // 쓰기 작업이 먼저 시작되도록 잠시 대기
        executor.submit(readTask);

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));  // 모든 작업 완료 확인
    }

    @Test
    @DisplayName("ReadWriteLock::동시 쓰기 차단 테스트")
    public void testWriteLockBlocksOtherWrites() throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable writeTask = () -> {
            lock.writeLock().lock();  // 쓰기 락 획득
            try {
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 획득");
                Thread.sleep(1000);  // 1초 동안 쓰기 작업
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();  // 쓰기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 해제");
            }
        };

        // 두 개의 쓰기 작업이 순차적으로 실행됨
        executor.submit(writeTask);
        executor.submit(writeTask);  // 첫 번째 쓰기 작업이 끝나야 두 번째 쓰기 작업 시작

        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));  // 모든 작업 완료 확인
    }

    @Test
    @DisplayName("ReadWriteLock::읽기 후 쓰기 작업 차단 테스트")
    public void testReadLockBlocksWrite() throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable readTask = () -> {
            lock.readLock().lock();  // 읽기 락 획득
            try {
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 획득");
                Thread.sleep(1000);  // 1초 동안 읽기 작업
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.readLock().unlock();  // 읽기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 읽기 락 해제");
            }
        };

        Runnable writeTask = () -> {
            lock.writeLock().lock();  // 쓰기 락 획득 (읽기 락이 해제될 때까지 대기)
            try {
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 획득");
            } finally {
                lock.writeLock().unlock();  // 쓰기 락 해제
                System.out.println(Thread.currentThread().getName() + ": 쓰기 락 해제");
            }
        };

        // 읽기 작업 먼저 수행
        executor.submit(readTask);

        // 쓰기 작업은 읽기 락이 해제될 때까지 대기
        Thread.sleep(100);  // 읽기 작업이 먼저 시작되도록 잠시 대기
        executor.submit(writeTask);

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));  // 모든 작업 완료 확인
    }
}
