package g419.crete.core.classifier.factory.item;

import g419.crete.core.classifier.AbstractCreteClassifier;

import java.util.List;

public interface IClassifierFactoryItem<M, I, L> {
	public AbstractCreteClassifier<M, I, L> createClassifier(List<String> features);
}
