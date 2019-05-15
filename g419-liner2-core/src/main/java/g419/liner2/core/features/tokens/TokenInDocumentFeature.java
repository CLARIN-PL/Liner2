package g419.liner2.core.features.tokens;

import g419.corpus.structure.Document;

public abstract class TokenInDocumentFeature extends Feature {
  public TokenInDocumentFeature(final String name) {super(name); }

  public abstract void generate(Document document);
}
