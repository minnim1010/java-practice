import java.lang.reflect.Method;

public class MethodInvokePerformanceTest {

    public static void main(String[] args) {
        int iterations = 1000000; // 실행 횟수
        Method method = null;

        try {
            method = ExampleObject.class.getMethod("exampleMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        long startTime = System.nanoTime();
        testDirectMethodCall(iterations);
        long endTime = System.nanoTime();
        long directMethodCallTime = endTime - startTime;
        System.out.println("직접 메서드 호출 소요 시간\n" + directMethodCallTime + " ns");

        startTime = System.nanoTime();
        testMethodInvoke(iterations, method);
        endTime = System.nanoTime();
        long methodInvokeTime = endTime - startTime;
        System.out.println("Method.invoke() 소요 시간\n" + methodInvokeTime + " ns");

        System.out.println("Method.invoke()와 직접 메서드 호출 간의 성능 차이: " + (methodInvokeTime - directMethodCallTime) + " ns");
    }

    public static void testMethodInvoke(int iterations, Method method) {
        ExampleObject obj = new ExampleObject();
        try {
            for (int i = 0; i < iterations; i++) {
                method.invoke(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testDirectMethodCall(int iterations) {
        ExampleObject obj = new ExampleObject();
        for (int i = 0; i < iterations; i++) {
            obj.exampleMethod();
        }
    }
}
class ExampleObject {
    public void exampleMethod() {
        // 메서드 내용
    }
}
