package g419.liner2.core.filter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.List;


public abstract class Filter {

  // List of annotation types to which the filter can be applied
  protected ArrayList<String> appliesTo = new ArrayList<>();

  /**
   * Check if chunk passes a filter condition.
   *
   * @param chunk
   * @param charSeq
   * @return
   */
  protected abstract Annotation pass(Annotation chunk, CharSequence charSeq);

  public abstract String getDescription();

  public Annotation run(final Annotation chunk, final CharSequence charSeq) {
    if (appliesTo.contains(chunk.getType())) {
      return pass(chunk, charSeq);
    } else {
      return chunk;
    }
  }

  /**
   * Pass chunk through set of filters.
   *
   * @param chunk
   * @param filters
   * @return
   */
  static public Annotation filter(final Annotation chunk, final ArrayList<Filter> filters) {
    final StringBuilder sb = new StringBuilder();
    final List<Token> tokens = chunk.getSentence().getTokens();
    for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++) {
      final Token token = tokens.get(i);
      sb.append(token.getOrth() + (token.getNoSpaceAfter() ? "" : " "));
    }
    return Filter.filter(chunk, sb.toString().trim(), filters);
  }

  static private Annotation filter(final Annotation chunk, final CharSequence cSeq, final ArrayList<Filter> filters) {
    Annotation chunkMod = chunk;
    for (final Filter filter : filters) {
      final Annotation chunkFiltered = filter.run(chunkMod, cSeq);

      if (chunkFiltered == null) {
        return null;
      }

      chunkMod = chunkFiltered;
    }
    return chunkMod;
  }
}
