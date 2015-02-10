package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.ClusterRankingInstance;
import g419.crete.api.instance.ClusterRankingTrainingDiffInstance;

import java.util.ArrayList;
import java.util.List;

public class ClusterRankingTrainingInstanceGenerator extends AbstractCreteInstanceGenerator<ClusterRankingTrainingDiffInstance, Integer>{
	

	@Override
	public List<ClusterRankingTrainingDiffInstance> generateInstances(Document document, AbstractAnnotationSelector selector) {
		ArrayList<ClusterRankingTrainingDiffInstance> trainingInstances = new ArrayList<ClusterRankingTrainingDiffInstance>();
		
		for(Annotation mention : selector.selectAnnotations(document))
			trainingInstances.addAll(generateInstancesForMention(document, mention));
		
		return trainingInstances;
	}

	private List<ClusterRankingTrainingDiffInstance> generateInstancesForMention(Document document, Annotation mention){
		ArrayList<ClusterRankingTrainingDiffInstance> mentionInstances = new ArrayList<ClusterRankingTrainingDiffInstance>();
		ArrayList<ClusterRankingInstance> intermediateInstances = new ArrayList<ClusterRankingInstance>();

		for(AnnotationCluster cluster : AnnotationClusterSet.fromRelationSet(document.getRelations(Relation.COREFERENCE)).getClusters()){
			AnnotationCluster preceedingCluster = cluster.clusterPreceedingMention(mention);
			if(preceedingCluster.getAnnotations().size() > 0){
				Integer label = cluster.getAnnotations().contains(mention) ? 2 : 1;
				intermediateInstances.add(new ClusterRankingInstance(mention, cluster, label, null));
			}
		}
		
		for(ClusterRankingInstance instance1 : intermediateInstances)
			for(ClusterRankingInstance instance2 : intermediateInstances)
				if(instance1.getLabel() != instance2.getLabel())
					mentionInstances.add(new ClusterRankingTrainingDiffInstance(mention, instance1.getCluster().getHolder(), instance2.getCluster().getHolder(), instance1.getLabel() - instance2.getLabel(), featureNames));
		
		return mentionInstances;
	}

}
