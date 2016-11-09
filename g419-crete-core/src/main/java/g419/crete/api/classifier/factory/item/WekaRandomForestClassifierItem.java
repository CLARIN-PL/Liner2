package g419.crete.api.classifier.factory.item;

import g419.crete.api.CreteOptions;
import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaDecisionTreesClassifier;
import g419.crete.api.classifier.factory.WekaFilterFactory;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.filters.Filter;

import java.util.List;

public class WekaRandomForestClassifierItem implements IClassifierFactoryItem<Classifier, Instance, Double>{

	public static final String FILTER = "filter";

	@Override
	public AbstractCreteClassifier<Classifier, Instance, Double> createClassifier(List<String> features) {
		Filter resampler = WekaFilterFactory.getFactory().getFilter(CreteOptions.getOptions().getProperties().getProperty(FILTER));
		return new WekaDecisionTreesClassifier(features, resampler);
	}

}
