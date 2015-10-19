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
import java.util.stream.Collectors;

public class ClusterClassificationInstanceGenerator extends AbstractCreteInstanceGenerator<ClusterClassificationInstance, Integer>{

	public static final Integer POSITIVE_LABEL = 1;
	public static final Integer NEGATIVE_LABEL = -1;
	
	public final int negativeLimit;
	
	public ClusterClassificationInstanceGenerator() {
		this.negativeLimit = 100000;
	}
	
	public ClusterClassificationInstanceGenerator(int limit){
		this.negativeLimit = limit;
	}
	
	@Override
	public List<ClusterClassificationInstance> generateInstances(Document document, AbstractAnnotationSelector mentionSelector, AbstractAnnotationSelector singletonSelector) {
		ArrayList<ClusterClassificationInstance> instances = new ArrayList<ClusterClassificationInstance>();
		List<Annotation> mentions = mentionSelector.selectAnnotations(document);
		List<Annotation> singletons = singletonSelector.selectAnnotations(document);
		Collections.sort(mentions, new AnnotationPositionComparator());
		
		for(Annotation mention : mentions)
			instances.addAll(generateInstancesForMention(document, mention, mentions, singletons));
		
		return instances;
	}
	
	
	@Override
	public List<ClusterClassificationInstance> generateInstancesForMention(Document document, Annotation mention, List<Annotation> mentions, List<Annotation> singletons) {
		Set<AnnotationCluster> clusters = AnnotationClusterSet.fromRelationSetWithSingletons(document, Relation.COREFERENCE, Relation.COREFERENCE, document.getRelations(Relation.COREFERENCE), singletons).getClusters();
		ArrayList<ClusterClassificationInstance> mentionInstances = new ArrayList<ClusterClassificationInstance>();
		
//		List<ClusterClassificationInstance>  negativeInstances = clusters.parallelStream()
//				.filter(cluster -> !cluster.getAnnotations().contains(mention))
//				.map(cluster -> cluster.getPreceedingCluster(mention, mentions))
//				.map(preceedingCluster -> new ClusterClassificationInstance(mention, preceedingCluster, NEGATIVE_LABEL, this.featureNames))
//				.collect(Collectors.toList());
//		mentionInstances.addAll(negativeInstances);
//		
		int negatives = 0;
		for(AnnotationCluster cluster : clusters){
			AnnotationCluster preceedingCluster = cluster.getPreceedingCluster(mention, mentions); 
			if(preceedingCluster.getAnnotations().size() > 0){
				Integer label;
				if(cluster.getAnnotations().contains(mention)){
					label = POSITIVE_LABEL;
				}
				else{
					label = NEGATIVE_LABEL;
					negatives++;
					if(negatives > negativeLimit) continue;
				}
				mentionInstances.add(new ClusterClassificationInstance(mention, preceedingCluster, label, this.featureNames));
			}
		}
		
		
//		AnnotationCluster positiveCluster
		
		
		return mentionInstances;
	}
		
}
