package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentUtility_AtomicTest {

    @Test
    @DisplayName("AtomicInteger::동시성 테스트")
    public void testAtomicIntegerConcurrency() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        int numberOfThreads = 10;
        int incrementsPerThread = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 여러 스레드가 동시에 값을 증가시키는 작업을 수행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    atomicInteger.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        // 모든 스레드가 값을 증가시키므로, 최종 값은 numberOfThreads * incrementsPerThread가 되어야 함
        assertEquals(numberOfThreads * incrementsPerThread, atomicInteger.get());
    }

    @Test
    @DisplayName("AtomicBoolean::동시성 테스트")
    public void testAtomicBooleanConcurrency() throws InterruptedException {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        int numberOfThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 여러 스레드가 동시에 값을 true로 변경하는 작업을 수행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                atomicBoolean.compareAndSet(false, true);
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        // 모든 스레드가 true로 변경을 시도했으므로, 값은 true이어야 함
        assertTrue(atomicBoolean.get());
    }

    @Test
    @DisplayName("AtomicLong::동시성 테스트")
    public void testAtomicLongConcurrency() throws InterruptedException {
        AtomicLong atomicLong = new AtomicLong(0);
        int numberOfThreads = 10;
        int incrementsPerThread = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 여러 스레드가 동시에 값을 증가시키는 작업을 수행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    atomicLong.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        // 최종 값은 numberOfThreads * incrementsPerThread이어야 함
        assertEquals(numberOfThreads * incrementsPerThread, atomicLong.get());
    }

    @Test
    @DisplayName("AtomicReference::동시성 테스트")
    public void testAtomicReferenceConcurrency() throws InterruptedException {
        AtomicReference<String> atomicReference = new AtomicReference<>("initial");
        int numberOfThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 여러 스레드가 동시에 값을 변경하는 작업을 수행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                atomicReference.compareAndSet("initial", "updated");
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        // 여러 스레드가 동시에 값을 변경하려고 시도했으므로 최종 값은 "updated"이어야 함
        assertEquals("updated", atomicReference.get());
    }
}
