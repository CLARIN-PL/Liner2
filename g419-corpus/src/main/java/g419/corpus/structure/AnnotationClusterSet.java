package g419.corpus.structure;

import g419.corpus.structure.AnnotationCluster.ReturningStrategy;

import java.util.*;

/**
 * Zbiór klastrów anotacji będących w relacji przechodniej.
 *
 * @author Adam Kaczmarek
 * <p>
 * Opisuje relacje na poziomie dokumentu
 */

// TODO: przerobić na automatyczne tworzenie klastrów singletonowych
public class AnnotationClusterSet {

  private String relationType;
  private String relationSet;
  private final Map<Annotation, AnnotationCluster> annotationInCluster;
  private final Set<AnnotationCluster> relationClusters;

  public AnnotationClusterSet() {
    annotationInCluster = new HashMap<>();
    relationClusters = new HashSet<>();
  }

  public void addRelationCluster(final AnnotationCluster cluster) {
    relationType = cluster.getType();
    relationSet = cluster.getSet();

    relationClusters.add(cluster);
    for (final Annotation annotation : cluster.getAnnotations()) {
      annotationInCluster.put(annotation, cluster);
    }
  }

  public void addRelation(final Relation r) {
    relationType = r.getType();
    relationSet = r.getSet();

    final AnnotationCluster cluster = getCreateCluster(r.getAnnotationFrom(), r.getAnnotationTo());
    cluster.addRelation(r);
    annotationInCluster.put(r.getAnnotationFrom(), cluster);
    annotationInCluster.put(r.getAnnotationTo(), cluster);
    relationClusters.add(cluster);
  }

  private AnnotationCluster getCreateCluster(final Annotation ann1, final Annotation ann2) {
    final AnnotationCluster cluster1 = annotationInCluster.get(ann1);
    final AnnotationCluster cluster2 = annotationInCluster.get(ann2);

    if (cluster1 != null && cluster2 == null) {
      return cluster1;
    }
    if (cluster1 == null && cluster2 != null) {
      return cluster2;
    }

    if (cluster1 == null && cluster2 == null) {
      return new AnnotationCluster(relationType, relationSet);
    }

    for (final Annotation annotation : cluster2.getAnnotations()) {
      annotationInCluster.remove(annotation);
      annotationInCluster.put(annotation, cluster1);
      cluster1.addAnnotation(annotation);
    }
    relationClusters.remove(cluster2);

    return cluster1;
  }

  public RelationSet getRelationSet(final ReturningStrategy strategy) {
    final RelationSet relationSet = new RelationSet();
    for (final AnnotationCluster cluster : relationClusters) {
      for (final Relation relation : cluster.getRelations(strategy)) {
        relationSet.addRelation(relation);
      }
    }
    return relationSet;
  }

  public Set<AnnotationCluster> getClusters() {
    return relationClusters;
  }

  public boolean inSameCluster(final Annotation ann1, final Annotation ann2) {
    final AnnotationCluster cluster1 = annotationInCluster.get(ann1);
    final AnnotationCluster cluster2 = annotationInCluster.get(ann2);

    if (cluster1 != null && cluster2 != null) {
      return cluster1.equals(cluster2);
    }
    return false;
  }

  private AnnotationCluster getDefaultSingletonCluster(final Annotation singletonMention) {
    final AnnotationCluster cluster = new AnnotationCluster(relationType, relationSet);
    cluster.addAnnotation(singletonMention);
    return cluster;
  }

  public AnnotationCluster getClusterWithAnnotation(final Annotation annotation) {
    return annotationInCluster.getOrDefault(annotation, getDefaultSingletonCluster(annotation));
  }

  public Set<AnnotationCluster> getClustersWithAnnotations(final Set<Annotation> annotations) {
    final Set<AnnotationCluster> clustersWithAnnotations = new HashSet<>();
    for (final Annotation annotation : annotations) {
      clustersWithAnnotations.add(annotationInCluster.get(annotation));
    }

    return clustersWithAnnotations;
  }

  public static AnnotationClusterSet fromRelationSet(final RelationSet relations) {
    final AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();

    for (final Relation relation : relations.getRelations()) {
      relationClusterSet.addRelation(relation);
    }

    return relationClusterSet;
  }

  //TODO: move AbstractAnnotationSelector to g419-corpus
  public static AnnotationClusterSet fromRelationSet(final RelationSet relations, final List<Annotation> singletons) {
    final AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();

    for (final Relation relation : relations.getRelations()) {
      relationClusterSet.addRelation(relation);
    }

    for (final Annotation singleton : singletons) {
      if (!relationClusterSet.annotationInCluster.containsKey(singleton)) {
        relationClusterSet.addRelationCluster(relationClusterSet.getDefaultSingletonCluster(singleton));
      }
    }

    return relationClusterSet;
  }

  /**
   * Method for creating AnnotationClusterSet for all given relations
   * and including all given mentions - mentions that are not in any
   * relation are added to resulting set of clusters as singleton clusters
   * consisting of only one annotation
   *
   * @param relations Set of considered relations
   * @return Set of relational clusters for all given annotations based on passed relations
   */
  public static AnnotationClusterSet fromRelationSetWithSingletons(final Document document, String type, String set, final RelationSet relations, final List<Annotation> singletonAnnotations) {
    final AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();
    final List<Annotation> singletons = new ArrayList<>(singletonAnnotations);

    // Relation Type and Set for setting properties of singleton relation clusters
//		String type = "";
//		String set="";

    // Add every non-singleton cluster to set of resulting clusters
    for (final Relation relation : relations.getRelations()) {
      // Extract Type and Set properties' values
      if ("".equals(type) || "".equals(set)) {
        type = relation.getType();
        set = relation.getSet();
      }
      // Add current relation to resulting set of clusters
      relationClusterSet.addRelation(relation);
      // Remove mentions from potential singletons list
      singletons.remove(relation.getAnnotationFrom());
      singletons.remove(relation.getAnnotationTo());
    }

    // Add all remaining mentions which are singletons
    for (final Annotation singleton : singletons) {
      // Create new singleton cluster
      final AnnotationCluster cluster = new AnnotationCluster(type, set);
      // Add singleton mention
      cluster.addAnnotation(singleton);
      // Assign document to cluster
      cluster.setDocument(document);
      // Add cluster to resulting set of clusters
      relationClusterSet.addRelationCluster(cluster);
    }

    return relationClusterSet;
  }

  @Override
  public String toString() {
    String out = "{";
    for (final AnnotationCluster cluster : relationClusters) {
      out += cluster + "\n";
    }
    out += "}";
    return out;
  }

  public void removeAnnotations(final List<Annotation> toRemove) {
    for (final AnnotationCluster cluster : relationClusters) {
      cluster.removeAnnotations(toRemove);
    }

  }
}
