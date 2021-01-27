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

  public static <T>
  List<List<T>>
  getAllCombinations(final List<T> input, final int k) {

    final List<List<T>> subsets = new ArrayList<>();

    final int[] s = new int[k];                  // here we'll keep indices
    // pointing to elements in input array

    if (k <= input.size()) {
      // first index sequence: 0, 1, 2, ...
      for (int i = 0; (s[i] = i) < k - 1; i++) {
      }
      subsets.add(getSubset(input, s));
      for (; ; ) {
        int i;
        // find position of item that can be incremented
        for (i = k - 1; i >= 0 && s[i] == input.size() - k + i; i--) {
        }
        if (i < 0) {
          break;
        }
        s[i]++;                    // increment this item
        for (++i; i < k; i++) {    // fill up remaining items
          s[i] = s[i - 1] + 1;
        }
        subsets.add(getSubset(input, s));
      }
    }

    return subsets;
  }

  // generate actual subset by index sequence
  public static <T>
  List<T> getSubset(final List<T> input, final int[] subset) {
    final List<T> result = new LinkedList<>();
    for (int i = 0; i < subset.length; i++) {
      result.add(input.get(subset[i]));
    }
    return result;
  }


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
