package g419.liner2.core.chunker;

import g419.corpus.ConsolePrinter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.chunker.interfaces.DeserializableChunkerInterface;
import g419.liner2.core.chunker.interfaces.SerializableChunkerInterface;
import g419.liner2.core.chunker.interfaces.TrainableChunkerInterface;
import g419.liner2.core.features.AnnotationFeatureGenerator;
import weka.classifiers.Classifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TODO: wymaga aktualizacji dla rozdzielenia train na addTrainingData i train.
 *
 * @author czuk
 */
public class AnnotationWekaClassifierChunker extends Chunker
    implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {

  private AnnotationFeatureGenerator featureGenerator = null;
  private Chunker inputChunker = null;

  /**
   * To be serialized.
   */
  private Classifier classifier = null;
  private FastVector classes = new FastVector();
  private StringToNominal filter = null;
  private List<String> features = null;
  List<Pattern> types;
  FastVector fva = null;
  Instances instances = null;

  /**
   * @param inputChunker
   */
  public AnnotationWekaClassifierChunker(Chunker inputChunker, List<String> features) {
    this.features = features;
    this.featureGenerator = new AnnotationFeatureGenerator(features);
    this.inputChunker = inputChunker;
  }

  public void setTypes(List<Pattern> types) {
    this.types = types;
  }

  public void initializeTraining(String classifierName, String[] classifierOptions, String strategy) {
    try {
      if (strategy.equals("1-vs-all")) {
        classifier = new MultiClassClassifier();
        classifier.setOptions(new String[] {"-W", classifierName});
      } else if (strategy.equals("multi")) {
        classifier = Classifier.forName(classifierName, classifierOptions);
      } else {
        throw new Exception("Invalid classifier strategy: " + strategy);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.fva = this.getAttributesDefinition();
    this.instances = new Instances("ner", fva, 10);
    instances.setClassIndex(this.featureGenerator.getFeaturesCount());
  }

  /**
   * @return
   */
  private FastVector getAttributesDefinition() {
    FastVector attributes = new FastVector(
        this.featureGenerator.getFeaturesCount() + 1);
    FastVector fvnull = null;

    for (int i = 0; i < this.featureGenerator.getFeaturesCount(); i++) {
      attributes.addElement(new Attribute("attr" + i, fvnull));
    }
    attributes.addElement(new Attribute("class", this.classes));

    return attributes;
  }

  /**
   * @param paragraphSet
   */
  public void updateClassDomain(Document paragraphSet) {
    // Create a list of possible classes on the basis on training set
    for (Sentence sentence : paragraphSet.getSentences()) {
      for (Annotation ann : sentence.getChunks()) {
        if (!this.classes.contains(ann.getType())) {
          if (types != null && !types.isEmpty()) {
            for (Pattern patt : types) {
              if (patt.matcher(ann.getType()).find()) {
                this.classes.addElement(ann.getType());
                break;
              }
            }
          } else {
            this.classes.addElement(ann.getType());
          }
        }

      }
    }
  }

  @Override
  public void train() throws Exception {
    ConsolePrinter.log("AnnoatationClassifier: training model");
    this.filter = new StringToNominal();
    filter.setAttributeRange("first-" + this.featureGenerator.getFeaturesCount());
    filter.setInputFormat(instances);
    for (int i = 0; i < instances.numInstances(); i++) {
      filter.input(instances.instance(i));
    }

    filter.batchFinished();

    Instances instancesFiltered = filter.getOutputFormat();
    Instance filtered;
    while ((filtered = filter.output()) != null) {
      instancesFiltered.add(filtered);
    }

    this.classifier.buildClassifier(instancesFiltered);
  }

  /**
   * Wczytuje chunker z modelu binarnego.
   *
   * @param model_filename
   */
  @Override
  public void deserialize(String model_filename) {
    try {
      FileInputStream stream = new FileInputStream(model_filename);
      ObjectInputStream in = new ObjectInputStream(stream);
      this.classes = (FastVector) in.readObject();
      this.classifier = (Classifier) in.readObject();
      this.filter = (StringToNominal) in.readObject();
      in.close();
      stream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void serialize(String filename) {
    try {
      FileOutputStream stream = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(stream);
      out.writeObject(this.classes);
      out.writeObject(this.classifier);
      out.writeObject(this.filter);
      out.close();
      stream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }


  @Override
  public Map<Sentence, AnnotationSet> chunk(Document ps) {
    Map<Sentence, AnnotationSet> inputChunks = this.inputChunker.chunk(ps);
    try {
      this.classify(inputChunks);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    return inputChunks;
  }

  /**
   * Classify each annotation in the collection and set the annotation category.
   *
   * @param annotationsBySentence — collection of AnnotationSet's grouped by sentences
   * @throws Exception
   */
  public void classify(Map<Sentence, AnnotationSet> annotationsBySentence) throws Exception {
    ConsolePrinter.log("AnnoatationClassifier: classifying data");
    if (fva == null) {
      fva = this.getAttributesDefinition();
    }
    Instances instances = new Instances("ner", fva, 10);
    instances.setClassIndex(this.featureGenerator.getFeaturesCount());

    List<Annotation> allAnnotations = new ArrayList<Annotation>();
    for (Sentence sentence : annotationsBySentence.keySet()) {

      Map<String, Map<Annotation, String>> allFeatures = this.featureGenerator.generate(sentence, annotationsBySentence.get(sentence).chunkSet());

      instanceLoop:
      for (Annotation ann : annotationsBySentence.get(sentence).chunkSet()) {
        Instance instance = new Instance(this.featureGenerator.getFeaturesCount() + 1);

        int featureIndex = 0;
        for (Map<Annotation, String> featureValues : allFeatures.values()) {
          if (!featureValues.containsKey(ann)) {
            continue instanceLoop;
          }
          allAnnotations.add(ann);
          instance.setValue((Attribute) fva.elementAt(featureIndex), featureValues.get(ann));
          featureIndex++;
        }
        instances.add(instance);
      }
    }

    for (int i = 0; i < instances.numInstances(); i++) {
      filter.input(instances.instance(i));
    }

    filter.batchFinished();

    Instance filtered = null;
    Attribute classAttribute = filter.getOutputFormat().classAttribute();
    int index = 0;
    int zero = 0;
    while ((filtered = filter.output()) != null) {
      Instance instance = filtered;
      double d = this.classifier.classifyInstance(instance);
      if (d < 1.0) {
        zero++;
      }
      allAnnotations.get(index++).setType(classAttribute.value((int) d));
    }
  }

  @Override
  public void addTrainingData(Document document) throws Exception {
    ConsolePrinter.log("AnnoatationClassifier: Loading training data for from document:" + document.getName());
    updateClassDomain(document);
    for (Sentence sentence : document.getSentences()) {
      Map<String, Map<Annotation, String>> allFeatures = this.featureGenerator.generate(sentence, sentence.getChunks());
      instanceLoop:
      for (Annotation ann : sentence.getChunks()) {
        if (this.classes.contains(ann.getType())) {
          Instance instance = new Instance(this.featureGenerator.getFeaturesCount() + 1);
          instance.setDataset(instances);

          int featureIndex = 0;
          for (Map<Annotation, String> featureValues : allFeatures.values()) {
            if (!featureValues.containsKey(ann)) {
              continue instanceLoop;
            }
            instance.setValue((Attribute) fva.elementAt(featureIndex), featureValues.get(ann));
            featureIndex++;
          }
          instance.setClassValue(ann.getType());
          instances.add(instance);
        }
      }
    }

  }

}
