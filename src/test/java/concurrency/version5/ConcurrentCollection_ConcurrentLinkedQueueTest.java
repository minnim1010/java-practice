package concurrency.version5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentCollection_ConcurrentLinkedQueueTest {

    @Test
    @DisplayName("ConcurrentLinkedQueue::add 동시성 테스트")
    public void testConcurrentAdd() throws InterruptedException {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        int numberOfThreads = 10;
        int operationsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 10개의 스레드에서 동시에 1000개의 요소 추가
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    queue.add(j);
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES));

        // 큐의 크기가 모든 요소를 반영하는지 확인
        assertEquals(numberOfThreads * operationsPerThread, queue.size());
    }

    @Test
    @DisplayName("ConcurrentLinkedQueue::poll 동시성 테스트")
    public void testConcurrentPoll() throws InterruptedException {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        int numberOfThreads = 10;
        int elementsPerThread = 1000;

        // 큐에 미리 10000개의 요소 추가
        for (int i = 0; i < numberOfThreads * elementsPerThread; i++) {
            queue.add(i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // 10개의 스레드가 동시에 요소 제거
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < elementsPerThread; j++) {
                    queue.poll();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES));

        // 큐가 비었는지 확인
        assertEquals(0, queue.size());
    }
}
