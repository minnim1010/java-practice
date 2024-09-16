package concurrency.version1_4;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitNotifyTest {

    /**
     * 1. wait()과 notify()를 사용한 단순한 생산자-소비자 예제
     */
    @DisplayName("wait()과 notify()를 사용한 생산자-소비자 예제")
    @Test
    void testWaitAndNotify() throws InterruptedException {
        Object lock = new Object();
        Thread producer = new Thread(() -> {
            synchronized (lock) {
                System.out.println("생산자: 데이터 생산 중...");
                try {
                    Thread.sleep(1000); // 데이터 생산에 1초 소요
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("생산자: 데이터 생산 완료, 소비자에게 알림");
                lock.notify(); // 대기 중인 소비자 스레드 하나를 깨움
            }
        });

        Thread consumer = new Thread(() -> {
            synchronized (lock) {
                System.out.println("소비자: 데이터 대기 중...");
                try {
                    lock.wait(); // 생산자의 알림을 기다림
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("소비자: 데이터 수신 및 처리");
            }
        });

        consumer.start();
        Thread.sleep(100); // 소비자가 먼저 대기 상태에 들어가도록 약간 대기
        producer.start();

        consumer.join();
        producer.join();
    }

    /**
     * 2. 여러 스레드가 wait()으로 대기하고, notify()로 하나의 스레드만 깨우기
     */
    @DisplayName("여러 스레드 대기 중 notify()로 하나만 깨우기")
    @Test
    void testMultipleWaitAndSingleNotify() throws InterruptedException {
        Object lock = new Object();

        Runnable waitingTask = () -> {
            synchronized (lock) {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": 대기 시작");
                try {
                    lock.wait(); // 알림을 기다림
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(threadName + ": 깨어남");
            }
        };

        Thread waiter1 = new Thread(waitingTask, "대기자1");
        Thread waiter2 = new Thread(waitingTask, "대기자2");
        Thread waiter3 = new Thread(waitingTask, "대기자3");

        waiter1.start();
        waiter2.start();
        waiter3.start();

        Thread.sleep(100); // 모든 대기자가 대기 상태에 들어가도록 약간 대기

        Runnable notifierTask = () -> {
            synchronized (lock) {
                System.out.println("통지자: notify() 호출");
                lock.notify(); // 대기 중인 스레드 하나를 깨움
            }
        };

        Thread notifier1 = new Thread(notifierTask, "통지자");
        Thread notifier2 = new Thread(notifierTask, "통지자");
        Thread notifier3 = new Thread(notifierTask, "통지자");
        notifier1.start();
        notifier2.start();
        notifier3.start();

        waiter1.join();
        waiter2.join();
        waiter3.join();
        notifier1.join();
        notifier2.join();
        notifier3.join();
    }

    /**
     * 3. 여러 스레드가 wait()으로 대기하고, notifyAll()로 모두 깨우기
     */
    @DisplayName("여러 스레드 대기 중 notifyAll()로 모두 깨우기")
    @Test
    void testMultipleWaitAndNotifyAll() throws InterruptedException {
        Object lock = new Object();

        Runnable waitingTask = () -> {
            synchronized (lock) {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": 대기 시작");
                try {
                    lock.wait(); // 알림을 기다림
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(threadName + ": 깨어남");
            }
        };

        Thread waiter1 = new Thread(waitingTask, "대기자1");
        Thread waiter2 = new Thread(waitingTask, "대기자2");
        Thread waiter3 = new Thread(waitingTask, "대기자3");

        waiter1.start();
        waiter2.start();
        waiter3.start();

        Thread.sleep(100); // 모든 대기자가 대기 상태에 들어가도록 약간 대기

        Thread notifier = new Thread(() -> {
            synchronized (lock) {
                System.out.println("통지자: notifyAll() 호출");
                lock.notifyAll(); // 대기 중인 모든 스레드를 깨움
            }
        });

        notifier.start();

        waiter1.join();
        waiter2.join();
        waiter3.join();
        notifier.join();
    }

    /**
     * 4. 동기화 블록 밖에서 wait() 또는 notify()를 호출하여 IllegalMonitorStateException 발생시키기
     */
    @DisplayName("동기화 블록 밖에서 wait() 또는 notify() 호출 시 예외 발생")
    @Test
    void testWaitNotifyOutsideSynchronizedBlock() throws InterruptedException {
        Object lock = new Object();

        Thread thread = new Thread(() -> {
            System.out.println("스레드: wait() 호출 시도");
            try {
                lock.wait(); // 동기화되지 않은 블록에서 wait() 호출
            } catch (IllegalMonitorStateException e) {
                System.out.println("예외 발생: " + e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
        thread.join();

        Thread notifier = new Thread(() -> {
            System.out.println("스레드: notify() 호출 시도");
            try {
                lock.notify(); // 동기화되지 않은 블록에서 notify() 호출
            } catch (IllegalMonitorStateException e) {
                System.out.println("예외 발생: " + e);
            }
        });

        notifier.start();
        notifier.join();
    }

    /**
     * 5. wait()이 모니터 락을 해제하고 다른 스레드가 락을 획득할 수 있음을 보여주는 예제
     */
    @DisplayName("wait() 호출 시 모니터 락을 해제함을 확인하는 테스트")
    @Test
    void testWaitReleasesLock() throws InterruptedException {
        Object lock = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread 1: 락 획득, wait() 호출하여 대기");
                try {
                    lock.wait(); // 모니터 락 해제하고 대기 상태로 들어감
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 1: 깨어남 및 작업 종료");
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread 2: 락 획득하여 작업 수행");
                // 일부 작업 수행
                System.out.println("Thread 2: 작업 완료 후 notify() 호출");
                lock.notify(); // 대기 중인 스레드 깨움
            }
        });

        thread1.start();
        Thread.sleep(100); // thread1이 먼저 락을 획득하도록 약간 대기
        thread2.start();

        thread1.join();
        thread2.join();
    }
}
