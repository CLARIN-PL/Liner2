package g419.liner2.cli.action;


import g419.corpus.ConsolePrinter;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import g419.liner2.core.tools.ChunkerEvaluator;
import g419.liner2.core.tools.ChunkerEvaluatorMuc;
import g419.liner2.core.tools.ProcessingTimer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Evaluate chunker on a specified corpus.
 *
 * @author Michał Marcińczuk
 */
public class ActionEval extends Action {

  private String inputFile = null;
  private String inputFormat = null;
  private boolean errorsOnly = false;

  private boolean checkLemma = false;

  private static final String PARAM_ERRORS_ONLY = "e";
  private static final String PARAM_ERRORS_ONLY_LONG = "errors-only";
  private static final String PARAM_CHECK_LEMMA = "l";
  private static final String PARAM_CHECK_LEMMA_LONG = "lemma";

  //@SuppressWarnings("static-access")
  public ActionEval() {
    super("eval");
    setDescription("evaluates chunkers against a specific set of documents (-i batch:FORMAT, -i FORMAT) #or perform cross validation (-i cv:{format})");

    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getModelFileOption());
    options.addOption(CommonOptions.getVerboseDeatilsOption());
    options.addOption(Option.builder(PARAM_ERRORS_ONLY).longOpt(PARAM_ERRORS_ONLY_LONG).desc("print only sentence with errors").build());
    options.addOption(Option.builder(PARAM_CHECK_LEMMA).longOpt(PARAM_CHECK_LEMMA_LONG).desc("evaluate with annotation lemma").build());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    errorsOnly = line.hasOption(PARAM_ERRORS_ONLY_LONG);
    checkLemma = line.hasOption(PARAM_CHECK_LEMMA_LONG);
    LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
    if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
      ConsolePrinter.verboseDetails = true;
    }
  }

  /**
   *
   */
  @Override
  public void run() throws Exception {

    if (!LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER)) {
      throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
    }

    final ProcessingTimer timer = new ProcessingTimer();
    TokenFeatureGenerator gen = null;
    if (!LinerOptions.getGlobal().features.isEmpty()) {
      gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
    }

    System.out.print("Annotations to evaluate:");
    if (LinerOptions.getGlobal().types.isEmpty()) {
      System.out.print(" all");
    } else {
      for (final Pattern pattern : LinerOptions.getGlobal().types) {
        System.out.print(" " + pattern);
      }
    }
    System.out.println();

    if (inputFormat.startsWith("cv:")) {
      final ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions.getGlobal().types, true);
      globalEval.setCheckLemma(checkLemma);
      final ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

      inputFormat = inputFormat.substring(3);
      LinerOptions.getGlobal().setCVDataFormat(inputFormat);
      final List<List<String>> folds = loadFolds();
      for (int i = 0; i < folds.size(); i++) {
        timer.startTimer("fold " + (i + 1));
        System.out.println("***************************************** FOLD " + (i + 1) + " *****************************************");
        final String trainSet = getTrainingSet(i, folds);
        final String testSet = getTestingSet(i, folds);
        final ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadTrainData(new BatchReader(IOUtils.toInputStream(trainSet, "UTF-8"), "", inputFormat), gen);
        final AbstractDocumentReader reader = new BatchReader(IOUtils.toInputStream(testSet, "UTF-8"), "", inputFormat);
        evaluate(reader, gen, cm, globalEval, globalEvalMuc, i, errorsOnly);
        timer.stopTimer();
      }

      System.out.println("***************************************** SUMMARY *****************************************");
      globalEval.printResults();
      globalEvalMuc.printResults();
      System.out.println("");
      timer.printStats();
    } else {
      final ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
      evaluate(ReaderFactory.get().getStreamReader(inputFile, inputFormat),
          gen, cm, null, null, null, errorsOnly);
    }
  }

  /**
   * @param dataReader
   * @param gen
   * @param cm
   * @param globalEval
   * @param globalEvalMuc
   * @param errorsOnly
   * @throws Exception
   */
  private void evaluate(final AbstractDocumentReader dataReader, final TokenFeatureGenerator gen, final ChunkerManager cm,
                        final ChunkerEvaluator globalEval, final ChunkerEvaluatorMuc globalEvalMuc, final Integer foldNumber, final boolean errorsOnly) throws Exception {
    final ProcessingTimer timer = new ProcessingTimer();
    timer.startTimer("Model loading");
    if (foldNumber != null) {
      cm.loadChunkers(foldNumber);
    } else {
      cm.loadChunkers();
    }
    final Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
    timer.stopTimer();


    /* Create all defined chunkers. */
    final ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().types, false, errorsOnly);
    eval.setCheckLemma(checkLemma);
    final ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

    timer.startTimer("Data reading");
    Document ps = dataReader.nextDocument();
    timer.stopTimer();

    Map<Sentence, AnnotationSet> chunkings = null;
    while (ps != null) {

      /* Get reference set of annotations */
      final Map<Sentence, AnnotationSet> referenceChunks = ps.getChunkings();

      /* Remove annotations from data */
      //ps.removeAnnotations();
      ps.removeAnnotationsByTypePatterns(LinerOptions.getGlobal().types);

      /* Generate features */
      timer.startTimer("Feature generation");
      if (gen != null) {
        gen.generateFeatures(ps);
      }
      timer.stopTimer();

      timer.startTimer("Chunking");
      chunker.prepare(ps);
      try {
        chunkings = chunker.chunk(ps);
      } catch (final Exception ex) {
        System.err.println("Failed to chunk a sentence in document " + ps.getName());
        ex.printStackTrace(System.err);
        chunkings = new HashMap<>();
      }
      timer.stopTimer();

      timer.startTimer("Evaluation", false);
      timer.addTokens(ps);
      if (globalEval != null) {
        globalEval.evaluate(ps, chunkings, referenceChunks);
        globalEvalMuc.evaluate(ps, chunkings, referenceChunks);
      }
      eval.evaluate(ps, chunkings, referenceChunks);
      evalMuc.evaluate(ps, chunkings, referenceChunks);
      timer.stopTimer();

      timer.startTimer("Data reading");
      ps = dataReader.nextDocument();
      timer.stopTimer();
    }

    eval.printResults();
    evalMuc.printResults();
    timer.printStats();
  }

  /**
   * @return
   * @throws IOException
   * @throws DataFormatException
   */
  private List<List<String>> loadFolds() throws IOException, DataFormatException {
    final List<List<String>> folds = new ArrayList<>();
    /** Wczytaj listy plików */
    final File sourceFile = new File(inputFile);
    final String root = sourceFile.getParentFile().getAbsolutePath();
    final BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

    String line = bf.readLine();
    while (line != null) {
      final String[] fileData = line.split("\t");
      if (fileData.length != 2) {
        throw new DataFormatException("Incorrect line in folds file: " + inputFile + "\\" + line + "\nProper line format: {file_name}\\t{fold_nr}");
      }
      String file = fileData[0];
      final int fold = Integer.parseInt(fileData[1]);
      if (!file.startsWith("/")) {
        file = root + "/" + file;
      }
      while (folds.size() < (fold)) {
        folds.add(new ArrayList<>());
      }
      folds.get(fold - 1).add(file);
      line = bf.readLine();
    }
    bf.close();

    return folds;

  }

  private String getTrainingSet(final int fold, final List<List<String>> folds) {
    final StringBuilder sbtrain = new StringBuilder();

    for (int i = 0; i < folds.size(); i++) {
      if (i != fold) {
        for (final String line : folds.get(i)) {
          sbtrain.append(line + "\n");
        }
      }
    }
    return sbtrain.toString().trim();
  }

  private String getTestingSet(final int fold, final List<List<String>> folds) {
    final StringBuilder sbtrain = new StringBuilder();
    for (final String line : folds.get(fold)) {
      sbtrain.append(line + "\n");
    }
    return sbtrain.toString().trim();
  }
}
