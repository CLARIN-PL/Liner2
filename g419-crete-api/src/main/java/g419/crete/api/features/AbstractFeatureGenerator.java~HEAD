//package g419.crete.api.features;
//
//import g419.crete.api.features.factory.FeatureFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public abstract class AbstractFeatureGenerator<InputType>{
//	
//	protected List<String> featureNames;
//	protected FeatureFactory featureFactory;
//	public AbstractFeatureGenerator(FeatureFactory featureFactory, List<String> featureNames){
//		this.featureNames = featureNames;
//	}
//	
//	@SuppressWarnings("unchecked")
//	public List<IFeature<InputType,?>> generateFeatures(InputType instance){
//		ArrayList<IFeature<InputType, ?>> generatedFeatures = new ArrayList<IFeature<InputType,?>>();
//		
//		for(String featureName : featureNames){
//			IFeature<InputType, ?> feature = (IFeature<InputType, ?>) featureFactory.getFeature(featureName);
//			feature.generateFeature(instance);
//			generatedFeatures.add(feature);
//		}
//		
//		return generatedFeatures;
//	}
//}
