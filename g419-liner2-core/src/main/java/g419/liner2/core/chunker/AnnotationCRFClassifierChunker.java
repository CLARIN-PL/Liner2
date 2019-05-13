package g419.liner2.core.chunker;

import g419.corpus.structure.*;
import g419.liner2.core.features.AnnotationFeatureGenerator;
import g419.liner2.core.features.TokenFeatureGenerator;
import g419.liner2.core.features.TokenToAnnotationFeatureGenerator;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Michał Krautforst
 */
public class AnnotationCRFClassifierChunker extends Chunker {

  private List<Pattern> list;
  private String base;
  private CrfppChunker baseChunker;
  private TokenToAnnotationFeatureGenerator tokenToAnnotationFeatureGenerator;
  private AnnotationFeatureGenerator annotationFeatureGenerator;

  public AnnotationCRFClassifierChunker(List<Pattern> list, String base, CrfppChunker baseChunker, TokenFeatureGenerator gen, List<String> annotationFeatures, String featuresContext) throws Exception {
    this.list = list;
    this.base = base;
    this.baseChunker = baseChunker;
    this.annotationFeatureGenerator = new AnnotationFeatureGenerator(annotationFeatures);
    this.tokenToAnnotationFeatureGenerator = new TokenToAnnotationFeatureGenerator(gen);

    for (String featureName : this.annotationFeatureGenerator.getFeatureNames()) {
      if (!baseChunker.getTemplate().getFeatures().containsKey(featureName)) {
        baseChunker.getTemplate().addFeature(featureName + ":" + featuresContext);
//                    ToDo: Zdecydować czy warto generować cechy złożone z context dla cech anotacji
        String[] windowDesc = baseChunker.getTemplate().getFeatures().get(featureName);
        for (int i = 1; i < windowDesc.length; i++) {
          baseChunker.getTemplate().addFeature(featureName + ":" + windowDesc[i] + "/context:0");
        }
      }
    }
  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    Document wrapped = null;
    try {
      wrapped = prepareData(ps, "classify");
    } catch (Exception e) {
      System.out.println("AnnotationCRFClassifier: Error while preparing training data");
      System.exit(1);
    }
    HashMap<Sentence, AnnotationSet> chunked = baseChunker.chunk(wrapped);
    HashMap<Sentence, AnnotationSet> result = new HashMap<Sentence, AnnotationSet>();
    HashMap<String, Sentence> sentences = sentencesById(ps.getSentences());
    for (AnnotationSet wrappedSet : chunked.values()) {
      Sentence sent = sentences.get(wrappedSet.getSentence().getId());
      AnnotationSet newSet = new AnnotationSet(sent);
      for (Annotation source : sent.getChunks()) {
        Annotation newAnn = source.clone();
        if (source.getType().equals(base)) {
          for (Annotation wrappedAnn : wrappedSet.chunkSet()) {
            if (wrappedAnn.getText().equals(source.getText())) {
              newAnn.setType(wrappedAnn.getType());
              break;
            }

          }
        }
        newSet.addChunk(newAnn);
      }
      result.put(sent, newSet);
    }

    for (Sentence sent : ps.getSentences()) {
      LinkedHashSet<Annotation> originalAnns = sent.getChunks();
      for (Annotation newAnn : result.get(sent).chunkSet()) {
        for (Annotation oldAnn : originalAnns) {
          if (newAnn.getTokens().equals(oldAnn.getTokens()) && !newAnn.getType().equals("nam") && oldAnn.getType().equals("nam")) {
            originalAnns.remove(oldAnn);
            break;
          }
        }
      }
    }
    return result;
  }

  public HashMap<String, Sentence> sentencesById(ArrayList<Sentence> sentences) {
    HashMap<String, Sentence> mapped = new HashMap<String, Sentence>();
    for (Sentence sent : sentences) {
      mapped.put(sent.getId(), sent);
    }
    return mapped;
  }

  public Document prepareData(Document doc, String mode) throws Exception {
    Paragraph whole = new Paragraph("wholeDoc");
    TokenAttributeIndex index = doc.getAttributeIndex().clone();
    index.addAttribute("context");
    for (String featureName : this.annotationFeatureGenerator.getFeatureNames()) {
      index.addAttribute(featureName);
    }
    for (Sentence sentence : doc.getSentences()) {
      Sentence workCopySent = sentence.clone();
      workCopySent.setAttributeIndex(index);
      generateAnnotationFeatures(workCopySent);
      HashMap<Integer, Annotation> annotationsByStart = filterAnnotations(workCopySent.getChunks(), mode);
      Sentence prepared = wrapAnnotations(annotationsByStart, workCopySent, index);
      prepared.setAttributeIndex(index);
      whole.addSentence(prepared);
    }
    Document crfppData = new Document(doc.getName(), index);
    crfppData.addParagraph(whole);
    return crfppData;

  }

