package g419.liner2.core.converter.factory;

import g419.lib.cli.ParameterException;
import g419.liner2.core.converter.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class ConverterFactoryItem {

  protected Pattern pattern;
  protected Matcher matcher;

  public ConverterFactoryItem(final String stringPattern) {
    pattern = Pattern.compile("^" + stringPattern + "$");

  }

  public boolean matchPattern(final String description) {
    matcher = pattern.matcher(description);
    return matcher.find();
  }

  abstract public Converter getConverter() throws ParameterException;
}
