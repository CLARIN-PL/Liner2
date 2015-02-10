package g419.crete.api.classifier;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;

import java.util.List;

public abstract class AbstractCreteResolver<M, T extends AbstractCreteInstance<L>, I, L> {

	protected AbstractCreteClassifier<M, I, L> classifier;
	protected AbstractCreteInstanceGenerator<T, L> generator;
	protected AbstractCreteInstanceConverter<T, I> converter;
	
	public void setUp(AbstractCreteClassifier<M, I, L> clas, AbstractCreteInstanceGenerator<T, L> gen, AbstractCreteInstanceConverter<T, I> conv){
		this.classifier = clas;
		this.generator = gen;
		this.converter = conv;
	}
	
	public AnnotationClusterSet resolveDocument(Document document, List<Annotation> mentions){
		
		for(Annotation mention : mentions) document = resolveMention(document, mention);
		
		return AnnotationClusterSet.fromRelationSet(document.getRelations());
	}
	
	
	
	//-------------------------- Abstract methods -------------------------
	protected abstract  List<T> extractClassifiedAsCorrect(List<T> instances, List<L> labels);
	protected abstract Document resolveMention(Document document, Annotation mention);
	
	//-------------------------- Abstract Generic methods --------------
	public abstract Class<M> getModelClass();
	public abstract Class<T> getAbstractInstanceClass();
	public abstract Class<I> getClassifierInstanceClass();
	public abstract Class<L> getLabelClass();
}