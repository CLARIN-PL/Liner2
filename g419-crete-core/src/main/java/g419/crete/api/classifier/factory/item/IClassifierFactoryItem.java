package g419.crete.api.classifier.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;

import java.util.List;

public interface IClassifierFactoryItem<M, I, L> {
	public AbstractCreteClassifier<M, I, L> createClassifier(List<String> features);
}
