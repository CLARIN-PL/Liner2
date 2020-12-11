package g419.serel.tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Permutations {

  public static <T>
  List<List<T>>
  getAllPermutations(final List<T> elements) {

    final ArrayList<T> list = new ArrayList<>(elements);
    final List<List<T>> result = new LinkedList<>();

    return getAllPermutations(list.size(), list, result);
  }

  public static <T>
  List<List<T>>
  getAllPermutations(
      final int n,
      final ArrayList<T> elements,
      final List<List<T>> result
  ) {
    if (n == 1) {
      add2result(elements, result);
    } else {
      for (int i = 0; i < n - 1; i++) {
        getAllPermutations(n - 1, elements, result);
        if (n % 2 == 0) {
          swap(elements, i, n - 1);
        } else {
          swap(elements, 0, n - 1);
        }
      }
      getAllPermutations(n - 1, elements, result);
    }

    return result;
  }

  private static <T> void add2result(final ArrayList<T> input, final List<List<T>> result) {
    result.add((List<T>) input.clone());
  }

  private static <T> void swap(final List<T> input, final int a, final int b) {
    final T tmp = input.get(a);
    input.set(a, input.get(b));
    input.set(b, tmp);
  }

/*
  public static void main(final String[] args) {
    final Integer[] ar = new Integer[]{1, 2, 3, 4, 5, 6};
    final List<Integer> list = Arrays.asList(ar);
    getAllPermutations(ar.length, list);
  }

  private static <T> void printArray(final List<T> input, final int delimiter) {
    System.out.print('\n');
    for (int i = 0; i < input.size(); i++) {
      System.out.print(input.get(i));
    }
  }
*/


}
