package g419.corpus.structure;

import java.util.*;


/**
 * Klaster dla opakowania zbioru relacji, które są przechodnie
 * tj. relacja zachodzi pomiędzy każdą dowolną parą anotacji w zbiorze
 * <p>
 * Opakowuje anotacje dla pojedynczej encji
 *
 * @author Adam Kaczmarek
 */
public class AnnotationCluster {
  private Document document;
  private final SortedSet<Annotation> annotations;
  private final String type;
  private final String set;
  private Annotation headAnnotation;
  private final ReheadingStrategy defaultReheadStrategy = new ReheadToFirst();
  private final ReturningStrategy defaultReturningStrategy = new ReturnRelationsToPredecessor();

  public AnnotationCluster(final String type, final String set) {
    annotations = new TreeSet<>(new AnnotationPositionComparator());
    this.type = type;
    this.set = set;
  }

  public String getType() {
    return type;
  }

  public String getSet() {
    return set;
  }

  public Document getDocument() {
    return document;
  }

  public AnnotationCluster getFilteredCluster(final List<Annotation> removeMentions) {
    final AnnotationCluster filteredCluster = new AnnotationCluster(type, set);
    filteredCluster.setDocument(document);

    for (final Annotation annotation : getAnnotations()) {
      if (!removeMentions.contains(annotation)) {
        filteredCluster.addAnnotation(annotation);
      }
    }

    return filteredCluster;
  }

  public AnnotationCluster getPreceedingCluster(final Annotation mention, final List<Annotation> selectedAnnotations) {
    final AnnotationPositionComparator comparator = new AnnotationPositionComparator();
    final AnnotationCluster preceedingCluster = new AnnotationCluster(type, set);
    preceedingCluster.setDocument(document);

    for (final Annotation annotation : getAnnotations()) {
      // Sprawdź czy anotacja występuje przed wzmianką -- dodatkowo usuwaj tylko wzmianki tego samego typu
      // Zakładamy bowiem, że wzmianki innych typów pochodzą z wcześniejszych etapów klasyfikacji
      if (selectedAnnotations.contains(annotation)) {
        // Jeśli anotacja jest tego samego typu co wzmianka, to sprawdź pozycję
        if (comparator.compare(annotation, mention) < 0) {
          preceedingCluster.addAnnotation(annotation);
        }
      } else {
        // W p. p. dodaj wzmiankę do klastra
        preceedingCluster.addAnnotation(annotation);
      }
    }

    return preceedingCluster;
  }

  public void addRelation(final Relation relation) {
    final Annotation source = relation.getAnnotationFrom();
    final Annotation target = relation.getAnnotationTo();

    annotations.add(target);
    annotations.add(source);

    document = relation.getDocument();
  }

  public void addAnnotation(final Annotation annotation) {
    annotations.add(annotation);
  }

  public SortedSet<Annotation> getAnnotations() {
    return annotations;
  }

  public Annotation getHead() {
    if (headAnnotation == null) {
      rehead(defaultReheadStrategy);
    }
    return headAnnotation;
  }

  public void rehead(final ReheadingStrategy s) {
    headAnnotation = s.rehead(annotations);
  }

  public Set<Relation> getRelations() {
    return defaultReturningStrategy.returnRelations(getHead(), annotations, type, set, document);
  }

  public Set<Relation> getRelations(final ReturningStrategy strategy) {
    if (strategy == null) {
      return getRelations();
    }
    return strategy.returnRelations(getHead(), annotations, type, set, document);
  }

  public Set<Relation> getRelationsToHead() {
    return getRelations(new ReturnRelationsToHead());
  }

  public Set<Relation> getRelationsToPredecessor() {
    return getRelations(new ReturnRelationsToPredecessor());
  }


  @Override
  public String toString() {
    String out = headAnnotation != null ? "{" + headAnnotation.getText() + "(H)}" : "";
    for (final Annotation annotation : annotations) {
      if (headAnnotation == null || !annotation.equals(headAnnotation)) {
        out += "{" + annotation + "}";
      }
    }

    return "[" + out + "]";

  }

  //--------------------------- INNER CLASSES AND INTERFACES ------------------------------------- //

