package g419.corpus.structure;

import g419.corpus.structure.AnnotationCluster.ReturningStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Zbiór klastrów anotacji będących w relacji przechodniej.
 * @author Adam Kaczmarek
 *
 * Opisuje relacje na poziomie dokumentu
 */

// TODO: przerobić na automatyczne tworzenie klastrów singletonowych
public class AnnotationClusterSet {

	private String relationType;
	private String relationSet;
	private Map<Annotation, AnnotationCluster> annotationInCluster;
	private Set<AnnotationCluster> relationClusters;
	
	public AnnotationClusterSet(){
		this.annotationInCluster = new HashMap<Annotation, AnnotationCluster>();
		this.relationClusters = new HashSet<AnnotationCluster>();
	}
	
	public void addRelationCluster(AnnotationCluster cluster){
		this.relationType = cluster.getType();
		this.relationSet = cluster.getSet();
		
		this.relationClusters.add(cluster);
		for(Annotation annotation : cluster.getAnnotations())
			this.annotationInCluster.put(annotation, cluster);
	}
	
	public void addRelation(Relation r){
		this.relationType = r.getType();
		this.relationSet = r.getSet();
		
		AnnotationCluster cluster = getCreateCluster(r.getAnnotationFrom(), r.getAnnotationTo());
		cluster.addRelation(r);
		this.annotationInCluster.put(r.getAnnotationFrom(), cluster);
		this.annotationInCluster.put(r.getAnnotationTo(), cluster);
		this.relationClusters.add(cluster);
	}
	
	private AnnotationCluster getCreateCluster(Annotation ann1, Annotation ann2){
		AnnotationCluster cluster1 = annotationInCluster.get(ann1);
		AnnotationCluster cluster2 = annotationInCluster.get(ann2);
		
		if(cluster1 != null && cluster2 == null) return cluster1;
		if(cluster1 == null && cluster2 != null) return cluster2;
		
		if(cluster1 == null && cluster2 == null) return new AnnotationCluster(this.relationType, this.relationSet);
		
		for(Annotation annotation : cluster2.getAnnotations()){
			this.annotationInCluster.remove(annotation);
			this.annotationInCluster.put(annotation, cluster1);
			cluster1.addAnnotation(annotation);
		}
		this.relationClusters.remove(cluster2);
		
		return cluster1;		
	}
	
	public RelationSet getRelationSet(ReturningStrategy strategy){
		RelationSet relationSet = new RelationSet();
		for(AnnotationCluster cluster : this.relationClusters){
			for(Relation relation : cluster.getRelations(strategy)){
				relationSet.addRelation(relation);
			}
		}
		return relationSet;
	}
	
	public Set<AnnotationCluster> getClusters(){
		return this.relationClusters;
	}

	public boolean inSameCluster(Annotation ann1, Annotation ann2){
		AnnotationCluster cluster1 = annotationInCluster.get(ann1);
		AnnotationCluster cluster2 = annotationInCluster.get(ann2);
		
		if(cluster1 != null  && cluster2 != null) return cluster1.equals(cluster2);
		return false;
	}

	private AnnotationCluster getDefaultSingletonCluster(Annotation singletonMention){
		AnnotationCluster cluster = new AnnotationCluster(this.relationType, this.relationSet);
		cluster.addAnnotation(singletonMention);
		return cluster;
	}

	public AnnotationCluster getClusterWithAnnotation(Annotation annotation){
		return annotationInCluster.getOrDefault(annotation, getDefaultSingletonCluster(annotation));
	}

	public Set<AnnotationCluster> getClustersWithAnnotations(Set<Annotation> annotations){
		Set<AnnotationCluster> clustersWithAnnotations = new HashSet<AnnotationCluster>();
		for(Annotation annotation: annotations)
			clustersWithAnnotations.add(annotationInCluster.get(annotation));
		
		return clustersWithAnnotations;
	}

	public static AnnotationClusterSet fromRelationSet(RelationSet relations) {
		AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();
		
		for(Relation relation: relations.getRelations())
			relationClusterSet.addRelation(relation);
		
		return relationClusterSet;
	}

	//TODO: move AbstractAnnotationSelector to g419-corpus
	public static AnnotationClusterSet fromRelationSet(RelationSet relations, List<Annotation> singletons) {
		AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();

		for(Relation relation: relations.getRelations())
			relationClusterSet.addRelation(relation);

		for(Annotation singleton : singletons)
			if(!relationClusterSet.annotationInCluster.containsKey(singleton))
				relationClusterSet.addRelationCluster(relationClusterSet.getDefaultSingletonCluster(singleton));

		return relationClusterSet;
	}

	/**
	 *  Method for creating AnnotationClusterSet for all given relations 
	 *  and including all given mentions - mentions that are not in any
	 *  relation are added to resulting set of clusters as singleton clusters
	 *  consisting of only one annotation
	 * @param relations Set of considered relations
	 * @return Set of relational clusters for all given annotations based on passed relations
	 */
	public static AnnotationClusterSet fromRelationSetWithSingletons(Document document, String type, String set, RelationSet relations, List<Annotation> singletonAnnotations){
		AnnotationClusterSet relationClusterSet = new AnnotationClusterSet();
		List<Annotation> singletons = new ArrayList<Annotation>(singletonAnnotations);
		
		// Relation Type and Set for setting properties of singleton relation clusters
//		String type = "";
//		String set="";
		
		// Add every non-singleton cluster to set of resulting clusters
		for(Relation relation: relations.getRelations()){
			// Extract Type and Set properties' values
			if("".equals(type) || "".equals(set)){ type = relation.getType(); set = relation.getSet();}
			// Add current relation to resulting set of clusters
			relationClusterSet.addRelation(relation);
			// Remove mentions from potential singletons list
			singletons.remove(relation.getAnnotationFrom());
			singletons.remove(relation.getAnnotationTo());
		}
		
		// Add all remaining mentions which are singletons
		for(Annotation singleton : singletons){
			// Create new singleton cluster
			AnnotationCluster cluster = new AnnotationCluster(type, set);
			// Add singleton mention
			cluster.addAnnotation(singleton);
			// Assign document to cluster
			cluster.setDocument(document);
			// Add cluster to resulting set of clusters
			relationClusterSet.addRelationCluster(cluster);
		}
		
		return relationClusterSet;
	}
	
	public String toString(){
		String out = "{";
		for(AnnotationCluster cluster : relationClusters)
			out += cluster +"\n";
		out += "}";
		return out;
	}

	public void removeAnnotations(List<Annotation> toRemove) {
		for(AnnotationCluster cluster : relationClusters)
			cluster.removeAnnotations(toRemove);
		
	}
}
