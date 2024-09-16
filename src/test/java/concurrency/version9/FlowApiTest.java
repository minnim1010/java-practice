package concurrency.version9;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlowApiTest {

    // 간단한 Publisher-Subscriber 연결 테스트
    @Test
    @DisplayName("Flow API의 기본 Publisher-Subscriber 연결 테스트")
    void testBasicFlow() throws InterruptedException {
        // Publisher 생성
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        
        // Publisher에 Subscriber 구독 등록
        publisher.subscribe(subscriber);
        
        // Publisher가 데이터 전송
        publisher.submit("Hello, Flow API!");
        publisher.close();

        TimeUnit.MILLISECONDS.sleep(100);
        // Subscriber에서 데이터를 제대로 받았는지 확인
        assertEquals("Hello, Flow API!", subscriber.getReceivedData());
    }

    // 백프레셔(backpressure) 테스트
    @Test
    @DisplayName("백프레셔 적용 테스트")
    void testBackpressure() throws InterruptedException {
        // Publisher 생성
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        SlowSubscriber subscriber = new SlowSubscriber(1);  // 느린 소비자 생성
        
        // Publisher에 Subscriber 구독 등록
        publisher.subscribe(subscriber);
        
        // 다량의 데이터 전송
        for (int i = 0; i < 10; i++) {
            publisher.submit(i);
        }
        
        // Publisher 종료
        publisher.close();
        
        // Subscriber가 일부 데이터만 처리한 것 확인 (백프레셔 때문에)
        Thread.sleep(2000); // 충분한 대기 시간 부여
        assertEquals(1, subscriber.getProcessedCount());
    }

    // Processor 테스트
    @Test
    @DisplayName("Processor를 이용한 데이터 변환 테스트")
    void testProcessor() throws InterruptedException {
        // Processor 생성 (Publisher -> Processor -> Subscriber)
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        UpperCaseProcessor processor = new UpperCaseProcessor();
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        
        // Processor를 연결하여 데이터 흐름을 구성
        publisher.subscribe(processor);
        processor.subscribe(subscriber);
        
        // Publisher가 데이터를 전송
        publisher.submit("hello");
        publisher.close();

        TimeUnit.MILLISECONDS.sleep(100);
        // Subscriber가 Processor에서 변환된 데이터를 받았는지 확인
        assertEquals("HELLO", subscriber.getReceivedData());
    }

    // 간단한 Subscriber 구현
    static class TestSubscriber<T> implements Flow.Subscriber<T> {
        private Flow.Subscription subscription;
        private T receivedData;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1); // 데이터 하나 요청
        }

        @Override
        public void onNext(T item) {
            this.receivedData = item;
            subscription.request(1); // 다음 데이터 요청
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("완료");
        }

        public T getReceivedData() {
            return receivedData;
        }
    }

    // 느린 Subscriber 구현 (백프레셔 테스트용)
    static class SlowSubscriber implements Flow.Subscriber<Integer> {
        private final int maxItems;
        private Flow.Subscription subscription;
        private AtomicInteger processedCount = new AtomicInteger(0);

        public SlowSubscriber(int maxItems) {
            this.maxItems = maxItems;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(maxItems); // 제한된 수의 데이터 요청
        }

        @Override
        public void onNext(Integer item) {
            processedCount.incrementAndGet();
            try {
                Thread.sleep(1000); // 느리게 처리
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("완료");
        }

        public int getProcessedCount() {
            return processedCount.get();
        }
    }

    // 대문자로 변환하는 Processor 구현
    static class UpperCaseProcessor extends SubmissionPublisher<String> implements Flow.Processor<String, String> {
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1); // 처음 데이터 요청
        }

        @Override
        public void onNext(String item) {
            submit(item.toUpperCase()); // 대문자로 변환 후 전송
            subscription.request(1); // 다음 데이터 요청
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            close(); // 완료 시 종료
        }
    }
}
