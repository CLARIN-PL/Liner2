package g419.crete.core.resolver;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.classifier.serialization.Serializer;
import g419.crete.core.instance.AbstractCreteInstance;
import g419.crete.core.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.core.instance.generator.AbstractCreteInstanceGenerator;

import java.util.Collections;
import java.util.List;


// TODO: change to AbstractCreteMentionFocusedResolver
public abstract class AbstractCreteResolver<M, T extends AbstractCreteInstance<L>, I, L> {

	// TODO: Zostawić
	protected AbstractCreteClassifier<M, I, L> classifier;
	// TODO: Zostawić
	protected AbstractCreteInstanceGenerator<T, L> generator;
	// TODO: przenieść do Klasyfikatora !
	protected AbstractCreteInstanceConverter<T, I> converter;
	
	public void setUp(AbstractCreteClassifier<M, I, L> clas, AbstractCreteInstanceGenerator<T, L> gen, AbstractCreteInstanceConverter<T, I> conv){
		this.classifier = clas;
		this.generator = gen;
		this.converter = conv;
	}
	
	public Document resolveDocument(Document document, AbstractAnnotationSelector selector, AbstractAnnotationSelector singletonSelector){
		List<Annotation> mentions = selector.selectAnnotations(document);
//		System.out.println(mentions);
		List<Annotation> singletons = singletonSelector.selectAnnotations(document);
	   Collections.sort(mentions, new AnnotationPositionComparator());
		
		for(Annotation mention : mentions){
			List<T> instancesForMention = this.generator.generateInstancesForMention(document, mention, mentions, singletons);
			document = resolveMention(document, mention, instancesForMention);
		}
		
//		return AnnotationClusterSet.fromRelationSet(document.getRelations());
		return document;
	}
	
	
	public void loadModel(Serializer<M> model){
		this.classifier.setModel(model);
	}
	
	//-------------------------- Abstract methods -------------------------
//	protected abstract  List<T> extractClassifiedAsCorrect(List<T> instances, List<L> labels);
	protected abstract Document resolveMention(Document document, Annotation mention, List<T> instancesForMention);
	
	//-------------------------- Abstract Generic methods --------------
	public abstract Class<M> getModelClass();
	public abstract Class<T> getAbstractInstanceClass();
	public abstract Class<I> getClassifierInstanceClass();
	public abstract Class<L> getLabelClass();
}