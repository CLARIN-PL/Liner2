package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.MentionPairClassificationInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MentionPairInstanceGenerator extends AbstractCreteInstanceGenerator<MentionPairClassificationInstance, Integer> {

	public static final Integer POSITIVE_LABEL = 1;
	public static final Integer NEGATIVE_LABEL = -1;
	
	private final boolean training;
	
	public MentionPairInstanceGenerator(boolean train){
		this.training = train;
	}
	
	@Override
	public List<MentionPairClassificationInstance> generateInstances(Document document, AbstractAnnotationSelector mentionSelector, 	AbstractAnnotationSelector singletonSelector) {
		List<MentionPairClassificationInstance> instances = new ArrayList<>();
		for(Annotation mention : mentionSelector.selectAnnotations(document))
			instances.addAll(generateInstancesForMention(document, mention, null, singletonSelector.selectAnnotations(document)));
		
		return instances;
	}

	@Override
	public List<MentionPairClassificationInstance> generateInstancesForMention(Document document, Annotation mention, 	List<Annotation> allMentionsClassified, List<Annotation> singletons) {
		AnnotationPositionComparator comparator = new AnnotationPositionComparator();
		AnnotationClusterSet clusters = AnnotationClusterSet.fromRelationSet(document.getRelations());
		
		List<MentionPairClassificationInstance> negativeInstances  = document.getAnnotations()
				.parallelStream()
				.filter(annotation -> comparator.compare(annotation, mention) < 0)
				.filter(annotation -> !clusters.inSameCluster(mention, annotation))
				.sorted(comparator)
				// Apply limit only to training data
				.limit(training ? 3 : 1000)
				.map(antecedent -> 
					new MentionPairClassificationInstance(
						mention, 
						antecedent, 
						NEGATIVE_LABEL, 
						this.featureNames
					)
				)
				.collect(Collectors.toList());
		
		List<MentionPairClassificationInstance> positiveInstances = document.getAnnotations()
				.parallelStream()
				.filter(annotation -> comparator.compare(annotation, mention) < 0)
				.filter(annotation -> clusters.inSameCluster(mention, annotation))
				.sorted(comparator)
				// Apply limit only to training data
				.limit(training ? 1: 1000)
				.map(antecedent -> 
					new MentionPairClassificationInstance(
						mention, 
						antecedent, 
						POSITIVE_LABEL, 
						this.featureNames
					)
				)
				.collect(Collectors.toList());
		
		List<MentionPairClassificationInstance> instances = new ArrayList<>();
		instances.addAll(positiveInstances);
		instances.addAll(negativeInstances);
		return instances;
		
//		negativeInstances.addAll(positiveInstances);
//		return negativeInstances; 
	}

	

}
