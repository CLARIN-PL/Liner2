package g419.crete.api.instance.generator;

import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.instance.AbstractCreteInstance;

import java.util.List;

public abstract class AbstractCreteInstanceGenerator<T extends AbstractCreteInstance<LabelType>, LabelType> {
	protected List<String> featureNames;
	public abstract List<T> generateInstances(Document document, AbstractAnnotationSelector selector);
	
	public void setFeatures(List<String> features){
		this.featureNames = features;
	}
}
