package concurrency.version1_4;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ThreadTest {

    @DisplayName("스레드를 생성하고 실행한다.")
    @Test
    void test1() {
        Thread t = new Thread(() -> System.out.println("T1 - Thread running"));
        t.start();
    }

    @Nested
    @DisplayName("Thread.sleep() 메서드를 테스트한다.")
    class test_sleep {

        @DisplayName("스레드를 생성하고 1초 동안 일시 중지 후 실행한다.")
        @Test
        void test2() {
            Thread t = new Thread(() -> System.out.println("T2 - Thread running"));
            try {
                Thread.sleep(1000); // 메인 스레드에서 1초 동안 일시 중지
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t.start();
        }

        private final Object lock = new Object();

        @DisplayName("sleep() 호출 시에도 해당 스레드는 모니터 락을 해제하지 않는다.")
        @Test
        void testSleepDoesNotReleaseLock() throws InterruptedException {
            Thread thread1 = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("Thread 1: lock acquired, going to sleep");
                    try {
                        Thread.sleep(3000); // 3초 동안 일시 중지
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread 1: woke up and releasing lock");
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    // Thread 1이 락을 먼저 획득하도록 약간 대기
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 2: attempting to acquire lock");
                synchronized (lock) {
                    System.out.println("Thread 2: lock acquired");
                }
            });

            thread1.start();
            thread2.start();

            // 두 스레드가 종료될 때까지 대기
            thread1.join();
            thread2.join();

            System.out.println("Test completed");
        }
    }

    @Nested
    @DisplayName("Thread.join() 메서드를 테스트한다.")
    class test_join {
        @DisplayName("스레드를 생성하고 해당 스레드가 종료될 때까지 메인 스레드를 대기시킨다.")
        @Test
        void test3() {
            Thread t = new Thread(() -> {
                System.out.println("T3 - Thread running");
                try {
                    Thread.sleep(2000); // 스레드 내부에서 2초 동안 작업 수행
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("T3 - Thread finished");
            });
            t.start();
            try {
                t.join(); // 메인 스레드는 t 스레드가 종료될 때까지 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Main thread resumes after T3 finishes");
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("잘못된 join 사용 시 데드락이 발생한다.")
        void testDeadlockWithJoin() throws InterruptedException {
            // 두 개의 스레드를 선언합니다.
            final Thread[] threads = new Thread[2];

            threads[0] = new Thread(() -> {
                System.out.println("Thread 1 시작");
                try {
                    // thread1이 thread2를 기다립니다.
                    threads[1].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 1 종료");
            });

            threads[1] = new Thread(() -> {
                System.out.println("Thread 2 시작");
                try {
                    // thread2가 thread1을 기다립니다.
                    threads[0].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 2 종료");
            });

            // 스레드를 시작합니다.
            threads[0].start();
            threads[1].start();

            // 잠시 대기하여 데드락이 발생할 시간을 줍니다.
            Thread.sleep(2000);

            // 두 스레드가 여전히 살아 있는지 확인합니다.
            assertTrue(threads[0].isAlive(), "Thread 1은 데드락으로 인해 아직 실행 중이어야 합니다.");
            assertTrue(threads[1].isAlive(), "Thread 2는 데드락으로 인해 아직 실행 중이어야 합니다.");
        }
    }

    @DisplayName("스레드를 인터럽트한다.")
    @Test
    void test4() {
        Thread t = new Thread(() -> {
            System.out.println("T4 - Thread started");
            try {
                Thread.sleep(5000); // 5초 동안 대기 (인터럽트로 인해 중단될 예정)
            } catch (InterruptedException e) {
                System.out.println("T4 - Thread interrupted");
            }
        });
        t.start();
        try {
            Thread.sleep(2000); // 2초 후에 인터럽트 시도
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt(); // 스레드 t를 인터럽트
    }

    // 스레드 우선순위는 JVM과 운영체제의 스케줄러에 의해 관리되며, 항상 높은 우선순위의 스레드가 먼저 실행된다고 보장되지 않습니다.
    @DisplayName("우선순위가 높은 스레드가 더 빨리 실행될 수 있다.")
    @Test
    void test5() {
        Thread highPriorityThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("High Priority Thread");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread lowPriorityThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("Low Priority Thread");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 우선순위 설정 (높은 우선순위: MAX_PRIORITY, 낮은 우선순위: MIN_PRIORITY)
        highPriorityThread.setPriority(Thread.MAX_PRIORITY);
        lowPriorityThread.setPriority(Thread.MIN_PRIORITY);

        // 스레드 시작
        highPriorityThread.start();
        lowPriorityThread.start();

        // 스레드 종료 대기
        try {
            highPriorityThread.join();
            lowPriorityThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
