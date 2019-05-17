package g419.liner2.core.normalizer.global_rules;

public interface Rule {
  boolean matches(String lval, String base);

  /**
   * Should return null if matches, but cannot normalize.
   * Should assume that matches - if not, it can throw anything it want.
   *
   * @param lval
   * @param base
   * @param previous
   * @param first
   * @param creationDate
   * @return
   */
  String normalize(String lval, String base, String previous, String first, String creationDate);
}
