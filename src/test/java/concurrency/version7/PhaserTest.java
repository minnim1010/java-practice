package concurrency.version7;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PhaserTest {

    // 여러 스레드가 단계별로 동기화되는 테스트
    @Test
    @DisplayName("Phaser 단계별 동기화 테스트")
    public void testPhaserPhaseSynchronization() throws InterruptedException {
        int numberOfThreads = 3;
        Phaser phaser = new Phaser(numberOfThreads);  // 3개의 파티 등록

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + ": 1단계 도착");
                phaser.arriveAndAwaitAdvance();  // 첫 번째 단계 완료 후 대기
                System.out.println(Thread.currentThread().getName() + ": 2단계 도착");
                phaser.arriveAndAwaitAdvance();  // 두 번째 단계 완료 후 대기
                System.out.println(Thread.currentThread().getName() + ": 종료");
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES), "스레드들이 모두 완료되어야 합니다.");
    }

    // 여러 단계의 테스트
    @Test
    @DisplayName("Phaser 여러 단계 테스트")
    public void testPhaserMultiplePhases() throws InterruptedException {
        Phaser phaser = new Phaser(1); // 메인 스레드만 등록됨

        Thread worker1 = new Thread(() -> {
            phaser.register();  // 새로운 스레드 동적 등록
            for (int phase = 1; phase <= 3; phase++) {
                System.out.println(Thread.currentThread().getName() + ": " + phase + "단계 도착");
                phaser.arriveAndAwaitAdvance();  // 각 단계 완료 후 대기
            }
            phaser.arriveAndDeregister();  // 모든 단계 완료 후 스레드 해제
        });

        worker1.start();

        for (int phase = 1; phase <= 3; phase++) {
            System.out.println("메인 스레드: " + phase + "단계 완료");
            phaser.arriveAndAwaitAdvance();  // 메인 스레드도 각 단계 완료
        }

        worker1.join();
        assertEquals(1, phaser.getRegisteredParties(), "메인 스레드를 제외한 모든 스레드가 해제되어야 합니다.");
    }

    // 타임아웃을 포함한 단계 동기화 테스트
    @Test
    @DisplayName("Phaser 타임아웃을 포함한 동기화 테스트")
    public void testPhaserTimeout() throws InterruptedException {
        Phaser phaser = new Phaser(1);  // 메인 스레드만 등록

        Thread worker = new Thread(() -> {
            phaser.register();
            try {
                System.out.println(Thread.currentThread().getName() + ": 1단계 도착");
                phaser.arriveAndAwaitAdvance();  // 1단계 대기
                System.out.println(Thread.currentThread().getName() + ": 2단계 도착");
                int i = phaser.awaitAdvanceInterruptibly(phaser.getPhase(), 1, TimeUnit.SECONDS);
                if (i < 0) {
                    System.out.println(Thread.currentThread().getName() + ": 타임아웃 없이 2단계 진행됨");
                }
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + ": 타임아웃 발생 ");
                e.printStackTrace();
            }
        });

        worker.start();

        // 1단계 대기 후 2초 후에만 완료되도록 함
        phaser.arriveAndAwaitAdvance();
        Thread.sleep(2000); // 타임아웃이 발생하도록 대기
        phaser.arriveAndAwaitAdvance();

        worker.join();
    }

    // 스레드 동적으로 추가 및 동적 해제 테스트
    @Test
    @DisplayName("Phaser 동적 스레드 추가 및 동적 해제")
    public void testDynamicThreadRegistrationAndDeregistration() throws InterruptedException {
        Phaser phaser = new Phaser(1);  // 메인 스레드만 등록

        Thread worker1 = new Thread(() -> {
            phaser.register();  // 첫 번째 스레드 동적 등록
            phaser.arriveAndAwaitAdvance();
            System.out.println("Worker1: 1단계 완료");
            phaser.arriveAndDeregister();  // 첫 번째 스레드 동적 해제
            System.out.println("Worker1: 해제됨");
        });

        Thread worker2 = new Thread(() -> {
            phaser.register();  // 두 번째 스레드 동적 등록
            phaser.arriveAndAwaitAdvance();
            System.out.println("Worker2: 1단계 완료");
            phaser.arriveAndDeregister();  // 두 번째 스레드 동적 해제
            System.out.println("Worker2: 해제됨");
        });

        worker1.start();
        worker2.start();

        Thread.sleep(100);
        phaser.arriveAndAwaitAdvance();  // 메인 스레드가 첫 번째 단계 완료
        worker1.join();
        worker2.join();

        assertEquals(1, phaser.getRegisteredParties(), "메인 스레드를 제외한 모든 스레드가 해제되어야 합니다.");
    }
}
