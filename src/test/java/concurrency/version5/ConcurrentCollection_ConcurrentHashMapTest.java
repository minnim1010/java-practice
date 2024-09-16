package concurrency.version5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConcurrentCollection_ConcurrentHashMapTest {

    @Test
    @DisplayName("ConcurrentHashMap을 사용한 put() 동시성 테스트")
    public void testConcurrentAccess() throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        int numberOfThreads = 10;
        int operationsPerThread = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // 쓰기 작업
        Runnable writeTask = () -> {
            for (int i = 0; i < operationsPerThread; i++) {
                map.put(i, i);
            }
        };

        // 읽기 작업
        Runnable readTask = () -> {
            for (int i = 0; i < operationsPerThread; i++) {
                map.get(i);
            }
        };

        // 쓰기 및 읽기 작업을 가진 스레드 생성
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(writeTask);
            executorService.submit(readTask);
        }

        // 스레드 종료 대기
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        // 맵의 크기 확인
        assertTrue(map.size() == operationsPerThread);

        // 데이터 무결성 확인
        for (int i = 0; i < operationsPerThread; i++) {
            Integer value = map.get(i);
            if (value != null) {
                assertEquals(i, value);
            }
        }
    }

    @Test
    @DisplayName("ConcurrentHashMap을 사용한 동시 업데이트 테스트")
    public void testConcurrentUpdates() throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        String key = "counter";
        map.put(key, 0);

        int numberOfThreads = 100;
        int incrementsPerThread = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 여러 스레드가 동일한 키의 값을 증가
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    map.compute(key, (k, v) -> v + 1);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        int expectedValue = numberOfThreads * incrementsPerThread;
        assertEquals(expectedValue, map.get(key));
    }

    @Test
    @DisplayName("ConcurrentHashMap을 사용한 compute() 동시성 테스트, 갱신 누락 문제 발생")
    public void testComputeIfAbsentConcurrency() throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        String key = "count";
        int numberOfThreads = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 스레드들이 동시에 compute를 호출하여 값 초기화 및 업데이트
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                map.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
            });
        }

        executorService.shutdown();

        // 최종 값이 스레드 수와 일치하는지 확인
        assertNotEquals(numberOfThreads, map.get(key), "최종 값은 스레드 수와 일치하지 않습니다.: " + map.get(key));
    }

    @Test
    @DisplayName("ConcurrentHashMap을 사용한 putIfAbsent() 동시성 테스트")
    public void testPutIfAbsent() throws InterruptedException {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        String key = "uniqueKey";
        int numberOfThreads = 50;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 여러 스레드가 동시에 동일한 키에 값을 넣으려고 시도
        for (int i = 0; i < numberOfThreads; i++) {
            final String value = "ValueFromThread" + i;
            executorService.submit(() -> {
                String existingValue = map.putIfAbsent(key, value);
                if (existingValue != null) {
                    assertNotEquals(value, existingValue);
                }
            });
        }

        executorService.shutdown();

        // 맵에서 해당 키의 값이 하나인지 확인
        assertNotNull(map.get(key));
        System.out.println("Final value for key '" + key + "': " + map.get(key));
    }

    private static final int THREAD_COUNT = 100;
    private static final int OPERATION_COUNT = 10000;

    @Test
    @DisplayName("HashTable, SynchornizedMap, ConcurrentHashMap  비교")
    public void testMapPerformance() throws InterruptedException {
        Map<String, Integer> hashtable = new Hashtable<>();
        Map<String, Integer> synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();

        long hashtableTime = measurePerformance(hashtable);
        long synchronizedMapTime = measurePerformance(synchronizedMap);
        long concurrentHashMapTime = measurePerformance(concurrentHashMap);

        System.out.println("Hashtable Time: " + hashtableTime + " ms");
        System.out.println("SynchronizedMap Time: " + synchronizedMapTime + " ms");
        System.out.println("ConcurrentHashMap Time: " + concurrentHashMapTime + " ms");

        assertTrue(concurrentHashMapTime <= synchronizedMapTime);
        assertTrue(concurrentHashMapTime <= hashtableTime);
    }

    private long measurePerformance(Map<String, Integer> map) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < OPERATION_COUNT; j++) {
                    String key = "key" + ThreadLocalRandom.current().nextInt(OPERATION_COUNT);
                    map.put(key, ThreadLocalRandom.current().nextInt(OPERATION_COUNT));
                    map.get(key);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    @Test
    @DisplayName("ConcurrentHashMap을 사용한 forEach(), search(), reduce() 테스트")
    public void testForEachAndSearch() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        int itemCount = 1000;

        // 맵에 데이터 추가
        for (int i = 0; i < itemCount; i++) {
            map.put("key" + i, i);
        }

        // 병렬 처리 레벨 설정
        int parallelismThreshold = 2;

        // 모든 요소 출력
        map.forEach(parallelismThreshold, (key, value) -> {
            assertNotNull(key);
            assertNotNull(value);
//            System.out.println("Thread: " + Thread.currentThread().getName() + " processing key: " + key);
        });

        // 특정 조건에 맞는 값 검색
        Integer foundValue = map.search(parallelismThreshold, (key, value) -> {
            if (value == 500) {
                return value;
            }
            return null;
        });

        assertEquals(500, foundValue);

        // 값의 합계 계산
        Integer sum = map.reduceValues(parallelismThreshold, (a, b) -> {
            System.out.println("Thread: " + Thread.currentThread().getName() + " - a = " + a + ", b = " + b);
            return a + b;
        });
        int expectedSum = (itemCount - 1) * itemCount / 2;
        assertEquals(expectedSum, sum);
    }
}
