package concurrency.version5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentCollection_CopyOnWriteArrayListTest {

    @Test
    @DisplayName("CopyOnWriteArrayList::add 동시성 테스트")
    public void testConcurrentAdd() throws InterruptedException {
        // CopyOnWriteArrayList 생성
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 10개의 스레드에서 각 1000개의 요소를 추가
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    list.add(j);
                }
            });
        }

        // ExecutorService 종료
        executor.shutdown();
        executor.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);

        // 최종 크기 확인
        assertEquals(10000, list.size());
    }

    @Test
    public void testConcurrentReadAndWrite() throws InterruptedException {
        // CopyOnWriteArrayList 생성
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        // 10개의 스레드를 생성하여 읽기 작업 수행
        ExecutorService readExecutor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            readExecutor.submit(() -> {
                for (String item : list) {
                    System.out.println("Thread: " + Thread.currentThread().getName() + " - 읽기 작업: " + item);
                }
            });
        }

        // 쓰기 작업 수행
        ExecutorService writeExecutor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            writeExecutor.submit(() -> {
                list.add("New Element");
            });
        }

        // Executor 종료
        readExecutor.shutdown();
        writeExecutor.shutdown();

        // 완료될 때까지 대기
        readExecutor.awaitTermination(1, TimeUnit.MINUTES);
        writeExecutor.awaitTermination(1, TimeUnit.MINUTES);

        // 최종 요소 확인
        assertTrue(list.contains("New Element"));
        list.remove("New Element");
        assertTrue(list.contains("New Element"));
    }

    @Test
    @DisplayName("CopyOnWriteArrayList::iterator 스냅샷 테스트 - 크기는 변경되지만 반복자에 반영되지 않음")
    public void testIteratorSnapshotWithSizeChange() {
        // CopyOnWriteArrayList 생성
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        // 반복 중에 리스트에 변경 작업
        int count = 0;
        for (String item : list) {
            ++count;
            if (item.equals("B")) {
                list.add("D");  // 반복 중에 추가

                // 리스트의 크기는 즉시 변경되어 반영됨
                assertEquals(4, list.size(), "리스트 크기는 4로 반영되어야 합니다.");
            }

            // 하지만 반복자에서는 스냅샷이 사용되므로, 새로 추가된 "D"는 출력되지 않음
            assertNotEquals("D", item, "반복자에서는 'D'를 보지 못해야 합니다.");
        }

        // 반복이 끝난 후 리스트 크기 확인
        assertEquals(3, count, "순회한 리스트 크기는 3입니다.");
        assertEquals(4, list.size(), "리스트의 크기는 최종적으로 4이어야 합니다.");
        assertTrue(list.contains("D"), "'D'는 리스트에 포함되어야 합니다.");
    }
}
