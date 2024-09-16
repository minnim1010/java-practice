package stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreamTest {

    @Test
    void distinct() {
        //given
        List<String> a = List.of("1", "2", "1");
        //when
        long count = a.stream()
                .distinct()
                .count();
        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 인수1개_groupingBy() {
        //given
        List<String> a = List.of("aaaaa", "bbbbb", "cc", "dd", "eee", "fff", "ggg");
        //when
        Map<Integer, List<String>> map = a.stream()
                .collect(groupingBy(String::length));
        //then
        System.out.println(map);
    }

    @Test
    void 인수2개_groupingBy() {
        //given
        List<String> a = List.of("aaaaa", "bbbbb", "cc", "dd", "eee", "fff", "ggg");
        //when
        Map<Integer, Long> map = a.stream()
                .collect(groupingBy(String::length, counting()));
        //then
        System.out.println(map);
    }

    @Test
    void 인수3개_groupingBy() {
        //given
        List<String> a = List.of("aaaaa", "bbbbb", "cc", "dd", "eee", "fff", "ggg");
        //when
        Map<Integer, Long> map = a.stream()
                .collect(groupingBy(String::length, TreeMap::new, counting()));
        //then
        System.out.println(map);
    }

    @Test
    void 인수1개_paritioningBy() {
        //given
        List<String> a = List.of("aaaaa", "bbbbb", "cc", "dd", "eee", "fff", "ggg");
        //when
        Map<Boolean, List<String>> map = a.stream()
                .collect(partitioningBy(e -> e.length() <= 3));
        //then
        System.out.println(map);
    }

    @Test
    void 인수2개_paritioningBy() {
        //given
        List<String> a = List.of("aaaaa", "bbbbb", "cc", "dd", "eee", "fff", "ggg");
        //when
        Map<Boolean, Long> map = a.stream()
                .collect(partitioningBy(e -> e.length() <= 3, counting()));
        //then
        System.out.println(map);
    }
}
