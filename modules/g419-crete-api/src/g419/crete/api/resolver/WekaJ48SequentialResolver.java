package g419.crete.api.resolver;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.structure.AnnotationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48SequentialResolver extends AbstractCreteResolver<Classifier, ClusterClassificationInstance, Instance, Integer>{

	static class BestClosestClusterMentionComparator implements Comparator<ClusterClassificationInstance>{

		Document document;
		
		public BestClosestClusterMentionComparator(Document document) {
			this.document = document;
		}
		
		@Override
		public int compare(ClusterClassificationInstance clusterFirst, ClusterClassificationInstance clusterSecond) {
			Annotation m1 = clusterFirst.getMention().getHolder();
			Annotation m2 = clusterSecond.getMention().getHolder();
			// ASSERT m1 == m2
			AnnotationCluster ac1 = clusterFirst.getCluster().getHolder();
			AnnotationCluster ac2 = clusterSecond.getCluster().getHolder();
			
			Annotation ac1p = AnnotationUtil.getClosestPreceeding(m1, ac1);
			Annotation ac2p = AnnotationUtil.getClosestPreceeding(m2, ac2);
			
			int m1dist = ac1p != null ? AnnotationUtil.annotationTokenDistance(ac1p, m1, this.document) : 10000;
			int m2dist = ac2p != null ?AnnotationUtil.annotationTokenDistance(ac2p, m2, this.document) : 10000;
			
			
			return m1dist - m2dist;
		}
		
	}
	
	@Override
	protected Document resolveMention(Document document, Annotation mention, List<ClusterClassificationInstance> instancesForMention) {
		List<Integer> labels = this.classifier.classify(this.converter.convertInstances(instancesForMention));
		ArrayList<ClusterClassificationInstance> correctPairs = new ArrayList<ClusterClassificationInstance>();
		
		for(int i = 0; i < instancesForMention.size(); i++)
			// TODO: fixme
			if(labels.get(i) > 0)
				correctPairs.add(instancesForMention.get(i));
		
		if(instancesForMention.size() > 0) System.out.println(instancesForMention.get(0).getMention().getHolder());
		if(correctPairs.size() <= 0) return document; // Return unchanged document (mention does not have coreferential cluster)
		
		Collections.sort(correctPairs, new BestClosestClusterMentionComparator(document));
		AnnotationCluster bestCluster = correctPairs.get(0).getCluster().getHolder();
		 
		Relation mentionRelation = new Relation(mention, bestCluster.getHead(), Relation.COREFERENCE, Relation.COREFERENCE, document);
		document.addRelation(mentionRelation);
		return document;
	}
	
	@Override public Class<Classifier> getModelClass() {return Classifier.class;}
	@Override public Class<ClusterClassificationInstance> getAbstractInstanceClass() {return ClusterClassificationInstance.class;}
	@Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
	@Override public Class<Integer> getLabelClass() {return Integer.class;	}

}
