package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.ClusterRankingInstance;

import java.util.ArrayList;
import java.util.List;

public class ClusterClassificationInstanceGenerator extends AbstractCreteInstanceGenerator<ClusterClassificationInstance, Integer>{

	public static final Integer POSITIVE_LABEL = 1;
	public static final Integer NEGATIVE_LABEL = -1;
	
	@Override
	public List<ClusterClassificationInstance> generateInstances(Document document, AbstractAnnotationSelector selector) {
		ArrayList<ClusterClassificationInstance> instances = new ArrayList<ClusterClassificationInstance>();
		
		for(Annotation mention : selector.selectAnnotations(document)){
			ArrayList<ClusterClassificationInstance> mentionInstances = new ArrayList<ClusterClassificationInstance>();
			boolean hasOneNeg = false;
			for(AnnotationCluster cluster : AnnotationClusterSet.fromRelationSet(document.getRelations(Relation.COREFERENCE)).getClusters()){
				AnnotationCluster preceedingCluster = cluster.clusterPreceedingMention(mention);
				if(preceedingCluster.getAnnotations().size() > 0){
//					Integer label = cluster.getAnnotations().contains(mention) ? POSITIVE_LABEL : NEGATIVE_LABEL;
					if(cluster.getAnnotations().contains(mention)){
						mentionInstances.add(new ClusterClassificationInstance(mention, preceedingCluster, POSITIVE_LABEL, this.featureNames));
					}
					else if(!hasOneNeg){
						hasOneNeg = true;
						mentionInstances.add(new ClusterClassificationInstance(mention, preceedingCluster, NEGATIVE_LABEL, this.featureNames));
					}
				}
				
//				double negativeWeight = 1.0 / (mentionInstances.size() - 1);
//				for(ClusterClassificationInstance instance : mentionInstances) 
//					if(POSITIVE_LABEL.equals(instance.getLabel()))
//						instance.setWeight(negativeWeight);
//					else
//						instance.setWeight(1.0);
						
			}
			
			instances.addAll(mentionInstances);
		}
		
		
		return instances;
	}

	

}
