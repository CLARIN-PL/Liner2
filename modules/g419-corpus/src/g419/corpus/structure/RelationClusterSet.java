package g419.corpus.structure;

import g419.corpus.structure.RelationCluster.ReturningStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Zbiór klastrów anotacji będących w relacji przechodniej.
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 * Opisuje relacje na poziomie dokumentu
 */
public class RelationClusterSet {

	private String relationType;
	private Map<Annotation, RelationCluster> annotationInCluster;
	private Set<RelationCluster> relationClusters;
	
	public RelationClusterSet(){
		this.annotationInCluster = new HashMap<Annotation, RelationCluster>();
		this.relationClusters = new HashSet<RelationCluster>();
	}
	
	public void addRelationCluster(RelationCluster cluster){
		this.relationClusters.add(cluster);
		for(Annotation annotation : cluster.getAnnotations())
			this.annotationInCluster.put(annotation, cluster);
	}
	
	public void addRelation(Relation r){
		RelationCluster cluster = getCreateCluster(r.getAnnotationFrom(), r.getAnnotationTo());
		cluster.addRelation(r); 
		this.annotationInCluster.put(r.getAnnotationFrom(), cluster);
		this.annotationInCluster.put(r.getAnnotationTo(), cluster);
		this.relationClusters.add(cluster);
	}
	
	private RelationCluster getCreateCluster(Annotation ann1, Annotation ann2){
		RelationCluster cluster1 = annotationInCluster.get(ann1);
		RelationCluster cluster2 = annotationInCluster.get(ann2);
		
		if(cluster1 != null && cluster2 == null) return cluster1;
		if(cluster1 == null && cluster2 != null) return cluster2;
		
		if(cluster1 == null && cluster2 == null) return new RelationCluster(this.relationType);
		
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
		for(RelationCluster cluster : this.relationClusters){
			for(Relation relation : cluster.getRelations(strategy)){
				relationSet.addRelation(relation);
			}
		}
		return relationSet;
	}
	
	public Set<RelationCluster> getClusters(){
		return this.relationClusters;
	}

	public Set<RelationCluster> getClustersWithAnnotations(Set<Annotation> annotations){
		Set<RelationCluster> clustersWithAnnotations = new HashSet<RelationCluster>();
		for(Annotation annotation: annotations)
			clustersWithAnnotations.add(annotationInCluster.get(annotation));
		
		return clustersWithAnnotations;
	}

	public static RelationClusterSet fromRelationSet(RelationSet relations) {
		RelationClusterSet relationClusterSet = new RelationClusterSet();
		
		for(Relation relation: relations.getRelations())
			relationClusterSet.addRelation(relation);
		
		return relationClusterSet;
	}
	
	public String toString(){
		String out = "{";
		for(RelationCluster cluster : relationClusters)
			out += cluster +"\n";
		out += "}";
		return out;
	}
}
