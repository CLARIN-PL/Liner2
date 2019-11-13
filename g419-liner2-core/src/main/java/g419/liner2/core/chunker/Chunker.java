package g419.liner2.core.chunker;

import java.util.Map;

import g419.corpus.structure.*;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.ini4j.Ini;

public abstract class Chunker {

  Ini.Section description;
  TokenFeatureGenerator generator = null;

  /**
   * Recognize annotations in the document.
   * The recognized annotations should not be added to the sentences but returned in the map.
   *
   * @param ps
   * @return
   */
  abstract public Map<Sentence, AnnotationSet> chunk(Document ps);

  public void chunkInPlace(final Document ps) {
    final Map<Sentence, AnnotationSet> chunking = chunk(ps);
    for (final Paragraph paragraph : ps.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        if (chunking.containsKey(sentence)) {
          sentence.addAnnotations(chunking.get(sentence));
        }
      }
    }
  }

  public void setDescription(final Ini.Section description) {
    this.description = description;
  }

  public org.ini4j.Profile.Section getDescription() {
    return description;
  }

  public void setFeatureGenerator(TokenFeatureGenerator generator){
    this.generator = generator;
  }

  public TokenFeatureGenerator getFeatureGenerator(){
    return this.generator;
  }

  /**
   * Zwolnienie zasobów wykorzystywanych przez chunker,
   * np. zamknięcie zewnętrznych procesów i połączeń.
   * Jeżeli jest to wymagane, to klasa dziedzicząca powinna przeciążyć
   * tą metodę.
   */
  public void close() {

  }

  /**
   * Przygotowanie do klasyfikacji danego tekstu. Tą metodę przeciążają klasyfikatory,
   * które wymagają podania całego tekstu przed rozpoczęciem pracy, np. dwuprzebiegowe.
   */
  public void prepare(final Document ps) {
  }
}
