package g419.corpus.structure;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Michał Marcińczuk
 */
public class Document {

  String name = null;
  String uri = null;
  TokenAttributeIndex attributeIndex = null;
  List<Paragraph> paragraphs = new ArrayList<>();
  DocumentDescriptor documentDescriptor = new DocumentDescriptor();

  private HashMap<String, Integer> bases = null;
  private HashMap<String, Integer> titleBases = null;
  private final Set<Frame<Annotation>> frames = Sets.newHashSet();

  /* Zbiór relacji */
  RelationSet relations = new RelationSet();

  public Document(final String name, final TokenAttributeIndex attributeIndex) {
    this.name = name;
    this.attributeIndex = attributeIndex;
  }

  public Document(final String name, final List<Paragraph> paragraphs, final TokenAttributeIndex attributeIndex) {
    this.name = name;
    this.paragraphs = paragraphs;
    for (final Paragraph paragraph : paragraphs) {
      paragraph.setDocument(this);
    }
    this.attributeIndex = attributeIndex;
  }

  public Document(final String name, final List<Paragraph> paragraphs, final TokenAttributeIndex attributeIndex, final RelationSet relations) {
    this.name = name;
    this.paragraphs = paragraphs;
    this.attributeIndex = attributeIndex;
    this.relations = relations;
  }

  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Get the name of document source. If the document was read from a file,
   * it is a relative path to the file.
   *
   * @return source of the document
   */
  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(final String uri) {
    this.uri = uri;
  }

  public Set<Relation> getRelationsSet() {
    return relations.getRelations();
  }

  public RelationSet getRelations() {
    return relations;
  }

  public RelationSet getRelations(final String set) {
    return relations.filterBySet(set);
  }

  public void setRelations(final RelationSet relations) {
    this.relations = relations;
  }

  public void addParagraph(final Paragraph paragraph) {
    paragraphs.add(paragraph);
    if (paragraph.getAttributeIndex() == null) {
      paragraph.setAttributeIndex(attributeIndex);
    }
  }

  public TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  public List<Paragraph> getParagraphs() {
    return paragraphs;
  }

  public Set<Frame<Annotation>> getFrames() {
    return frames;
  }

  public void setAttributeIndex(final TokenAttributeIndex attributeIndex) {
    this.attributeIndex = attributeIndex;
    for (final Paragraph p : paragraphs) {
      p.setAttributeIndex(this.attributeIndex);
    }
  }

