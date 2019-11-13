package g419.liner2.core.features.tokens;


import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.regex.Pattern;

public class PatternFeature extends TokenFeature {

  private Pattern ALL_UPPER = Pattern.compile("^\\p{Lu}+$");
  private Pattern ALL_LOWER = Pattern.compile("^\\p{Ll}+$");
  private Pattern DIGITS = Pattern.compile("^\\p{N}+$");
  private Pattern SYMBOLS = Pattern.compile("^[\\p{Punct}\\p{P}\\p{S}\\p{Space}\\p{C}\\p{M}]+$");
  private Pattern UPPER_INIT = Pattern.compile("^\\p{Lu}\\p{Ll}+$");
  private Pattern UPPER_CAMEL_CASE = Pattern.compile("^\\p{Lu}+\\p{Ll}+\\p{Lu}*\\p{Ll}*$");
  private Pattern LOWER_CAMEL_CASE = Pattern.compile("^\\p{Ll}+\\p{Lu}\\p{Ll}*$");


  public PatternFeature(String name) {
    super(name);
  }

  public String generate(Token t, TokenAttributeIndex index) {
    String orth = t.getAttributeValue(index.getIndex("orth"));
    if (ALL_UPPER.matcher(orth).find()) {
      return "ALL_UPPER";
    } else if (ALL_LOWER.matcher(orth).find()) {
      return "ALL_LOWER";
    } else if (DIGITS.matcher(orth).find()) {
      return "DIGITS";
    } else if (SYMBOLS.matcher(orth).find()) {
      return "SYMBOLS";
    } else if (UPPER_INIT.matcher(orth).find()) {
      return "UPPER_INIT";
    } else if (UPPER_CAMEL_CASE.matcher(orth).find()) {
      return "UPPER_CAMEL_CASE";
    } else if (LOWER_CAMEL_CASE.matcher(orth).find()) {
      return "LOWER_CAMEL_CASE";
    } else {
      return "MIXED";
    }

  }


}
