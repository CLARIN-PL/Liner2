package g419.serel.formatter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Document;
import g419.serel.structure.SerelExpression;
import java.util.List;
import java.util.StringJoiner;

public class SerelExpressionFormatterPlain implements ISerelExpressionFormatter {

  @Override
  public List<String> getHeader() {
    final StringJoiner joiner = new StringJoiner("\t");
    joiner.add("Path");
    return Lists.newArrayList(joiner.toString());
  }

  @Override
  public List<String> format(final Document document, final List<SerelExpression> serelExpressions) {
    final List<String> lines = Lists.newArrayList();
    for (final SerelExpression se : serelExpressions) {
      lines.add(se.getPathAsString());
    }
    return lines;
  }

}