  /**
   * Interfejs strategii przypisywania głowy dla klastra
   *
   * @author Adam Kaczmarek
   */
  public static interface ReheadingStrategy {
    Annotation rehead(SortedSet<Annotation> annotationSet);
  }

  public static class ReheadToFirst implements ReheadingStrategy {

    @Override
    public Annotation rehead(final SortedSet<Annotation> annotationSet) {
      return annotationSet.first();
    }

  }

  public static class ReheadToFirstProperName implements ReheadingStrategy {

    @Override
    public Annotation rehead(final SortedSet<Annotation> annotationSet) {

      for (final Annotation currentAnnotation : annotationSet) {
        if (currentAnnotation.getType().endsWith("nam")) {
          return currentAnnotation;
        }
      }
      return null;
    }

  }


  /**
   * Interfejs strategii zwracania relacji w klastrze
   *
   * @author Adam Kaczmarek
   */
  public static interface ReturningStrategy {
    Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType, String relationSet, Document relDocument);
  }

  /**
   * Strategia zwracania relacji "do głowy"
   * Dla każdej anotacji w klastrze tworzona jest relacja z tej anotacji do głowy klastra
   *
   * @author Adam Kaczmarek
   */
  public static class ReturnRelationsToHead implements ReturningStrategy {

    @Override
    public Set<Relation> returnRelations(final Annotation headAnnotation, final SortedSet<Annotation> annotationSet, final String relationType, final String relationSet, final Document relDocument) {
      final Set<Relation> relationsToHead = new HashSet<>();
      for (final Annotation ann : annotationSet) {
        if (!ann.equals(headAnnotation)) {
          relationsToHead.add(new Relation(ann, headAnnotation, relationType, relationSet, relDocument));
        }
      }
      return relationsToHead;
    }
  }

  public static class ReturnRelationsToPredecessor implements ReturningStrategy {

    @Override
    public Set<Relation> returnRelations(final Annotation headAnnotation, final SortedSet<Annotation> annotationSet, final String relationType, final String relationSet, final Document relDocument) {
      final Set<Relation> relationsToPredecessor = new HashSet<>();
      Annotation predecessor = null;
      for (final Annotation ann : annotationSet) {
        if (predecessor != null) {
          relationsToPredecessor.add(new Relation(ann, predecessor, relationType, relationSet, relDocument));
        }
        predecessor = ann;
      }

      return relationsToPredecessor;
    }

  }

  public static class ReturnRelationsToDistinctEntities implements ReturningStrategy {

    private final Map<Annotation, Integer> mentionEntityMapping;
    private final Set<Annotation> entities;
    private final Set<Annotation> references;
//		private int numEntities;

    public ReturnRelationsToDistinctEntities(final Set<Annotation> entities, final Set<Annotation> references, final Map<Annotation, Integer> mapping) {
      mentionEntityMapping = mapping;
      this.entities = entities;
      this.references = references;
//			this.numEntities = numEntities;
    }


    @Override
    public Set<Relation> returnRelations(final Annotation headAnnotation, final SortedSet<Annotation> annotationSet, final String relationType, final String relationSet, final Document relDocument) {
      final Set<Relation> relationsToEntities = new HashSet<>();
      final Set<Integer> entitiesFound = new HashSet<>();
//			boolean[] entitiesFound = new boolean[this.numEntities];
      final Set<Annotation> distinctEntityAnnotations = new HashSet<>();
      final Set<Annotation> referenceAnnotations = new HashSet<>();

      for (final Annotation annotation : annotationSet) {
        if (entities.contains(annotation)) {
          if (!entitiesFound.contains(mentionEntityMapping.get(annotation))) {
            distinctEntityAnnotations.add(annotation);
            entitiesFound.add(mentionEntityMapping.get(annotation));
          }
        } else if (references.contains(annotation)) {
          referenceAnnotations.add(annotation);
        }
      }

      for (final Annotation entityAnnotation : distinctEntityAnnotations) {
        for (final Annotation referenceAnnotation : referenceAnnotations) {
          relationsToEntities.add(new Relation(referenceAnnotation, entityAnnotation, Relation.COREFERENCE));
        }
      }

      return relationsToEntities;
    }
  }

  public void setDocument(final Document document2) {
    document = document2;
  }

  public void removeAnnotations(final List<Annotation> toRemove) {
    annotations.removeAll(toRemove);
  }
}
