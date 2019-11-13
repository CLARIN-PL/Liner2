package g419.tools.utils;

/**
 * A thread-safe counter.
 *
 * @author czuk
 */
public class Counter {

  private Integer counter = 0;

  public synchronized void increment() {
    this.counter++;
  }

  public Integer getValue() {
    return this.counter;
  }

}
