package data_structure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.assertj.core.data.MapEntry;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

public class MapPracticeTest {

    @Test
    void test_Map(){
        Map<Integer, Integer> map = new HashMap<>();

        map.put(1, 101);
        map.put(2, 100);
        map.put(3, 102);
        map.put(4, 103);

        map.compute(1, (k, v) -> v == 102 ? 1001 : -1);
        assertThat(map).containsEntry(1, -1);

        map.computeIfAbsent(5, k -> k+100);
        assertThat(map).containsEntry(5, 105);

        map.computeIfPresent(5, (k, v) -> v + 1);
        assertThat(map).containsEntry(5, 106);

        assertThat(map.containsKey(5)).isTrue();
        assertThat(map.containsKey(6)).isFalse();

        assertThat(map.containsValue(102)).isTrue(); // O(N)
        assertThat(map.containsValue(99)).isFalse();

        map.merge(5, 3, (k, v) -> v + 3);
        assertThat(map).containsEntry(5, 109);
    }

    @Test
    void test_TreeMap(){
        TreeMap<Integer, Integer> map = new TreeMap<>();

//        map.addAll()


    }
}
