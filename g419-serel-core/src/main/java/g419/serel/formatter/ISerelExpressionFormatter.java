package g419.serel.formatter;

import g419.corpus.structure.Document;
import g419.serel.structure.SerelExpression;
import java.util.List;

public interface ISerelExpressionFormatter {
  public List<String> getHeader();

  public List<String> format(Document document, List<SerelExpression> serelExpressions);
}
