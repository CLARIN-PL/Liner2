package g419.liner2.core.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

import java.util.List;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureSubstModifierBefore extends AnnotationAtomicFeature {

  /**
   *
   */
  public AnnotationFeatureSubstModifierBefore() {
  }

  @Override
  public String generate(final Annotation an) {
    String value = null;

    final List<Token> tokens = an.getSentence().getTokens();

    if ((an.getHeadToken().getDisambTag().getCase().equals("nom")
        || an.getHeadToken().getDisambTag().getPos().equals("adj"))
        && an.getBegin() > 0
        && tokens.get(an.getBegin() - 1).getDisambTag().getPos().equals("subst")) {
      value = tokens.get(an.getBegin() - 1).getDisambTag().getBase();
    }

    return value;
  }

}
