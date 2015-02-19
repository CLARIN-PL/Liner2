package g419.corpus.structure;

import g419.corpus.structure.AnnotationCluster.ReturningStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Zbiór klastrów anotacji będących w relacji przechodniej.
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 * Opisuje relacje na poziomie dokumentu
 */

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
