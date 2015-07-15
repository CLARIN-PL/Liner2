package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.ClusterClassificationInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClusterClassificationInstanceGenerator extends AbstractCreteInstanceGenerator<ClusterClassificationInstance, Integer>{

	public static final Integer POSITIVE_LABEL = 1;
	public static final Integer NEGATIVE_LABEL = -1;
	
	@Override
	public List<ClusterClassificationInstance> generateInstances(Document document, AbstractAnnotationSelector selector) {
		ArrayList<ClusterClassificationInstance> instances = new ArrayList<ClusterClassificationInstance>();
		List<Annotation> mentions = selector.selectAnnotations(document);
		Collections.sort(mentions, new AnnotationPositionComparator());
		
		for(Annotation mention : mentions)
			instances.addAll(generateInstancesForMention(document, mention, mentions));
		
		return instances;
	}
	
	
	@Override
	public List<ClusterClassificationInstance> generateInstancesForMention(Document document, Annotation mention, List<Annotation> mentions) {
		Set<AnnotationCluster> clusters = AnnotationClusterSet.fromRelationSet(document.getRelations(Relation.COREFERENCE)).getClusters();
		ArrayList<ClusterClassificationInstance> mentionInstances = new ArrayList<ClusterClassificationInstance>();
		for(AnnotationCluster cluster : clusters){
			AnnotationCluster preceedingCluster = cluster.getPreceedingCluster(mention, mentions); 
			if(preceedingCluster.getAnnotations().size() > 0){
				Integer label = cluster.getAnnotations().contains(mention) ? POSITIVE_LABEL : NEGATIVE_LABEL;
				mentionInstances.add(new ClusterClassificationInstance(mention, preceedingCluster, label, this.featureNames));
			}
		}
		
		return mentionInstances;
	}
		
}
