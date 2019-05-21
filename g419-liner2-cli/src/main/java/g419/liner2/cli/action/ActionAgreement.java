package g419.liner2.cli.action;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.tools.ChunkerEvaluator;
import g419.liner2.core.tools.ProcessingTimer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

public class ActionAgreement extends Action {

  boolean debug_flag = false;

  private String[] input_files = null;
  private String input_format = null;

  public ActionAgreement() {
    super("agreement");
    setDescription("checks agreement (of annotations) between suplied documents");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getModelFileOption());
    options.addOption(CommonOptions.getVerboseDeatilsOption());

  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_files = line.getOptionValues(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
    if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
      ConsolePrinter.verboseDetails = true;
    }

  }

  @Override
  public void run() throws Exception {
    final ResultHolder results = new ResultHolder();

    if (input_format.startsWith("batch:")) {

      input_format = input_format.substring(6);

      final InputIndexMixer numbers = new InputIndexMixer(input_files.length);
      for (final NumberPair pair : numbers) {
        final AbstractDocumentReader originalDocument = new BatchReader(IOUtils.toInputStream(getBatch(pair.first), "UTF-8"), "", input_format);
        final AbstractDocumentReader referenceDocument = new BatchReader(IOUtils.toInputStream(getBatch(pair.second), "UTF-8"), "", input_format);
        compare(originalDocument, referenceDocument, results);
      }
    } else {

      final InputIndexMixer numbers = new InputIndexMixer(input_files.length);
      for (final NumberPair pair : numbers) {
        final AbstractDocumentReader originalDocument = ReaderFactory.get().getStreamReader(input_files[pair.first], input_format);
        final AbstractDocumentReader referenceDocument = ReaderFactory.get().getStreamReader(input_files[pair.second], input_format);
        compare(originalDocument, referenceDocument, results);
      }
    }

    // Print final results
    results.printResults();
  }

  public void compare(final AbstractDocumentReader dataReader, final AbstractDocumentReader referenceDataReader, final ResultHolder results) throws Exception {

    final ProcessingTimer timer = new ProcessingTimer();

    /* Create all defined chunkers. */
    final ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().types);

    timer.startTimer("Data reading");
    Document originalDocument = dataReader.nextDocument();
    Document referenceDocument = referenceDataReader.nextDocument();
    timer.stopTimer();

    TranslatedChunkings translatedChunkings;
    while (originalDocument != null && referenceDocument != null) {
      try {
        /* Get set of annotations (original and translated), meanwhile checking if documents are the same */
        translatedChunkings = getTranslatedChunkings(originalDocument, referenceDocument);

        /* Evaluate */
        eval.evaluate(originalDocument, translatedChunkings.original, translatedChunkings.translated);
      } catch (final Exception e) {
        System.out.println("Documents do not match!");
        throw e;
      }

      timer.startTimer("Data reading");
      originalDocument = dataReader.nextDocument();
      referenceDocument = referenceDataReader.nextDocument();
      timer.stopTimer();
    }

    // Submit results to holder
    results.submitResult(eval);
    // eval.printResults();
    timer.printStats();
  }

  private String getBatch(final int index) throws IOException {
    final File sourceFile = new File(input_files[index]);
    final String root = sourceFile.getParentFile().getAbsolutePath();
    final StringBuffer outputBuffer = new StringBuffer();
    final BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

    String line;
    while ((line = inputReader.readLine()) != null) {
      line = line.startsWith("/") ? line : root + "/" + line;
      outputBuffer.append(line).append('\n');
    }
    inputReader.close();

    return outputBuffer.toString();
  }

  private static class TranslatedChunkings {
    final HashMap<Sentence, AnnotationSet> original, translated;

    public TranslatedChunkings(final HashMap<Sentence, AnnotationSet> original, final HashMap<Sentence, AnnotationSet> translated) {
      this.original = original;
      this.translated = translated;
    }
  }

  private TranslatedChunkings getTranslatedChunkings(final Document original, final Document reference) {
    // Keep original set of annotations
    final HashMap<Sentence, AnnotationSet> originalChunkings = original.getChunkings();

    // Peel all original annotations from original document (the translated annotations will be placed instead)
    original.removeAnnotations();

    final Iterator<Paragraph> originalParagraphs = original.getParagraphs().iterator();
    final Iterator<Paragraph> referenceParagraphs = reference.getParagraphs().iterator();

    // Translate each paragraph
    while (originalParagraphs.hasNext() && referenceParagraphs.hasNext()) {
      translateParagraphs(originalParagraphs.next(), referenceParagraphs.next());
    }

    // If anything is left in original or reference iterator than the number of paragraphs do not match
    if (originalParagraphs.hasNext() || referenceParagraphs.hasNext()) {
      throw new RuntimeException("Number of paragraphs do not match.");
    }

    return new TranslatedChunkings(originalChunkings, original.getChunkings());
  }

  private void translateParagraphs(final Paragraph translated, final Paragraph reference) {
    final Iterator<Sentence> originalSentences = translated.getSentences().iterator();
    final Iterator<Sentence> referenceSentences = reference.getSentences().iterator();

    // Translate each sentence
    while (originalSentences.hasNext() && referenceSentences.hasNext()) {
      translateSentence(originalSentences.next(), referenceSentences.next());
    }

    // If anything is left in original or reference iterator than the number of sentences do not match
    if (originalSentences.hasNext() || referenceSentences.hasNext()) {
      throw new RuntimeException("Number of sentences in paragraph do not match.");
    }
  }

  private void translateSentence(final Sentence translated, final Sentence reference) {
    if (!compareSentences(translated, reference)) {
      throw new RuntimeException("Sentences do not match.");
    }

    // Create new AnnotationSet for proper sentence (the translated one reference -> original)
    final AnnotationSet translatedAnnotationSet = new AnnotationSet(translated);

    // Get annotations to translate
    final LinkedHashSet<Annotation> referenceAnnotations = reference.getChunks();

    // Translate each annotation to original sentence and put them into created AnnotationSet
    for (final Annotation oldAnnotation : referenceAnnotations) {
      translatedAnnotationSet.addChunk(new Annotation(oldAnnotation.getBegin(), oldAnnotation.getEnd(), oldAnnotation.getType(), translated));
    }

    // Attach AnnotationSet to sentence
    translated.addAnnotations(translatedAnnotationSet);

  }

  private boolean compareSentences(final Sentence firstSentence, final Sentence secondSentence) {
    final List<Token> firstSentenceTokens = firstSentence.getTokens();
    final List<Token> seconsSentenceTokens = secondSentence.getTokens();

    boolean match = firstSentenceTokens.size() == seconsSentenceTokens.size();
    for (int i = 0; match && i < firstSentenceTokens.size() && i < seconsSentenceTokens.size(); i++) {
      match &= compareTokens(firstSentenceTokens.get(i), seconsSentenceTokens.get(i));
    }

    return match;
  }

  private boolean compareTokens(final Token firstToken, final Token secondToken) {
    final int firstTokenAttributesNo = firstToken.getNumAttributes();
    final int secondTokenAttributesNo = secondToken.getNumAttributes();

    boolean match = (firstTokenAttributesNo == secondTokenAttributesNo) && (firstToken.getNoSpaceAfter() == secondToken.getNoSpaceAfter());
    for (int i = 0; match && i < firstTokenAttributesNo && i < secondTokenAttributesNo; i++) {
      match &= firstToken.getAttributeValue(i).equals(secondToken.getAttributeValue(i));
    }

    return match;
  }

  /*
   * Holds info about evaluation results from all data sets.
   */
  private static class ResultHolder {
    int evaluationNumber;
    float precision, spanPrecision, recall, spanRecall;
    int truePositive, falsePositive, falseNegative;

    public ResultHolder() {
      precision = spanPrecision = recall = spanRecall = 0.0f;
      evaluationNumber = truePositive = falsePositive = falseNegative = 0;
    }

    public void submitResult(final ChunkerEvaluator eval) {
      evaluationNumber++;

      precision += eval.getPrecision();
      spanPrecision += eval.getSpanPrecision();
      recall += eval.getRecall();
      spanRecall += eval.getSpanRecall();

      truePositive += eval.getTruePositive();
      falsePositive += eval.getFalsePositive();
      falseNegative += eval.getFalseNegative();
    }

    public float getPrecision() {
      return precision / evaluationNumber;
    }

    public float getSpanPrecision() {
      return spanPrecision / evaluationNumber;
    }

    public float getRecall() {
      return recall / evaluationNumber;
    }

    public float getSpanRecall() {
      return spanRecall / evaluationNumber;
    }

    public float getFMeasure() {
      final float p = precision;
      final float r = recall;
      return (p + r) == 0 ? 0 : (2 * p * r) / (p + r);
    }

    public float getSpanFMeasure() {
      final float p = spanPrecision;
      final float r = spanRecall;
      return (p + r) == 0 ? 0 : (2 * p * r) / (p + r);
    }

    public int getTruePositive() {
      return truePositive;
    }

    public int getFalsePositive() {
      return falsePositive;
    }

    public int getFalseNegative() {
      return falseNegative;
    }

    public void printResults() {
      final String header = "        Annotation           &   TP &   FP &   FN & Precision & Recall  & F$_1$   \\\\";
      final String line = "        %-20s & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\";

      printHeader("Exact match evaluation -- annotation span and types evaluation");
      System.out.println(header);
      System.out.println("\\hline");
      final ArrayList<String> keys = new ArrayList<>();
      System.out.println("\\hline");
      System.out.println(String.format(line, "*TOTAL*", getTruePositive(), getFalsePositive(), getFalseNegative(), getPrecision() * 100,
          getRecall() * 100, getFMeasure() * 100));
      System.out.println("\n");

      // this.printHeader("Annotation span evaluation (annotation types are ignored)");
      // System.out.println(header);
      // System.out.println("\\hline");
      // System.out.println(String.format(line, "*TOTAL*",
      // this.globalTruePositivesRangeOnly, this.globalFalsePositivesRangeOnly, this.globalFalseNegativesRangeOnly,
      // this.getSpanPrecision()*100, this.getSpanRecall()*100, this.getSpanFMeasure()*100));
      // System.out.println("\n");
    }

    public void printHeader(final String header) {
      System.out.println("======================================================================================");
      System.out.println("# " + header);
      System.out.println("======================================================================================");
    }
  }

  /*
   * Holds information (indexes) about which two inputs should be compared.
   */
  private class NumberPair {
    public final int first, second;

    public NumberPair(final int first, final int second) {
      this.first = first;
      this.second = second;
    }
  }

  /* Mixes input data (indexes) in such way that each document or batch is compared with others.
   * Eliminates redundancy (situation in which two data sets have been compared in different order)
   * ie. (a,b) has been checked already, so (b,a) is omitted.
   */
  private class InputIndexMixer implements Iterable<NumberPair>, Iterator<NumberPair> {
    int firstIndex, secondIndex, max;

    public InputIndexMixer(final int max) {
      if (max < 2) {
        throw new RuntimeException("Cannot check agreement, too few documents to compare.");
      }

      this.max = max;
      secondIndex = 0;
      firstIndex = 0;
    }

    @Override
    public Iterator<NumberPair> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return max - firstIndex > 2 || max - secondIndex > 1;
    }

    @Override
    public NumberPair next() {

      if (++secondIndex == max) {
        secondIndex = ++firstIndex + 1;
      }

      return new NumberPair(firstIndex, secondIndex);
    }

    @Override
    public void remove() {
      // Dummy, no removing
    }
  }

}
