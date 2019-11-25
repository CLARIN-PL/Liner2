package g419.liner2.core.tools;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A thread-safe element frequency counter.
 *
 * @param <T> Counted object class.
 * @author Michał Marcińczuk
 */
public class FrequencyCounter<T> {

  private final Map<T, Integer> frequency = new HashMap<>();

  public synchronized void add(final T object) {
    Integer c = frequency.get(object);
    if (c == null) {
      c = 0;
    }
    c += 1;
    frequency.put(object, c);
  }

  public void addAll(final Collection<T> objects) {
    for (final T o : objects) {
      add(o);
    }
  }

  public List<java.util.Map.Entry<T, Integer>> getSorted() {
    return frequency.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .collect(Collectors.toList());
  }

  /**
   * Return a set of most frequent elements.
   *
   * @return
   */
  public Set<T> getMostFrequent() {
    final Set<T> itemsWithMaxFrequency = new HashSet<>();
    if (frequency.size() == 0) {
      return itemsWithMaxFrequency;
    } else {
      final int max = Collections.max(frequency.values());
      for (final T o : frequency.keySet()) {
        if (frequency.get(o) == max) {
          itemsWithMaxFrequency.add(o);
        }
      }
      return itemsWithMaxFrequency;
    }
  }

}
