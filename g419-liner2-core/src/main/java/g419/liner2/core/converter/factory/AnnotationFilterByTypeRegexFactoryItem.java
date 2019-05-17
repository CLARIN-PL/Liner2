package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationFilterByTypeRegexConverter;
import g419.liner2.core.converter.Converter;

import java.util.regex.Pattern;

public class AnnotationFilterByTypeRegexFactoryItem extends ConverterFactoryItem {

  public AnnotationFilterByTypeRegexFactoryItem() {
    super("annotation-filter-by-type-regex:(.*)");
  }

  @Override
  public Converter getConverter() {
    return new AnnotationFilterByTypeRegexConverter(Pattern.compile("^" + matcher.group(1) + "$"));
  }
}
