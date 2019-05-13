package g419.liner2.core.tools;

import g419.corpus.structure.Annotation;
import g419.liner2.core.lib.LibLoaderPolem;
import g419.polem.CascadeLemmatizer;

import java.util.stream.Collectors;

public class PolemLemmatizer {

  final CascadeLemmatizer lemmatizer;

  public PolemLemmatizer() {
    LibLoaderPolem.load();
    lemmatizer = CascadeLemmatizer.assembleLemmatizer();
  }


  public String lemmatize(final String orths, final String bases, final String ctags) {
    return lemmatize(orths, bases, ctags, false);
  }

  public String lemmatize(final String orths, final String bases, final String ctags, final boolean debug) {
    return lemmatizer.lemmatizeS(orths, bases, ctags, debug);
  }

  public void lemmatize(final Annotation an) {
    final String orths = an.getTokenTokens().stream().map(t -> t.getOrth()).collect(Collectors.joining(" "));
    final String bases = an.getTokenTokens().stream().map(t -> t.getDisambTag().getBase()).collect(Collectors.joining(" "));
    final String ctags = an.getTokenTokens().stream().map(t -> t.getDisambTag().getCtag()).collect(Collectors.joining(" "));
    final String ns = an.getTokenTokens().stream().map(t -> t.getNoSpaceAfter() ? "False" : "True").collect(Collectors.joining(" "));
    final String category = an.getType();
    an.setLemma(lemmatizer.lemmatizeS(orths, bases, ctags, ns, category, false));
  }
}
