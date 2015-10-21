package g419.crete.api.resolver;

import java.util.Collections;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.MentionPairClassificationInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaRandomForestMentionPairClusterClassifyResolver extends AbstractCreteResolver<Classifier, MentionPairClassificationInstance,  Instance, Integer> {

	@Override
	public Document resolveDocument(Document document, AbstractAnnotationSelector selector, AbstractAnnotationSelector singletonSelector){
		List<Annotation> mentions = selector.selectAnnotations(document);
		List<Annotation> singletons = singletonSelector.selectAnnotations(document);
		Collections.sort(mentions, new AnnotationPositionComparator());
		
//		for(Annotation mention : mentions)
//			List<MentionPairClassificationInstance> instancesForMention = this.generator.generateInstancesForMention(document, mention, mentions, singletons);
		   
			
		return document;
	}
	
	@Override
	protected Document resolveMention(Document document, Annotation mention, List<MentionPairClassificationInstance> instancesForMention) {
		System.err.println("Call to undefined function resolveMention in WekaRandomForestMentionPariClusterClassifyResolver");
		return null;
	}

	@Override
	public Class<Classifier> getModelClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<MentionPairClassificationInstance> getAbstractInstanceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Instance> getClassifierInstanceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Integer> getLabelClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