  /**
   * Creates a copy of collections of annotations. A new collection is created. The annotation
   * and sentence are not copied.
   *
   * @return
   */
  public HashMap<Sentence, AnnotationSet> getChunkings() {
    final HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        final LinkedHashSet<Annotation> annotations = new LinkedHashSet<>();
        annotations.addAll(sentence.getChunks());
        chunkings.put(sentence, new AnnotationSet(sentence, annotations));
      }
    }
    return chunkings;
  }

  /**
   * Add annotations to sentences.
   *
   * @param chunkings
   */
  public void addAnnotations(final HashMap<Sentence, AnnotationSet> chunkings) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentence.addAnnotations(chunkings.get(sentence));
      }
    }
  }

  /**
   * Discard existing annotations and set given set.
   *
   * @param chunkings
   */
  public void setAnnotations(final HashMap<Sentence, AnnotationSet> chunkings) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentence.setAnnotations(chunkings.get(sentence));
      }
    }
  }

  public ArrayList<Sentence> getSentences() {
    final ArrayList<Sentence> sentences = new ArrayList<>();
    for (final Paragraph paragraph : paragraphs) {
      sentences.addAll(paragraph.getSentences());
    }
    return sentences;
  }

  public Optional<Sentence> getSentenceById(String id) {
    for( Sentence s : getSentences()) {
      if (s.getId().equals(id))
        return Optional.of(s);
    }
    return Optional.empty();
  }


  public void removeAnnotations(final List<Annotation> annotations) {
    for (final Annotation annotation : annotations) {
      annotation.getSentence().getChunks().remove(annotation);
    }
  }

  /**
   * Removes all anotation with given name.
   *
   * @param annotation
   */
  public void removeAnnotations(final String annotation) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentence.removeAnnotations(annotation);
      }
    }
  }

  /**
   * Removes all anotation
   */
  public void removeAnnotations() {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentence.chunks = new LinkedHashSet<>();
      }
    }
  }

  /**
   * Remove all annotations which type matches one of given type name patterns.
   *
   * @param types
   */
  public void removeAnnotationsByTypePatterns(final List<Pattern> types) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        sentence.getChunks().removeAll(sentence.getAnnotations(types));
      }
    }
  }

  /**
   * Removes metadata from chunks with given name
   */
  public void removeMetadata(final String key) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        for (final Annotation annotation : sentence.chunks) {
          annotation.getMetadata().remove(key);
        }
      }
    }
  }

  /**
   * Retreives Annotation given sentence id, channel and annotation index in channel
   */
  public Annotation getAnnotation(final String sentenceId, final String channelName, final int annotationIdx) {
    for (final Paragraph paragraph : paragraphs) {
      for (final Sentence sentence : paragraph.getSentences()) {
        if (sentence.getId().equals(sentenceId)) {
          return sentence.getAnnotationInChannel(channelName, annotationIdx);
        }
      }
    }
    return null;
  }

  public ArrayList<Annotation> getAnnotations(final List<Pattern> types) {
    final ArrayList<Annotation> annotations = new ArrayList<>();
    for (final Sentence sentence : getSentences()) {
      for (final Annotation sentenceAnnotation : sentence.getAnnotations(types)) {
        annotations.add(sentenceAnnotation);
      }
    }

    return annotations;
  }

  public ArrayList<Annotation> getAnnotations() {
    final ArrayList<Annotation> annotations = new ArrayList<>();

    for (final Sentence sentence : getSentences()) {
      for (final Annotation sentenceAnnotation : sentence.getChunks()) {
        annotations.add(sentenceAnnotation);
      }
    }

    return annotations;
  }

  @Override
  public Document clone() {
    final Document copy = new Document(name, attributeIndex.clone());
    for (final Paragraph p : paragraphs) {
      copy.addParagraph(p.clone());
    }
    copy.documentDescriptor = documentDescriptor.clone();
    return copy;

  }

  public void addRelation(final Relation relation) {
    relations.addRelation(relation);
  }

  /**
   * Removes given annotations from relational clusters and refreshes
   * documents' relation set
   *
   * @param annotations
   */
  public void filterAnnotationClusters(final List<Annotation> annotations) {
    final AnnotationClusterSet clusterSet = AnnotationClusterSet.fromRelationSet(relations);
    clusterSet.removeAnnotations(annotations);
    relations = clusterSet.getRelationSet(new AnnotationCluster.ReturnRelationsToHead());
  }

  /**
   * Przepięcie relacji z anotacji źródłowej do anotacji docelowej
   *
   * @param source
   * @param dest
   */
  public void rewireSingleRelations(final Annotation source, final Annotation dest) {
    final List<Relation> rewired = new ArrayList<>();

    if (relations.incomingRelations.containsKey(source)) {
      for (final Relation incoming : relations.incomingRelations.get(source)) {
        if (incoming.getAnnotationTo().equals(dest)) {
          continue;
        }
        final Relation rwRel = new Relation(incoming.getAnnotationFrom(), dest, incoming.getType(), incoming.getSet(), this);
        rewired.add(rwRel);
        relations.relations.remove(incoming);
      }
      relations.refresh();
//				this.relations.incomingRelations.remove(source);
    }

    if (relations.outgoingRelations.containsKey(source)) {
      for (final Relation outgoing : relations.outgoingRelations.get(source)) {
        if (outgoing.getAnnotationFrom().equals(dest)) {
          continue;
        }
        final Relation rwRel = new Relation(dest, outgoing.getAnnotationTo(), outgoing.getType(), outgoing.getSet(), this);
        rewired.add(rwRel);
        relations.relations.remove(outgoing);
      }
//				this.relations.outgoingRelations.remove(source);
    }

    for (final Relation relation : rewired) {
      relations.addRelation(relation);
    }
    relations.refresh();
  }

  public DocumentDescriptor getDocumentDescriptor() {
    return documentDescriptor;
  }

  public int isInTitle(final String base) {
    if (titleBases == null) {
      titleBases = new HashMap<>();
      for (final Paragraph p : paragraphs) {
        if (p.getChunkMetaData("type").equals("title")) {
          for (final Sentence s : p.getSentences()) {
            for (final Token t : s.getTokens()) {
              final String lemma = t.getAttributeValue("base");
              titleBases.put(lemma, 1);
            }
          }
        }
      }
    }
    if (titleBases.containsKey(base)) {
      return titleBases.get(base);
    }
    return 0;
  }
}
