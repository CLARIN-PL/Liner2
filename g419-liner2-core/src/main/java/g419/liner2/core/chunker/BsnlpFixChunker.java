package g419.liner2.core.chunker;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.*;
import g419.liner2.core.tools.NeLemmatizer;

import java.util.*;

/**
 * Motody do korekcji typowych błędów popełnianych przez model statystyczny.
 *
 * @author Michał Marcińczuk
 */
public class BsnlpFixChunker extends Chunker {

  Map<String, String> renameRules = new HashMap<String, String>();

  public static final String BSNLP_PREFIX = "bsnlp2017";
  public static final String BSNLP_PER = "bsnlp2017_per";
  public static final String BSNLP_LOC = "bsnlp2017_loc";
  public static final String BSNLP_ORG = "bsnlp2017_org";
  public static final String BSNLP_MISC = "bsnlp2017_misc";

  private boolean cleanup = false;

  NeLemmatizer lemmatizer = null;

  /**
   * @param cleanup if true, then annotations other than bsnlp2017_* are removed from documents.
   */
  public BsnlpFixChunker(NeLemmatizer lemmatizer, boolean cleanup) {
    renameRules.put(KpwrNer.NER_ORG_NATION, BSNLP_PER);
    renameRules.put(KpwrNer.NER_PRO_MEDIA_RADIO, BSNLP_ORG);
    this.cleanup = cleanup;
    this.lemmatizer = lemmatizer;
  }

  /**
   *
   */
  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    for (Sentence sentence : ps.getSentences()) {
      this.processSentence(sentence);
      this.filterByConfidence(sentence, 0.4);
      this.joinOrgWithLoc(sentence);
    }

    this.renameByMaxConfidence(ps);
    this.renamePersonNames(ps);

    if (cleanup) {
      this.cleanup(ps);
    }

    this.lemmatize(ps);

