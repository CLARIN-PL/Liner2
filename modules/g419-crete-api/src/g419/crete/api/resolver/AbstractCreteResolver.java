package g419.crete.api.resolver;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.model.Model;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;

import java.util.Collections;
import java.util.List;


// TODO: change to AbstractCreteMentionFocusedResolver
public abstract class AbstractCreteResolver<M, T extends AbstractCreteInstance<L>, I, L> {

	protected AbstractCreteClassifier<M, I, L> classifier;
	protected AbstractCreteInstanceGenerator<T, L> generator;
	protected AbstractCreteInstanceConverter<T, I> converter;
	
	public void setUp(AbstractCreteClassifier<M, I, L> clas, AbstractCreteInstanceGenerator<T, L> gen, AbstractCreteInstanceConverter<T, I> conv){
		this.classifier = clas;
		this.generator = gen;
		this.converter = conv;
	}
	
	public AnnotationClusterSet resolveDocument(Document document, AbstractAnnotationSelector selector){
		List<Annotation> mentions = selector.selectAnnotations(document);
	   Collections.sort(mentions, new AnnotationPositionComparator());
		
		for(Annotation mention : mentions){
			List<T> instancesForMention = this.generator.generateInstancesForMention(document, mention, mentions);
			document = resolveMention(document, mention, instancesForMention);
		}
		
		return AnnotationClusterSet.fromRelationSet(document.getRelations());
	}
	
	
	public void loadModel(Model<M> model){
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