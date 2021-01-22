package g419.serel.tools


import spock.lang.Specification

import static g419.serel.tools.Permutations.getAllCombinations
import static g419.serel.tools.Permutations.getAllPermutations

class PermutationsTest extends Specification {

    def "permutations of {1} "() {
        when:
            final List<Integer> list = List.of(1)
            def result = getAllPermutations(list)
        then:
            result.size() == 1
            result.get(0).get(0) == 1
    }

    def "permutations of {1,2} "() {
        when:
            final List<Integer> list = List.of(1, 2)
            def result = getAllPermutations(list)
        then:
            result.size() == 2
            (result.get(0).get(0) == 1 && result.get(1).get(0) == 2) ||
                    (result.get(0).get(0) == 2 && result.get(1).get(0) == 1)
    }

    def "permutations of {1,2,3} "() {
        when:
            final List<Integer> list = List.of(1, 2, 3)
            def result = getAllPermutations(list)
        then:
            result.size() == 6
//            (result.get(0).get(0) == 1 && result.get(1).get(0) == 2) ||
//                    (result.get(0).get(0) == 2 && result.get(1).get(0) == 1)
    }

    def "permutations of {1,2,3, 4} "() {
        when:
            final List<Integer> list = List.of(1, 2, 3, 4)
            def result = getAllPermutations(list)
        then:
            result.size() == 24
//            (result.get(0).get(0) == 1 && result.get(1).get(0) == 2) ||
//                    (result.get(0).get(0) == 2 && result.get(1).get(0) == 1)
    }

    def "permutations of {1,2,3,4,5,6} "() {
        when:
            final List<Integer> list = List.of(1, 2, 3, 4, 5, 6)
            def result = getAllPermutations(list)
        then:
            result.size() == 6 * 5 * 4 * 3 * 2;
    }


    def "combinations of {1,2,3} - 2 elements in set"() {
        when:
            final List<Integer> list = List.of(1, 2, 3)
            def result = getAllCombinations(list, 2)
            //result.stream().forEach { r -> System.out.println(r) }
        then:
            result.size() == 3
    }

    def "combinations of {3,4,5,6} - 1 elements in set"() {
        when:
            final List<Integer> list = List.of(3, 4, 5, 6)
            def result = getAllCombinations(list, 1)
            result.stream().forEach { r -> System.out.println(r) }
        then:
            result.size() == 4
    }

    def "combinations of {1,2,3,4,5,6,7,8} - 3 elements in set"() {
        when:
            final List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8)
            def result = getAllCombinations(list, 3)
            result.stream().forEach { r -> System.out.println(r) }
        then:
            result.size() == 56
    }


}
