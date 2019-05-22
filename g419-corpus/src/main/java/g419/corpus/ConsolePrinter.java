package g419.corpus;

/**
 * Contains a set of methods for printing text to the console.
 * It wraps all direct calls of System.out.println in case
 * a redirect to different stream will be required.
 *
 * @author Michał Marcińczuk
 */
public class ConsolePrinter {

  public static boolean verbose = false;
  public static boolean verboseDetails = false;

  /**
   * Messages are print to std.out only with -verbose parameter.
   *
   * @param text
   */
  public static void log(String text) {
    ConsolePrinter.log(text, false);
  }

  /**
   * Messages are print to std.out only with -verbose or -verboseDetails parameter.
   *
   * @param text
   * @param details
   */
  public static void log(String text, boolean details) {
    if (verboseDetails || (!details && verbose)) {
      System.out.println(text);
    }
  }

  /**
   * Prints line on System.out
   *
   * @param line
   */
  public static void println(String line) {
    System.out.println(line);
  }
}
