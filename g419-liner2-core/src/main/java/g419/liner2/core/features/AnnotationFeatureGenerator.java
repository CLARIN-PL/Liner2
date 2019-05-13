package g419.liner2.core.features;

import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.liner2.core.features.annotations.*;
import g419.liner2.core.tools.parser.MaltSentence;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureGenerator {

  public List<AnnotationAtomicFeature> features = new ArrayList<AnnotationAtomicFeature>();
  private List<AnnotationFeatureMalt> maltFeatures = new ArrayList<AnnotationFeatureMalt>();
  private List<AnnotationSentenceFeature> sentenceFeatures = new ArrayList<AnnotationSentenceFeature>();
  private List<String> featureNames = new ArrayList<String>();

  private Pattern patternBase = Pattern.compile("base:(-?[0-9]*)$");
  private Pattern patternDict = Pattern.compile("dict:([^:]*):([^:]*)$");
  private Pattern patternMalt = Pattern.compile("malt:([^:]*):([0-9]*):(base|relation)$");
  private Pattern patternClosestBase = Pattern.compile("closest-base:(-?[0-9]*):([a-z]+)$");
  private Pattern patternNeFirstBase = Pattern.compile("ne-first-base:(-?[0-9]*):([a-z]+)$");
  private Pattern patternHead = Pattern.compile("head:(.+)$");

  /**
   * @param features — array with feature definitions
   */
  public AnnotationFeatureGenerator(List<String> features) {
    for (String feature : features) {
      if (feature.equals("text")) {
        this.features.add(new AnnotationFeatureText());
        this.featureNames.add(feature);
        continue;
      }

      if (feature.equals("substModifierAfter")) {
        this.features.add(new AnnotationFeatureSubstModifierAfter());
        this.featureNames.add(feature);
        continue;
      }

      if (feature.equals("substModifierBefore")) {
        this.features.add(new AnnotationFeatureSubstModifierBefore());
        this.featureNames.add(feature);
        continue;
      }

      if (feature.equals("verbPredicate")) {
        this.features.add(new AnnotationFeatureVerbPredicate());
        this.featureNames.add(feature);
        continue;
      }

      if (feature.equals("substList")) {
        this.features.add(new AnnotationFeatureSubstList());
        this.featureNames.add(feature);
        continue;
      }

      Matcher matcherBase = this.patternBase.matcher(feature);
      if (matcherBase.find()) {
        AnnotationFeatureContextBase f = new AnnotationFeatureContextBase(Integer.parseInt(matcherBase.group(1)));
        f.setFeatureName(feature);
        this.features.add(f);
        featureNames.add(f.name);
        continue;
      }
      Matcher matcherDict = this.patternDict.matcher(feature);
      if (matcherDict.find()) {
        AnnotationFeatureDict f = new AnnotationFeatureDict(matcherDict.group(2), matcherDict.group(1));
        f.setFeatureName(feature);
        this.features.add(f);
        featureNames.add(f.name);
        continue;
      }
      Matcher matcherHead = this.patternHead.matcher(feature);
      if (matcherHead.find()) {
        AnnotationFeatureHeadValue f = new AnnotationFeatureHeadValue(matcherHead.group(1));
        f.setFeatureName(feature);
        this.features.add(f);
        featureNames.add(f.name);
        continue;
      }
      Matcher matcherClosestBase = this.patternClosestBase.matcher(feature);
      if (matcherClosestBase.find()) {
        AnnotationFeatureClosestBase f = new AnnotationFeatureClosestBase(matcherClosestBase.group(2), Integer.parseInt(matcherClosestBase.group(1)));
        f.setFeatureName(feature);
        this.features.add(f);
        featureNames.add(f.name);
        continue;
      }

      Matcher matcherMalt = this.patternMalt.matcher(feature);
      if (matcherMalt.find()) {
        AnnotationFeatureMalt f = new AnnotationFeatureMalt(matcherMalt.group(1), Integer.parseInt(matcherMalt.group(2)), matcherMalt.group(3));
        f.setFeatureName(feature);
        this.maltFeatures.add(f);
        featureNames.add(f.name);
        continue;
      }
      Matcher matcherNeFirstBase = this.patternNeFirstBase.matcher(feature);
      if (matcherNeFirstBase.find()) {
        AnnotationFeatureNeFirstBase f = new AnnotationFeatureNeFirstBase(matcherNeFirstBase.group(2), Integer.parseInt(matcherNeFirstBase.group(1)));
        f.setFeatureName(feature);
        this.sentenceFeatures.add(f);
        featureNames.add(f.name);
      }

      throw new Error("Unknown annotation feature: " + feature);
    }
  }

  /**
   * TODO: This is buggy because generates values of atomic features and ignore malt and sentence features.
   *
   * @param ann
   * @return
   */
  public List<String> generateAtomicFeatures(Annotation ann) {
    List<String> features = new ArrayList<String>();
    for (AnnotationAtomicFeature afg : this.features) {
      features.add(afg.generate(ann));
    }
    return features;
  }

  /**
   * @param sent
   * @param sentenceAnnotations
   * @return
   */
  public Map<String, Map<Annotation, String>> generate(Sentence sent, Set<Annotation> sentenceAnnotations) {
    Map<String, Map<Annotation, String>> features = new LinkedHashMap<String, Map<Annotation, String>>();
    for (AnnotationAtomicFeature afg : this.features) {
      HashMap<Annotation, String> atomicFeatureVals = new HashMap<Annotation, String>();
      for (Annotation ann : sentenceAnnotations) {
        atomicFeatureVals.put(ann, afg.generate(ann));
      }
      features.put(afg.name, atomicFeatureVals);
    }

    MaltSentence maltSent;
    if (!this.maltFeatures.isEmpty()) {
      maltSent = new MaltSentence(sent, MappingNkjpToConllPos.get());
      for (AnnotationFeatureMalt afg : this.maltFeatures) {
        features.put(afg.name, afg.generate(maltSent.getMaltData(), sentenceAnnotations));
      }
    }
    for (AnnotationSentenceFeature afg : this.sentenceFeatures) {
      features.put(afg.name, afg.generate(sent, sentenceAnnotations));
    }
    return features;
  }

  public int getFeaturesCount() {
    return this.features.size() + this.maltFeatures.size() + this.sentenceFeatures.size();
  }

  /**
   * Return a list of loaded feature names to generate.
   *
   * @return a list of feature names
   */
  public List<String> getFeatureNames() {
    return this.featureNames;
  }


}