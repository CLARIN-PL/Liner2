package g419.crete.core.classifier;

import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;

import java.util.List;


public class WekaDecisionTreesClassifier extends WekaClassifier{

	public WekaDecisionTreesClassifier(List<String> features, Filter resampler) {
		super(features, resampler);
		RandomForest rf = new RandomForest();
		rf.setNumTrees(200);
		rf.setNumFeatures(15);
		this.classifier = rf;
	}
}
