package data_structure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

public class SetPracticeTest {

    @Test
    void test_Set() {
        Set<Integer> set = new HashSet<>();

        set.add(1);
        set.addAll(List.of(2, 3, 4));

        System.out.println(set);
        assertThat(set).hasSize(4);

        assertThat(set.contains(1)).isTrue();
        assertThat(set.contains(5)).isFalse();

        assertThat(set.containsAll(List.of(1, 2, 3))).isTrue();
        assertThat(set.containsAll(List.of(3, 4, 5))).isFalse();

        set.remove(1);
        assertThat(set).contains(2, 3, 4);

        set.removeAll(List.of(1, 3));
        assertThat(set).contains(2, 4);

        set.addAll(List.of(1, 2, 3, 4));
        set.retainAll(List.of(1, 2));
        assertThat(set).contains(1, 2);

        set.clear();
        System.out.println(set);
        assertThat(set).isEmpty();
    }

    @Test
    void test_HashSet() {
        //Set과 거의 같으므로 생략
    }

    @Test
    void test_LinkedHashSet() {
        /*
        예측 가능한 반복 순서를 가진 Set 인터페이스의 해시 테이블 및 링크된 목록 구현입니다.
        이 구현은 모든 항목을 통해 이중으로 연결된 목록을 유지한다는 점에서 해시셋과 다릅니다.
        이 연결된 목록은 집합에 요소가 삽입된 순서(삽입 순서)인 반복 순서를 정의합니다.
        요소가 세트에 다시 삽입되는 경우 삽입 순서는 영향을 받지 않습니다.
        (An element e is reinserted into a set s
        if s.add(e) is invoked when s.contains(e) would return true immediately prior to the invocation.)
         */

        // 이미 존재하는 원소를 다시 추가하더라도 순서는 변경되지 않음
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        set.addAll(List.of(1, 2, 3));

        Iterator<Integer> iterator = set.iterator();

        StringBuilder sb1 = new StringBuilder();
        while(iterator.hasNext()){
            sb1.append(iterator.next());
        }

        set.add(1);
        iterator = set.iterator();
        StringBuilder sb2 = new StringBuilder();
        while(iterator.hasNext()){
            sb2.append(iterator.next());
        }

        assertThat(sb2.equals(sb1));
    }


    @Test
    void test_TreeSet(){
        /*
        기본 연산(추가, 제거, 포함)에 대해 보장된 로그(n) 시간 비용
         */
        TreeSet<Integer> set = new TreeSet<>();

        set.addAll(List.of(1, 2, 3, 4, 5, 7));

        assertThat(set.first()).isEqualTo(1);
        assertThat(set.last()).isEqualTo(7);
        assertThat(set.ceiling(6)).isEqualTo(7); //크거나 같은 원소 반환
        assertThat(set.floor(6)).isEqualTo(5); //작거나 같은 원소 반환
        assertThat(set.higher(5)).isEqualTo(7); //큰 원소 반환
        assertThat(set.lower(7)).isEqualTo(5); //작은 원소 반환

        assertThat(set.pollFirst()).isEqualTo(1);
        assertThat(set).containsExactly(2, 3, 4, 5, 7);

        assertThat(set.pollLast()).isEqualTo(7);
        assertThat(set).containsExactly(2, 3, 4, 5);

        set.addAll(List.of(1, 2, 3, 4, 5, 7));
        assertThat(set.descendingSet()).containsExactly(7, 5, 4, 3, 2, 1);

        assertThat(set.headSet(4)).containsExactly(1, 2, 3);    //작은 집합 반환
        assertThat(set.tailSet(4)).containsExactly(4, 5, 7); //크거나 같은 집합 반환
    }

}
