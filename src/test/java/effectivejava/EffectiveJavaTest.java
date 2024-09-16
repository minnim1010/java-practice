package effectivejava;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

class EffectiveJavaTest {

    @Test
    void tryWithResources_구문_안에서_생성하지않아도_autoclose가능() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try (reader) {
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThatThrownBy(reader::readLine);
    }

    @Test
    void Properties가_HashTable을_상속하고있다() {
        /**
         * Properties는 키와 값으로 문자열만을 허용하도록 설계하려 하였으나,
         * HashTable의 메서드를 직접 호출하면 불변식을 깨버릴 수 있음
         */
        //given
        Properties properties = new Properties();
        //when
        //then
    }

    @Test
    void push와_pop연산만_존재하는_stack에_임의원소를_삽입할수있다() {
        //given
        Stack<String> stack = new Stack<>();
        stack.push("first");
        stack.push("second");
        stack.push("third");
        //when
        stack.add(1, "fourth");
        //then
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        /**
         * third
         * second
         * fourth
         * first
         */
    }

    interface Greeting {
        void greet();

        void sayHello();
    }

    @Test
    void 익명클래스는_해당익명클래스타입의_메서드만_사용가능하다() {
        Greeting anonymousClass = new Greeting() {
            @Override
            public void greet() {
                System.out.println("Hello, I'm an anonymous class!");
            }

            @Override
            public void sayHello() {
                System.out.println("Saying hello!");
            }

            public void additionalMethod() {
                System.out.println("This is an additional method.");
            }
        };

        anonymousClass.greet(); // 가능
        anonymousClass.sayHello(); // 가능

        // 아래 코드는 컴파일 에러를 발생시킵니다.
        // 익명 클래스를 Greeting 타입으로 선언했기 때문에 Greeting 인터페이스에 정의된 메서드만 호출 가능합니다.
//         anonymousClass.additionalMethod();
    }

    @Test
    void 제네릭의_비한정적_와일드타입에는_null만_넣을수있음() {
        List<?> list = new ArrayList<>();

        list.add(null);
//        컴파일 에러 발생
//        error: incompatible types:String cannot be converted to CAP#1
//        list.add("asdf");
    }

    @Test
    void 제네릭의_타입안전성이_깨지므로_dangerous호출시_ClassCastException이_발생한다() {
        assertThatThrownBy(() -> dangerous(List.of("123"), List.of("234")))
                .isInstanceOf(ClassCastException.class);
    }

    static void dangerous(List<String>... stringLists) {
        List<Integer> intList = List.of(42);
        Object[] objects = stringLists;
        objects[0] = intList;
        String s = stringLists[0].get(0);
    }

    @Test
    void pickTwo호출시_Object배열이_반환되므로_ClassCastException이_발생한다() {
//        assertThatThrownBy(() -> {
//            String[] strings = pickTwo("1", "2", "3");
//        })
//                .isInstanceOf(ClassCastException.class);
        String[] strings = pickTwo("1", "2", "3");
    }

    static <T> T[] toArray(T... args) {
        return args;
    }

    static <T> T[] pickTwo(T a, T b, T c) {
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0 -> {
                return toArray(a, b);
            }
            case 1 -> {
                return toArray(a, c);
            }
            case 2 -> {
                return toArray(b, c);
            }
        }
        throw new AssertionError();
    }

    @Test
    @ParameterizedTest
    void a내부에서_ClassCastException이_발생한다() {
        //given
        String[] a = a();
    }

    static String[] a() {
        return (String[]) new Object[]{"1", "2"};
    }

    @Test
    void 람다와_메서드시그니처의_문법차이(){
        Consumer<String> lambda = (m) -> print(m);
        Consumer<String> methodReference = this::print;
    }

    public void print(String message){
        System.out.println(message);
    }

    public void printMessage(){
        System.out.println("message");
    }

    @Test
    void submit_다중정의에_다중정의되지않은_메서드를주면_실행됨(){
        //given
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.submit(this::printMessage);
    }

    @Test
    void 배열원소가0이라면_항상불변(){
        //given
        int[] a = new int[0];
    }

    static class Classifier {
        public static String classify(Set<?> s) {
            return "집합";
        }

        public static String classify(List<?> s) {
            return "리스트";
        }

        public static String classify(Collection<?> s) {
            return "그 외";
        }
    }

    @Test
    void 실제_실행될_다중정의메서드는_컴파일타임에_결정된다(){
        Collection<?>[] collections = {
                new HashSet<String>(),
                new ArrayList<Integer>(),
                new HashMap<Integer, Integer>().values()
        };

        for (Collection<?> c : collections) {
            System.out.println(Classifier.classify(c));
        }
    }

    @Test
    void String의_다중정의된valueOf는_같은객체를건네더라도_다른일을수행할수있다(){
        char[] charArray = {'H', 'e', 'l', 'l', 'o'};
        String strFromCharArray = String.valueOf(charArray);
        System.out.println(strFromCharArray); // Output: Hello

        Object obj = new char[] {'H', 'e', 'l', 'l', 'o'};
        String strFromObject = String.valueOf(obj);
        System.out.println(strFromObject); // Output: [C@<hashcode>
    }
}
