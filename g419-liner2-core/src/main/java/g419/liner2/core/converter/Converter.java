package g419.liner2.core.converter;

import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

public abstract class Converter {

  public void apply(final Document doc) {
    start(doc);
    doc.getSentences().stream().forEach(this::apply);
    finish(doc);
  }

  protected abstract void apply(final Sentence sentence);


  protected void start(final Document doc) {
  }

  protected void finish(final Document doc) {
  }

}
