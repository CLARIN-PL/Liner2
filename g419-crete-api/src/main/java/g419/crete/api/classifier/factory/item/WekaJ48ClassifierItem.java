package g419.crete.api.classifier.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaDecisionTreesClassifier;

import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaJ48ClassifierItem implements IClassifierFactoryItem<Classifier, Instance, Double>{

	@Override
	public AbstractCreteClassifier<Classifier, Instance, Double> createClassifier(List<String> features) {
		return new WekaDecisionTreesClassifier(features);
	}

}