  private Sentence wrapAnnotations(HashMap<Integer, Annotation> annotationsByStarts, Sentence sentence, TokenAttributeIndex index) throws Exception {
    List<Token> tokens = sentence.getTokens();
    Sentence wrappedSentence = new Sentence();
    ArrayList<Token> wrappedTokens = new ArrayList<Token>();
    LinkedHashSet<Annotation> wrappedAnnotations = new LinkedHashSet<Annotation>();
    HashMap<Token, Annotation> oldAnnotationData = new HashMap<Token, Annotation>();

    wrappedSentence.setTokens(wrappedTokens);
    wrappedSentence.setId(sentence.getId());

    for (int i = 0; i < tokens.size(); i++) {
      Token newToken;
      if (annotationsByStarts.containsKey(i)) {
        Annotation chosen = annotationsByStarts.get(i);
        newToken = findHead(chosen, sentence).clone();
        newToken.setAttributeValue(index.getIndex("context"), "A");
        int newIndex = wrappedTokens.size();
        newToken.setAttributeIndex(index);
        wrappedTokens.add(newToken);
        oldAnnotationData.put(newToken, chosen);
        wrappedAnnotations.add(new Annotation(newIndex, newIndex, chosen.getType(), wrappedSentence));
        i = chosen.getEnd();
      } else {
        newToken = tokens.get(i).clone();
        newToken.setAttributeValue(index.getIndex("context"), "T");
        newToken.setAttributeIndex(index);
        wrappedTokens.add(newToken);

      }

    }

    wrappedSentence.setAttributeIndex(index);
    wrappedSentence.setAnnotations(new AnnotationSet(wrappedSentence, wrappedAnnotations));
    this.tokenToAnnotationFeatureGenerator.mapFeatures(wrappedSentence, oldAnnotationData);

    return wrappedSentence;

  }

  private void generateAnnotationFeatures(Sentence sent) throws Exception {
    TokenAttributeIndex index = sent.getAttributeIndex();
    LinkedHashSet<Annotation> annotations = new LinkedHashSet<Annotation>(sent.getChunks());
    for (int i = 0; i < sent.getTokenNumber(); i++) {
      if (sent.getChunksAt(i, null).isEmpty()) {
        annotations.add(new Annotation(i, i, "token", sent));
      }

    }
    Map<String, Map<Annotation, String>> annotationFeatures = this.annotationFeatureGenerator.generate(sent, annotations);
    List<Token> tokens = sent.getTokens();
    for (String featureName : annotationFeatures.keySet()) {
      Map<Annotation, String> feature = annotationFeatures.get(featureName);
      for (Annotation ann : feature.keySet()) {
        for (int tokenIdx : ann.getTokens()) {
          tokens.get(tokenIdx).setAttributeValue(index.getIndex(featureName), feature.get(ann));
        }
      }
    }
  }


  private Token findHead(Annotation ann, Sentence sentence) {
    List<Token> tokens = sentence.getTokens();
    TokenAttributeIndex index = sentence.getAttributeIndex();
    for (Token tok : tokens.subList(ann.getBegin(), ann.getEnd() + 1)) {
      String tokClass = tok.getAttributeValue(index.getIndex("class"));
      if (tokClass != null && tokClass.equals("subst")) {
        return tok;
      }
    }
    return tokens.get(ann.getBegin());
  }

  private HashMap<Integer, Annotation> filterAnnotations(HashSet<Annotation> annotations, String mode) {
    HashSet<Annotation> annotationsToWrap = new HashSet<Annotation>();
    if (mode.equals("train")) {
      for (Annotation ann : annotations) {
        for (Pattern patt : list) {
          if (patt.matcher(ann.getType()).find()) {
            HashSet<Annotation> toRemove = new HashSet<Annotation>();
            boolean add = true;
            for (Annotation chosen : annotationsToWrap) {
              if (ann.getTokens().containsAll(chosen.getTokens())) {
                toRemove.add(chosen);
              } else if (chosen.getTokens().containsAll(ann.getTokens())) {
                add = false;
                break;
              }
            }
            for (Annotation replaced : toRemove) {
              annotationsToWrap.remove(replaced);
            }
            if (add) {
              annotationsToWrap.add(ann);
            }
            break;
          }
        }
      }
    } else if (mode.equals("classify")) {
      for (Annotation ann : annotations) {
        if (base.equals(ann.getType())) {
          annotationsToWrap.add(ann);
        }
      }
    }
    HashMap<Integer, Annotation> annotationsByStart = new HashMap<Integer, Annotation>();
    for (Annotation ann : annotationsToWrap) {
      annotationsByStart.put(ann.getBegin(), ann);
    }
    return annotationsByStart;
  }

}
