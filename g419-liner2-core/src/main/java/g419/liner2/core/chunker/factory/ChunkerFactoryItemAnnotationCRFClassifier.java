package g419.liner2.core.chunker.factory;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.AnnotationCRFClassifierChunker;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.CrfppChunker;
import g419.liner2.core.features.TokenFeatureGenerator;
import g419.liner2.core.lib.LibLoaderCrfpp;
import g419.liner2.core.tools.TemplateFactory;
import org.ini4j.Ini;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by michal on 9/2/14.
 */
public class ChunkerFactoryItemAnnotationCRFClassifier extends ChunkerFactoryItem {

  public ChunkerFactoryItemAnnotationCRFClassifier() {
    super("CRFclassifier");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    try {
      LibLoaderCrfpp.load();
    } catch (final UnsatisfiedLinkError e) {
      System.exit(1);
    }
    final String mode = description.get("mode");

    if (mode.equals("train")) {
      return train(description, cm);
    } else if (mode.equals("load")) {
      return load(description, cm);
    } else {
      throw new Exception("Unrecognized mode for CRFPP annotation classifier: " + mode + "(Valid: train/load)");
    }
  }

  private List<String> parseAnnotationFeatures(final String filePath) throws IOException {
    final List<String> features = new ArrayList<>();
    if (filePath != null) {
      final File featuresFile = new File(filePath);
      if (!featuresFile.exists()) {
        throw new FileNotFoundException("Error while parsing features:" + filePath + " is not an existing file!");
      }
      final String iniPath = featuresFile.getAbsoluteFile().getParentFile().getAbsolutePath();
      final BufferedReader br = new BufferedReader(new FileReader(featuresFile));
      final StringBuffer sb = new StringBuffer();
      String feature = br.readLine();
      while (feature != null) {
        if (!feature.isEmpty() && !feature.startsWith("#")) {
          feature = feature.trim().replace("{INI_PATH}", iniPath);
          features.add(feature);
        }
        feature = br.readLine();
      }
    }
    return features;
  }

  private Chunker load(final Ini.Section description, final ChunkerManager cm) throws Exception {


    final String store = description.get("store");

    ConsolePrinter.log("--> CRFPP Chunker deserialize from " + store);
    final CrfppChunker baseChunker = new CrfppChunker(loadUsedFeatures(description.get("crf-features")), null);
    baseChunker.deserialize(store);
    final CrfTemplate template = createTemplate(description.get("template"), description.get("context"));
    baseChunker.setTemplate(template);
    final TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
    final AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(null, description.get("base-annotation"), baseChunker, gen, parseAnnotationFeatures(description.get("annotation-features")), description.get("context"));

    return chunker;
  }

  private Chunker train(final Ini.Section description, final ChunkerManager cm) throws Exception {
    ConsolePrinter.log("--> CRFPP annotation classifier train");

    final String inputFile = description.get("training-data");
    final String inputFormat;

    final String modelFilename = description.get("store");
    final TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);

    ArrayList<Document> trainData = new ArrayList<>();
    if (inputFile.equals("{CV_TRAIN}")) {
      trainData = cm.trainingData;
    } else {
      inputFormat = description.get("format");
      final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
      Document document = reader.nextDocument();
      while (document != null) {
        gen.generateFeatures(document);
        trainData.add(document);
        document = reader.nextDocument();
      }
    }
    final List<Pattern> list = LinerOptions.getGlobal().parseTypes(description.get("types"));

    final CrfppChunker baseChunker = new CrfppChunker(Integer.parseInt(description.get("threads")), list, loadUsedFeatures(description.get("crf-features")), null);
    baseChunker.setTrainingDataFilename(description.get("store-training-data"));
    baseChunker.setModelFilename(modelFilename);
    ConsolePrinter.log("--> Training on file=" + inputFile);

    final CrfTemplate template = createTemplate(description.get("template"), description.get("context"));
    baseChunker.setTemplate(template);
    final AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(list, description.get("base-annotation"), baseChunker, gen, parseAnnotationFeatures(description.get("annotation-features")), description.get("context"));

    for (final Document document : trainData) {
      gen.generateFeatures(document);
      final Document wrapped = chunker.prepareData(document, "train");
      baseChunker.addTrainingData(wrapped);
      if (template.getAttributeIndex() == null) {
        template.setAttributeIndex(wrapped.getAttributeIndex());
      }
    }
    baseChunker.train();

    return chunker;

  }

  private CrfTemplate createTemplate(final String templateData, final String context) throws Exception {
    final CrfTemplate template = TemplateFactory.parseTemplate(templateData);
    template.addFeature("context:" + context);
    for (final String feature : new ArrayList<>(template.getFeatureNames())) {
      if (!(feature.contains("/") || feature.equals("context"))) {
        final String[] windowDesc = template.getFeatures().get(feature);
        for (int i = 1; i < windowDesc.length; i++) {
          template.addFeature(feature + ":" + windowDesc[i] + "/context:0");
        }
      }
    }
    return template;
  }

  private List<String> loadUsedFeatures(final String file) throws IOException {
    final ArrayList<String> usedFeatures = new ArrayList<>();
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();
    while (line != null) {
      if (!(line.isEmpty() || line.startsWith("#"))) {
        usedFeatures.add(line.trim());
      }
      line = reader.readLine();
    }
    return usedFeatures;
  }
}
