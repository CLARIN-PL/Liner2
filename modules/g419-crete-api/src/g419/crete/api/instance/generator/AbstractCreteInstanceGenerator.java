package g419.crete.api.instance.generator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.AbstractCreteInstance;

import java.util.List;

public abstract class AbstractCreteInstanceGenerator<T extends AbstractCreteInstance<LabelType>, LabelType> {
	protected List<String> featureNames;
	public abstract List<T> generateInstances(Document document, AbstractAnnotationSelector mentionSelector, AbstractAnnotationSelector singletonSelector);
	// TODO: extract to separate class like eg: AbstractMentionFocusedInstanceGenerator + implement generate instances using 
	// mention generation for each selected mention
	public abstract List<T> generateInstancesForMention(Document document, Annotation mention, List<Annotation> allMentionsClassified, List<Annotation> singletons);
	
	public void setFeatures(List<String> features){
		this.featureNames = features;
	}
}
