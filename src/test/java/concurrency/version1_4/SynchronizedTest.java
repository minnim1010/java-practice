package concurrency.version1_4;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SynchronizedTest {

    // 공유 자원인 Counter 클래스 (동기화 없음)
    static class Counter {
        private int count = 0;

        public void increment() {
            count++; // 동기화되지 않은 메서드
        }

        public int getCount() {
            return count;
        }
    }

    // 동기화된 Counter 클래스
    static class SynchronizedCounter {
        private int count = 0;

        public synchronized void increment() {
            count++; // 동기화된 메서드
        }

        public int getCount() {
            return count;
        }
    }

    @DisplayName("동기화 없이 여러 스레드에서 카운터 증가 - 데이터 정합성 문제 발생")
    @Test
    void testWithoutSynchronization() throws InterruptedException {
        Counter counter = new Counter();

        // 카운터를 1씩 증가시키는 작업
        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                counter.increment();
            }
        };

        // 두 개의 스레드 생성
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        // 스레드 시작
        thread1.start();
        thread2.start();

        // 스레드 종료 대기
        thread1.join();
        thread2.join();

        // 결과 출력
        System.out.println("동기화 없이 최종 카운트 값: " + counter.getCount());
    }

    @DisplayName("동기화하여 여러 스레드에서 카운터 증가 - 데이터 정합성 유지")
    @Test
    void testWithSynchronization() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();

        // 카운터를 1씩 증가시키는 작업
        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                counter.increment();
            }
        };

        // 두 개의 스레드 생성
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        // 스레드 시작
        thread1.start();
        thread2.start();

        // 스레드 종료 대기
        thread1.join();
        thread2.join();

        // 결과 출력
        System.out.println("동기화하여 최종 카운트 값: " + counter.getCount());
    }
}
