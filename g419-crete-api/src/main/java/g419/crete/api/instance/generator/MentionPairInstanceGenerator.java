package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.instance.MentionPairClassificationInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MentionPairInstanceGenerator<L> extends AbstractCreteInstanceGenerator<MentionPairClassificationInstance<L>, L> {

	private final L POSITIVE_LABEL;
	private final L NEGATIVE_LABEL;
	
	private final boolean training;
	private final AbstractAnnotationSelector namedEntitySelector = AnnotationSelectorFactory.getFactory().getInitializedSelector("named_entity_selector");
	
	public MentionPairInstanceGenerator(L positiveLabel, L negativeLabel, boolean train){
		POSITIVE_LABEL = positiveLabel;
		NEGATIVE_LABEL = negativeLabel;
		this.training = false;
	}
	
	@Override
	public List<MentionPairClassificationInstance<L>> generateInstances(Document document, AbstractAnnotationSelector mentionSelector, 	AbstractAnnotationSelector singletonSelector) {
		List<MentionPairClassificationInstance<L>> instances = new ArrayList<>();
		for(Annotation mention : mentionSelector.selectAnnotations(document))
			instances.addAll(generateInstancesForMention(document, mention, null, singletonSelector.selectAnnotations(document)));
		
		return instances;
	}

	@Override
	public List<MentionPairClassificationInstance<L>> generateInstancesForMention(Document document, Annotation mention, 	List<Annotation> allMentionsClassified, List<Annotation> singletons) {
		AnnotationPositionComparator comparator = new AnnotationPositionComparator();
		AnnotationClusterSet clusters = AnnotationClusterSet.fromRelationSet(document.getRelations());
		
		List<MentionPairClassificationInstance<L>> negativeInstances  = namedEntitySelector.selectAnnotations(document)
//				document.getAnnotations()
				.parallelStream()
//				.filter(annotation -> comparator.compare(annotation, mention) < 0)
				.filter(annotation -> !annotation.equals(mention))
				.filter(annotation -> !clusters.inSameCluster(mention, annotation))
				.sorted(comparator)
				// Apply limit only to training data
				.limit(training ? 2: 1000)
				.map(antecedent -> 
					new MentionPairClassificationInstance<>(
						mention, 
						antecedent, 
						NEGATIVE_LABEL, 
						this.featureNames
					)
				)
				.collect(Collectors.toList());
		
		List<MentionPairClassificationInstance<L>> positiveInstances = namedEntitySelector.selectAnnotations(document)
//				document.getAnnotations()
				.parallelStream()
//				.filter(annotation -> training || comparator.compare(annotation, mention) < 0)
				.filter(annotation -> !annotation.equals(mention))
				.filter(annotation -> clusters.inSameCluster(mention, annotation))
				.sorted(comparator)
				// Apply limit only to training data
				.limit(training ? 10: 1000)
				.map(antecedent -> 
					new MentionPairClassificationInstance<>(
						mention, 
						antecedent, 
						POSITIVE_LABEL, 
						this.featureNames
					)
				)
				.collect(Collectors.toList());
		
		if(positiveInstances.size() > 0){
			int x = 0;
			int y = x;
		}
		
		List<MentionPairClassificationInstance<L>> instances = new ArrayList<>();
		instances.addAll(positiveInstances);
		instances.addAll(negativeInstances);






		return instances;
		
//		negativeInstances.addAll(positiveInstances);
//		return negativeInstances; 
	}

	

}
