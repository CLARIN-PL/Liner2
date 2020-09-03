package g419.serel.formatter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Document;
import g419.serel.structure.SerelExpression;
import java.util.List;
import java.util.StringJoiner;

public class SerelExpressionFormatterTsv implements ISerelExpressionFormatter {

  @Override
  public List<String> getHeader() {
    final StringJoiner joiner = new StringJoiner("\t");
    joiner.add("Type");
    joiner.add("Source_text");
    joiner.add("Target_text");
    joiner.add("Path");
    joiner.add("Sentence");
    joiner.add("Filename");
    return Lists.newArrayList(joiner.toString());
  }

  @Override
  public List<String> format(final Document document, final List<SerelExpression> serelExpressions) {
    final List<String> lines = Lists.newArrayList();

    for (final SerelExpression se : serelExpressions) {
      final StringJoiner fields = new StringJoiner("\t");
      fields.add(se.getRelation().getType());
      fields.add(se.getRelation().getAnnotationFrom().getText());
      fields.add(se.getRelation().getAnnotationTo().getText());
      fields.add(se.getPathAsString());
      fields.add(se.getSentence().toString());
      fields.add(document.getName());
      lines.add(fields.toString());
    }
    return lines;
  }
}
