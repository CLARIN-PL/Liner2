package g419.liner2.core.tools;

import g419.corpus.ConsolePrinter;
import g419.corpus.structure.*;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class ChunkerEvaluator {

  /* typ chunku => lista chunków danego typu */
  private final HashMap<String, ArrayList<Annotation>> chunksTruePositives = new HashMap<>();
  private final HashMap<String, ArrayList<Annotation>> chunksFalsePositives = new HashMap<>();
  private final HashMap<String, ArrayList<Annotation>> chunksFalseNegatives = new HashMap<>();

  //<<< grupowanie bez uwzglądnienia kanałów anotacji
  private int globalTruePositivesRangeOnly = 0;
  private int globalFalsePositivesRangeOnly = 0;
  private int globalFalseNegativesRangeOnly = 0;
  // >>>

  //<<< grupowanie anotacji o granicach wystepujacych juz w korpusie
  private final HashMap<String, Float> precisionExistingRangeOnly = new HashMap<>();
  private final HashMap<String, Float> fMeasureExistingRangeOnly = new HashMap<>();
  private final HashMap<String, Integer> falsePositivesExistingRangeOnly = new HashMap<>();
  //>>>

  private int sentenceNum = 0;
  private String currentDocId = "";

  /* Określa, czy szczegóły oceny mają by ukryte */
  private boolean quiet = false;

  /* Określa, czy mają być wypisywane wyłącznie zdania z błędami (FP i/lub FN). */
  private boolean errorsOnly = false;

  private boolean checkLemma = false;

  private List<Pattern> patterns = new ArrayList<>();

  private final Set<String> types = new HashSet<>();

  public ChunkerEvaluator(final List<Pattern> types) {
    patterns = types;
  }

  public ChunkerEvaluator(final List<Pattern> types, final boolean quiet) {
    patterns = types;
    this.quiet = quiet;
  }

  /**
   * @param types      Lista typów anotacji do oceny
   * @param quiet      Jeżeli true, to logi z oceny nie zostają drukowane.
   * @param errorsOnly Jeżeli true, to wypisuje tylko zdania z błędami.
   */
  public ChunkerEvaluator(final List<Pattern> types, final boolean quiet, final boolean errorsOnly) {
    patterns = types;
    this.quiet = quiet;
    this.errorsOnly = errorsOnly;
  }

  public void setCheckLemma(final boolean checkLemma) {
    this.checkLemma = checkLemma;
  }

  /**
   * Ocenia nerowanie całego dokumentu.
   */
  public void evaluate(final Document document, final Map<Sentence, AnnotationSet> chunkings, final Map<Sentence, AnnotationSet> chunkigsRef) {
    currentDocId = document.getName();
    for (final Sentence sentence : document.getSentences()) {
      evaluate(sentence, chunkings.get(sentence), chunkigsRef.get(sentence));
    }
  }

  /**
   *
   */
  public void evaluate(final Sentence sentence, final AnnotationSet chunking, final AnnotationSet chunkingRef) {

    // tylko na potrzeby wyświetlania szczegółów
    final Set<Annotation> myTruePositives = new HashSet<>();
    sentenceNum++;
    // Wybierz anotacje do oceny jeżeli został określony ich typ
    Set<Annotation> chunkingRefSet = new HashSet<>();
    Set<Annotation> chunkingSet = new HashSet<>();

    final Set<String> newTypes = chunkingRef.getAnnotationTypes();
    newTypes.addAll(chunking.getAnnotationTypes());
    newTypes.removeAll(types);
    updateTypes(newTypes);

    if (types.size() == 0) {
      chunkingRefSet = chunkingRef.chunkSet();
      chunkingSet = chunking.chunkSet();
    } else {
      for (final Annotation ann : chunkingRef.chunkSet()) {
        if (types.contains(ann.getType())) {
          chunkingRefSet.add(ann);
        }
      }
      for (final Annotation ann : chunking.chunkSet()) {
        if (types.contains(ann.getType())) {
          chunkingSet.add(ann);
        }
      }
    }

    // każdy HashSet w dwóch kopiach - jedna do iterowania, druga do modyfikacji
    final HashSet<Annotation> trueChunkSet = new HashSet<>(chunkingRefSet);
    final HashSet<Annotation> trueChunkSetIter = new HashSet<>(trueChunkSet);

    final HashSet<Annotation> testedChunkSet = new HashSet<>(chunkingSet);
    final HashSet<Annotation> testedChunkSetIter = new HashSet<>(testedChunkSet);

    // usuń z danych wszystkie poprawne chunki
    for (final Annotation trueChunk : trueChunkSetIter) {
      for (final Annotation testedChunk : testedChunkSetIter) {
        if (trueChunk.equals(testedChunk) &&
            (!checkLemma || trueChunk.getLemmaOrText().equalsIgnoreCase(testedChunk.getLemmaOrText()))) {
          // wpisz klucz do tablicy, jeśli jeszcze nie ma
          if (!chunksTruePositives.containsKey(testedChunk.getType())) {
            chunksTruePositives.put(testedChunk.getType(), new ArrayList<>());
            //this.keys.add(testedChunk.getType());
          }
          // dodaj do istniejącego klucza
          chunksTruePositives.get(testedChunk.getType()).add(testedChunk);
          globalTruePositivesRangeOnly += 1;
          // oznacz jako TruePositive
          myTruePositives.add(testedChunk);
          trueChunkSet.remove(trueChunk);
          testedChunkSet.remove(testedChunk);
        }
      }
    }

    // w testedChunkSet zostały falsePositives
    for (final Annotation testedChunk : testedChunkSet) {
      // wpisz klucz do tablicy, jeśli jeszcze nie ma
      if (!chunksFalsePositives.containsKey(testedChunk.getType())) {
        chunksFalsePositives.put(testedChunk.getType(), new ArrayList<>());
        //this.keys.add(testedChunk.getType());
      }
      // dodaj do istniejącego klucza
      chunksFalsePositives.get(testedChunk.getType()).add(testedChunk);
      Boolean truePositiveSkippedChannelCheck = false;
      for (final Annotation trueChunk : trueChunkSet) {
        if (testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()) &&
            (!checkLemma || trueChunk.getLemmaOrText().equalsIgnoreCase(testedChunk.getLemmaOrText()))) {
          globalTruePositivesRangeOnly += 1;
          truePositiveSkippedChannelCheck = true;
          break;
        }
      }
      if (!truePositiveSkippedChannelCheck) {
        globalFalsePositivesRangeOnly += 1;
      }
    }

    // w trueChunkSet zostały falseNegatives
    for (final Annotation trueChunk : trueChunkSet) {
      // wpisz klucz do tablicy, jeśli jeszcze nie ma
      if (!chunksFalseNegatives.containsKey(trueChunk.getType())) {
        chunksFalseNegatives.put(trueChunk.getType(), new ArrayList<>());
        //this.keys.add(trueChunk.getType());
      }
      // dodaj do istniejącego klucza
      chunksFalseNegatives.get(trueChunk.getType()).add(trueChunk);

      Boolean truePositiveSkippedChannelCheck = false;
      for (final Annotation testedChunk : testedChunkSet) {
        if (testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()) &&
            (!checkLemma || trueChunk.getLemmaOrText().equalsIgnoreCase(testedChunk.getLemmaOrText()))) {
          truePositiveSkippedChannelCheck = true;
          break;
        }
      }
      if (!truePositiveSkippedChannelCheck) {
        globalFalseNegativesRangeOnly += 1;
      }
    }

    // zlicznie falsePositives dla anotacji o granicah wystepujacych we wzorcowym korpusie
    for (final Annotation testedChunk : testedChunkSet) {
      for (final Annotation trueChunk : trueChunkSetIter) {
        if (testedChunk.getTokens().equals(trueChunk.getTokens())) {
          // wpisz klucz do tablicy, jeśli jeszcze nie ma
          if (!falsePositivesExistingRangeOnly.containsKey(testedChunk.getType())) {
            falsePositivesExistingRangeOnly.put(testedChunk.getType(), 0);
          }
          // dodaj do istniejącego klucza
          falsePositivesExistingRangeOnly.put(testedChunk.getType(),
              falsePositivesExistingRangeOnly.get(testedChunk.getType()) + 1);
          break;
        }
      }
    }

    if (!quiet && (!errorsOnly || testedChunkSet.size() > 0 || trueChunkSet.size() > 0)) {
      printSentenceResults(sentence, sentence.getId(), myTruePositives, testedChunkSet, trueChunkSet);
    }

  }

  /**
   * @param newTypes
   */
  private void updateTypes(final Set<String> newTypes) {
    for (final String newType : newTypes) {
      if (!types.contains(newType)) {
        if (patterns != null && !patterns.isEmpty()) {
          for (final Pattern patt : patterns) {
            if (patt.matcher(newType).find()) {
              chunksTruePositives.put(newType, new ArrayList<>());
              chunksFalsePositives.put(newType, new ArrayList<>());
              chunksFalseNegatives.put(newType, new ArrayList<>());
              precisionExistingRangeOnly.put(newType, 0.0f);
              fMeasureExistingRangeOnly.put(newType, 0.0f);
              types.add(newType);
              break;
            }

          }
        } else {
          chunksTruePositives.put(newType, new ArrayList<>());
          chunksFalsePositives.put(newType, new ArrayList<>());
          chunksFalseNegatives.put(newType, new ArrayList<>());
          precisionExistingRangeOnly.put(newType, 0.0f);
          fMeasureExistingRangeOnly.put(newType, 0.0f);
          types.add(newType);
        }
      }
    }
  }

  /**
   * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
   *
   * @return
   */
  public float getPrecision() {
    final float tp = getTruePositive();
    final float fp = getFalsePositive();
    return (tp + fp) == 0 ? 0 : tp / (tp + fp);
  }

  /**
   * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
   *
   * @param type
   * @return
   */
  public float getPrecision(final String type) {
    final float tp = getTruePositive(type);
    final float fp = getFalsePositive(type);
    return (tp + fp) == 0 ? 0 : tp / (tp + fp);
  }

  /**
   * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
   *
   * @return
   */
  public float getSpanPrecision() {
    final float tp = globalTruePositivesRangeOnly;
    final float fp = globalFalsePositivesRangeOnly;
    return (tp + fp) == 0 ? 0 : tp / (tp + fp);
  }

  public boolean getQuiet() {
    return quiet;
  }

  /**
   * Kompletność dla wszystkich typów anotacji.
   */
  public float getRecall() {
    final float tp = getTruePositive();
    final float fn = getFalseNegative();
    return (tp + fn) == 0 ? 0 : tp / (tp + fn);
  }

  /**
   * Kompletność dla wskazanego typu anotacji.
   *
   * @param type
   */
  public float getRecall(final String type) {
    final float tp = getTruePositive(type);
    final float fn = getFalseNegatives(type);
    return (tp + fn) == 0 ? 0 : tp / (tp + fn);
  }

  /**
   * Kompletność dla rozpoznawania granic anotacji.
   */
  public float getSpanRecall() {
    final float tp = globalTruePositivesRangeOnly;
    final float fn = globalFalseNegativesRangeOnly;
    return (tp + fn) == 0 ? 0 : tp / (tp + fn);
  }

  /**
   * Średnia harmoniczna dla wszystkich typów anotacji.
   *
   * @return
   */
  public float getFMeasure() {
    final float p = getPrecision();
    final float r = getRecall();
    return (p + r) == 0 ? 0 : (2 * p * r) / (p + r);
  }

  /**
   * Średnia harmoniczna dla wskazanego typu anotacji.
   *
   * @param type
   * @return
   */
  public float getFMeasure(final String type) {
    final float p = getPrecision(type);
    final float r = getRecall(type);
    return (p + r) == 0 ? 0 : (2 * p * r) / (p + r);
  }

  /**
   * Średnia harmoniczna dla rozpoznawania granic anotacji.
   *
   * @return
   */
  public float getSpanFMeasure() {
    final float p = getSpanPrecision();
    final float r = getSpanRecall();
    return (p + r) == 0 ? 0 : (2 * p * r) / (p + r);
  }

  public int getTruePositive() {
    int tp = 0;
    for (final String type : chunksTruePositives.keySet()) {
      tp += getTruePositive(type);
    }
    return tp;
  }

  public int getTruePositive(final String type) {
    return chunksTruePositives.containsKey(type) ? chunksTruePositives.get(type).size() : 0;
  }

  public int getFalsePositive() {
    int fp = 0;
    for (final String type : chunksFalsePositives.keySet()) {
      fp += getFalsePositive(type);
    }
    return fp;
  }

  public int getFalsePositive(final String type) {
    return chunksFalsePositives.containsKey(type) ? chunksFalsePositives.get(type).size() : 0;
  }

  public int getFalseNegative() {
    int fn = 0;
    for (final String type : chunksFalseNegatives.keySet()) {
      fn += getFalseNegatives(type);
    }
    return fn;
  }

  public int getFalseNegatives(final String type) {
    return chunksFalseNegatives.containsKey(type) ? chunksFalseNegatives.get(type).size() : 0;
  }

  public void setQuiet(final boolean quiet) {
    this.quiet = quiet;
  }

  /**
   * Drukuje wynik w formacie:
   * <p>
   * Annotation        &amp;   TP &amp;   FP &amp;   FN &amp; Precision &amp;   Recall &amp;  F$_1$ \\
   * \hline
   * ROAD_NAM          &amp;  147 &amp;    8 &amp;   36 &amp;   94.84\% &amp;  80.33\% &amp;  86.98\%
   * PERSON_LAST_NAM   &amp;  306 &amp;    9 &amp;   57 &amp;   97.14\% &amp;  84.30\% &amp;  90.27\%
   * PERSON_FIRST_NAM  &amp;  319 &amp;    3 &amp;   29 &amp;   99.07\% &amp;  91.67\% &amp;  95.22\%
   * COUNTRY_NAM       &amp;  160 &amp;   51 &amp;   36 &amp;   75.83\% &amp;  81.63\% &amp;  78.62\%
   * CITY_NAM          &amp;  841 &amp;   65 &amp;   75 &amp;   92.83\% &amp;  91.81\% &amp;  92.32\%
   * \hline
   * *TOTAL*           &amp; 1773 &amp;  136 &amp;  233 &amp;   92.88\% &amp;  88.38\% &amp;  90.67\%
   */
  public void printResults() {
    final String header = "        Annotation                     &      TP &      FP &      FN & Precision & Recall  & F$_1$   \\\\";
    final String line = "        %-30s & %7d & %7d & %7d &   %6.2f\\%% & %6.2f\\%% & %6.2f\\%% \\\\";

    printHeader("Exact match evaluation -- annotation span and types evaluation");
    ConsolePrinter.println(header);
    ConsolePrinter.println("\\hline");
    final ArrayList<String> keys = new ArrayList<>();
    keys.addAll(types);
    Collections.sort(keys);
    for (final String key : keys) {
      ConsolePrinter.println(
          String.format(line, key,
              getTruePositive(key), getFalsePositive(key), getFalseNegatives(key),
              getPrecision(key) * 100, getRecall(key) * 100, getFMeasure(key) * 100));
    }
    ConsolePrinter.println("\\hline");
    ConsolePrinter.println(String.format(line, "*TOTAL*",
        getTruePositive(), getFalsePositive(), getFalseNegative(),
        getPrecision() * 100, getRecall() * 100, getFMeasure() * 100));
    ConsolePrinter.println("\n");


    printHeader("Annotation span evaluation (annotation types are ignored)");
    ConsolePrinter.println(header);
    ConsolePrinter.println("\\hline");
    ConsolePrinter.println(String.format(line, "*TOTAL*",
        globalTruePositivesRangeOnly, globalFalsePositivesRangeOnly, globalFalseNegativesRangeOnly,
        getSpanPrecision() * 100, getSpanRecall() * 100, getSpanFMeasure() * 100));
    ConsolePrinter.println("\n");
  }

  public void printHeader(final String header) {
    ConsolePrinter.println("======================================================================================");
    ConsolePrinter.println("# " + header);
    ConsolePrinter.println("======================================================================================");
  }

  /**
   * Dołącza do danych zawartość innego obiektu ChunkerEvaluator.
   */
  public void join(final ChunkerEvaluator foreign) {

    for (final String foreignKey : foreign.types) {
      joinMaps(foreignKey, chunksTruePositives, foreign.chunksTruePositives);
      joinMaps(foreignKey, chunksFalsePositives, foreign.chunksFalsePositives);
      joinMaps(foreignKey, chunksFalseNegatives, foreign.chunksFalseNegatives);
    }
  }

  private void joinMaps(final String key, final HashMap<String, ArrayList<Annotation>> target, final HashMap<String, ArrayList<Annotation>> source) {
    if (source.containsKey(key)) {
      if (!target.containsKey(key)) {
        target.put(key, new ArrayList<>());
      }
      for (final Annotation chunk : source.get(key)) {
        target.get(key).add(chunk);
      }
    }
  }

  /**
   * Wypisuje wynik porównania rozpoznanych anotacji z wzorcowym zbiorem anotacji dla danego zdania.
   *
   * @param sentence
   * @param paragraphId
   * @param truePositives
   * @param falsePositives
   * @param falseNegatives
   */
  private void printSentenceResults(final Sentence sentence, final String paragraphId, final Set<Annotation> truePositives, final Set<Annotation> falsePositives, final Set<Annotation> falseNegatives) {
    String sentenceHeader = "(ChunkerEvaluator) Sentence #" + sentenceNum + " from " + currentDocId;
    if (paragraphId != null) {
      sentenceHeader += " from " + paragraphId;
    }
    ConsolePrinter.log(sentenceHeader);
    final StringBuilder tokenOrths = new StringBuilder();
    final StringBuilder tokenNums = new StringBuilder();
    int idx = 0;
    for (final Token token : sentence.getTokens()) {
      String tokenOrth = token.getOrth();
      String tokenNum = "" + (++idx);
      tokenOrth += StringUtils.repeat("_", Math.max(0, tokenNum.length() - tokenOrth.length()));
      tokenNum += StringUtils.repeat("_", Math.max(0, tokenOrth.length() - tokenNum.length()));
      tokenOrths.append(tokenOrth + " ");
      tokenNums.append(tokenNum + " ");
    }
    ConsolePrinter.log("Text  : " + tokenOrths.toString().trim());
    ConsolePrinter.log("Tokens: " + tokenNums.toString().trim());
    ConsolePrinter.log("");
    ConsolePrinter.log("Chunks:");

    final AnnotationIndex fnIndex = new AnnotationIndex(falseNegatives);

    for (final Annotation chunk : Annotation.sortChunks(truePositives)) {
      ConsolePrinter.log(String.format("  TruePositive %s [%d,%d] = %s (confidence=%.2f)", chunk.getType(), chunk.getBegin() + 1,
          chunk.getEnd() + 1, printChunk(chunk), chunk.getConfidence()));
    }
    for (final Annotation chunk : Annotation.sortChunks(falsePositives)) {
      String errorType = "incorrect boundry";
      final Annotation correctCategory = fnIndex.get(chunk.getBegin(), chunk.getEnd());
      if (correctCategory != null) {
        if (!chunk.getType().equals(correctCategory.getType())) {
          errorType = String.format("incorrect category: %s => %s", chunk.getType(), correctCategory.getType());
        } else if (!chunk.getLemmaOrText().equals(correctCategory.getLemmaOrText())) {
          errorType = String.format("incorrect lemma: %s => %s", chunk.getLemmaOrText(), correctCategory.getLemmaOrText());
        }
      }
      ConsolePrinter.log(String.format("  FalsePositive %s [%d,%d] = %s (confidence=%.2f) (%s)", chunk.getType(), chunk.getBegin() + 1,
          chunk.getEnd() + 1, printChunk(chunk), chunk.getConfidence(), errorType));
    }
    for (final Annotation chunk : Annotation.sortChunks(falseNegatives)) {
      ConsolePrinter.log(String.format("  FalseNegative %s [%d,%d] = %s", chunk.getType(), chunk.getBegin() + 1,
          chunk.getEnd() + 1, printChunk(chunk)));
    }

    ConsolePrinter.log("");
    ConsolePrinter.log("Features:", true);
    final StringBuilder featuresHeader = new StringBuilder("       ");
    for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++) {
      featuresHeader.append(String.format("[%d]_%s ", i + 1, sentence.getAttributeIndex().getName(i)));
    }
    ConsolePrinter.log(featuresHeader.toString(), true);

    idx = 0;
    for (final Token token : sentence.getTokens()) {
      final StringBuilder tokenFeatures = new StringBuilder(String.format("  %3d) ", ++idx));
      for (int i = 0; i < token.getNumAttributes(); i++) {
        tokenFeatures.append(String.format("[%d]_%s ", i + 1, token.getAttributeValue(i)));
      }
      ConsolePrinter.log(tokenFeatures.toString(), true);
    }
    ConsolePrinter.log("", true);
  }

  /**
   * @param chunk
   * @return
   */
  private String printChunk(final Annotation chunk) {
    final List<Token> tokens = chunk.getSentence().getTokens();
    final StringBuilder result = new StringBuilder();
    for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++) {
      result.append(tokens.get(i).getOrth() + " ");
    }
    return result.toString().trim();
  }

}
