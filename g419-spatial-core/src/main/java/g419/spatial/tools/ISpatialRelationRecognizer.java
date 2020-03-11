package g419.spatial.tools;

import com.google.common.collect.Lists;
import g419.corpus.HasLogger;
import g419.corpus.structure.*;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.structure.SpatialExpression;
import io.vavr.control.Option;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.maltparser.core.exception.MaltChainedException;

public abstract class ISpatialRelationRecognizer implements HasLogger {

  Option<MaltParser> maltParser = Option.none();

  protected List<IRelationFilter> filters = Lists.newArrayList();

  protected final RelationFilterSemanticPattern semanticFilter = new RelationFilterSemanticPattern();

  protected ISpatialRelationRecognizer() throws IOException {
  }

  public ISpatialRelationRecognizer withMaltParser(final MaltParser maltParser) {
    this.maltParser = Option.of(maltParser);
    return this;
  }

  public abstract List<SpatialExpression> findCandidates(final Sentence sentence);

  /**
   * @return
   */
  public List<IRelationFilter> getFilters() {
    return filters;
  }

  /**
   * @return
   */
  public RelationFilterSemanticPattern getSemanticFilter() {
    return semanticFilter;
  }

  /**
   * Passes the spatial expression through the list of filters and return the first filter, for which
   * the expressions was discarded.
   *
   * @param se Spatial expression to test
   * @return
   */
  public Optional<String> getFilterDiscardingRelation(final SpatialExpression se) {
    final Iterator<IRelationFilter> filters = getFilters().iterator();
    while (filters.hasNext()) {
      final IRelationFilter filter = filters.next();
      if (!filter.pass(se)) {
        return Optional.ofNullable(filter.getClass().getSimpleName());
      }
    }
    return Optional.ofNullable(null);
  }


  public void recognizeInPlace(final Document document) {
    try {
      for (final Paragraph paragraph : document.getParagraphs()) {
        for (final Sentence sentence : paragraph.getSentences()) {
          for (final SpatialExpression rel : recognize(sentence)) {
            final Frame<Annotation> f = SpatialExpressionToFrame.convert(rel);
            document.getFrames().add(f);
          }
        }
      }
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public abstract List<SpatialExpression> recognize(final Sentence sentence) throws MaltChainedException;
}