    return ps.getChunkings();
  }

  private void cleanup(Document ps) {
    List<Annotation> toRemove = new LinkedList<Annotation>();
    for (Annotation an : ps.getAnnotations()) {
      if (!an.getType().startsWith(BSNLP_PREFIX)) {
        toRemove.add(an);
      }
    }
    ps.removeAnnotations(toRemove);
  }

  /**
   * For annotations with the same orth set the same category, which has the highest confidence among those annotations.
   *
   * @param ps
   */
  private void renameByMaxConfidence(Document ps) {
    Map<String, Annotation> maxConfidence = new HashMap<String, Annotation>();
    for (Annotation an : ps.getAnnotations()) {
      if (an.getType().startsWith("bsnlp2017_")) {
        if (maxConfidence.containsKey(an.getText())) {
          if (an.getConfidence() > maxConfidence.get(an.getText()).getConfidence()) {
            maxConfidence.put(an.getText(), an);
          }
        } else {
          maxConfidence.put(an.getText(), an);
        }
      }
    }
    for (Annotation an : ps.getAnnotations()) {
      if (an.getType().startsWith("bsnlp2017_")) {
        Annotation confidence = maxConfidence.get(an.getText());
        if (confidence != null && confidence.getConfidence() > an.getConfidence()) {
          an.setType(confidence.getType());
          an.setConfidence(confidence.getConfidence());
        }
      }
    }
  }

  /**
   * @param ps
   */
  private void renamePersonNames(Document ps) {
    Map<String, Double> personWithConfidence = new HashMap<String, Double>();
    for (Annotation an : ps.getAnnotations()) {
      if (BSNLP_PER.equals(an.getType()) && an.getTokens().size() > 1) {
        personWithConfidence.put(an.getText(), an.getConfidence());
        for (int index : an.getTokens()) {
          personWithConfidence.put(an.getSentence().getTokens().get(index).getOrth(), an.getConfidence());
        }
      }
    }

    for (Annotation an : ps.getAnnotations()) {
      if (!BSNLP_PER.equals(an.getType())) {
        Double confidence = personWithConfidence.get(an.getText());
        if (confidence != null && confidence > an.getConfidence()) {
          an.setType(BSNLP_PER);
          //an.setConfidence(confidence);
        }
      }
    }
  }

  private void processSentence(Sentence sentence) {
    Map<String, Annotation> bsnlp = new HashMap<String, Annotation>();
    for (Annotation an : sentence.getChunks()) {
      if (an.getType().startsWith("bsnlp2017_")) {
        String key = String.format("%d:%d", an.getBegin(), an.getEnd());
        bsnlp.put(key, an);
      }
    }

    for (Annotation an : sentence.getChunks()) {
      String renameTo = this.renameRules.get(an.getType());
      String key = String.format("%d:%d", an.getBegin(), an.getEnd());
      Annotation anBsnlp = bsnlp.get(key);
      if (renameTo != null && anBsnlp != null) {
        anBsnlp.setType(renameTo);
      }
    }
  }

  private void filterByConfidence(Sentence sentence, double minConfidence) {
    Set<Annotation> toRemove = new HashSet<Annotation>();
    for (Annotation an : sentence.getChunks()) {
      if (an.getConfidence() < minConfidence) {
        toRemove.add(an);
      }
    }
    sentence.getChunks().removeAll(toRemove);
  }

  private void joinOrgWithLoc(Sentence sentence) {
    Map<String, Annotation> bsnlp = new HashMap<String, Annotation>();
    for (Annotation an : sentence.getChunks()) {
      if (an.getType().startsWith("bsnlp2017_")) {
        String key = String.format("%d:%s", an.getBegin(), an.getType());
        bsnlp.put(key, an);
      }
    }

    List<Annotation> toRemove = new LinkedList<Annotation>();
    List<Annotation> newAnns = new LinkedList<Annotation>();
    for (Annotation an : sentence.getChunks()) {
      if (BSNLP_ORG.equals(an.getType()) && an.getEnd() + 2 < sentence.getTokenNumber()) {
        String key = String.format("%d:%s", an.getEnd() + 2, BSNLP_LOC);
        Annotation loc = bsnlp.get(key);
        if ("w".equals(sentence.getTokens().get(an.getEnd() + 1).getDisambTag().getBase())
            && loc != null) {
          newAnns.add(new Annotation(an.getBegin(), loc.getEnd(), BSNLP_ORG, sentence));
          toRemove.add(an);
          toRemove.add(loc);
        }
      }
    }

    sentence.getChunks().removeAll(toRemove);
    sentence.getChunks().addAll(newAnns);
  }

  /**
   * Lematyzacja nazw własnych.
   *
   * @param ps
   */
  private void lemmatize(Document ps) {
    for (Annotation an : ps.getAnnotations()) {
      this.lemmatize(an);
    }
  }

  /**
   * Bezkontekstowa lematyzacja pojedynczej nazwy.
   *
   * @param an
   */
  private void lemmatize(Annotation an) {
    if (this.lemmatizer != null) {
      String lemma = null;
      if (BSNLP_PER.equals(an.getType())) {
        lemma = this.lemmatizer.lemmatizePersonName(an);
        if (lemma != null) {
          an.setLemma(lemma);
          return;
        }
      }
      if (lemma == null) {
        lemma = this.lemmatizer.lemmatize(an);
        if (lemma != null) {
          an.setLemma(lemma);
          return;
        }
      }
    }

    if (an.getTokenTokens().size() == 1) {
      Token token = an.getSentence().getTokens().get(an.getBegin());
      for (Tag tag : token.getDisambTags()) {
        if (Character.isUpperCase(tag.getBase().charAt(0))) {
          // Jednoelementowa nazwa, której forma bazowa jest z dużej litery
          an.setLemma(tag.getBase());
          return;
        }
      }
    }

    an.setLemma(an.getText());
    an.setGroup("LEMMA_ORTH");
  }
}
