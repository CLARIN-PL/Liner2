package g419.crete.api.features.factory;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.factory.item.AnnotationFirstVerbInSentenceItem;
import g419.crete.api.features.factory.item.AnnotationFollowingConjunctionByLikeItem;
import g419.crete.api.features.factory.item.AnnotationGenderFeatureFactoryItem;
import g419.crete.api.features.factory.item.AnnotationNumberFeatureFactoryItem;
import g419.crete.api.features.factory.item.AnnotationPersonFeatureFactoryItem;
import g419.crete.api.features.factory.item.AnnotationPreceedingConjunctionByLikeItem;
import g419.crete.api.features.factory.item.AnnotationPreceedingCoordinateConjunctionItem;
import g419.crete.api.features.factory.item.AnnotationPreceedingRelativePronounCaseItem;
import g419.crete.api.features.factory.item.AnnotationPreceedingRelativePronounDistanceItem;
import g419.crete.api.features.factory.item.AnnotationPreceedingSubordinateConjunctionItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingFollowedByCoordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingFollowedBySubordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingInSameSentenceFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingIsReflexivePossesiveItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionCaseItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionGenderItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionNumberItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionPersonItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingMentionTypeItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingPreceededByCoordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingPreceededBySubordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingPredicateDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestFollowingTokenDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingFollowedByCoordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingFollowedByRelativeItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingFollowedBySubordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingInSameSentenceFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingIsReflexivePossesiveItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionCaseItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionGenderItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionNumberItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionPersonItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionTypeItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingPreceededByCoordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingPreceededBySubordConjItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingPredicateDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingTokenDistanceItem;
import g419.crete.api.features.factory.item.ClusterMentionCountFeatureFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionGenderMatchFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionNumberMatchFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionPersonMatchFactoryItem;
import g419.crete.api.features.factory.item.ClusterSentenceFrequencyFeatureFactoryItem;
import g419.crete.api.features.factory.item.ClusterTermFrequencyFeatureFactoryItem;
import g419.crete.api.features.factory.item.IFeatureFactoryItem;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;



public class FeatureFactory{
	private static final String PREFIX_SEPARATOR = "_";
	
	private FeatureFactory(){
		prefixToClass = new HashMap<String, Class<?>>();
		prefixToClass.put("annotation", Annotation.class);
		prefixToClass.put("mention", Annotation.class);
		prefixToClass.put("cluster", AnnotationCluster.class);
		prefixToClass.put("clustermention", ((Pair<Annotation, AnnotationCluster>) new ImmutablePair<Annotation, AnnotationCluster>(null, null)).getClass());
		
		features = new HashMap<Pair<Class<?>,String>, IFeatureFactoryItem<?,?>>();
		// ----------------------- MENTION FEATURES ---------------------------
		register(new AnnotationGenderFeatureFactoryItem());
		register(new AnnotationNumberFeatureFactoryItem());
		register(new AnnotationPersonFeatureFactoryItem());
		register(new AnnotationFirstVerbInSentenceItem());
		register(new AnnotationPreceedingConjunctionByLikeItem());
		register(new AnnotationPreceedingCoordinateConjunctionItem());
		register(new AnnotationPreceedingRelativePronounCaseItem());
		register(new AnnotationPreceedingRelativePronounDistanceItem());
		register(new AnnotationPreceedingSubordinateConjunctionItem());
		register(new AnnotationFollowingConjunctionByLikeItem());
		
		// ----------------------- CLUSTER FEATURES ----------------------------
		register(new ClusterMentionCountFeatureFactoryItem());
		register(new ClusterTermFrequencyFeatureFactoryItem());
		register(new ClusterSentenceFrequencyFeatureFactoryItem());
		
		// ----------------------- MENTION_CLUSTER PAIR FEATURES -------
			// ------ Preceding ----
		register(new ClusterMentionClosestPreceedingTokenDistanceItem());
		register(new ClusterMentionClosestPreceedingInSameSentenceFactoryItem());
		register(new ClusterMentionClosestPreceedingMentionGenderItem());
		register(new ClusterMentionClosestPreceedingMentionNumberItem());
		register(new ClusterMentionClosestPreceedingMentionPersonItem());
		
		register(new ClusterMentionClosestPreceedingFollowedByCoordConjItem());
		register(new ClusterMentionClosestPreceedingPreceededByCoordConjItem());
		register(new ClusterMentionClosestPreceedingFollowedBySubordConjItem());
		register(new ClusterMentionClosestPreceedingPreceededBySubordConjItem());
		register(new ClusterMentionClosestPreceedingFollowedByRelativeItem());
		register(new ClusterMentionClosestPreceedingIsReflexivePossesiveItem());
		register(new ClusterMentionClosestPreceedingMentionCaseItem());
		register(new ClusterMentionClosestPreceedingMentionDistanceItem());
		register(new ClusterMentionClosestPreceedingMentionTypeItem());
		register(new ClusterMentionClosestPreceedingPredicateDistanceItem());
			// ------ Following----		
		register(new ClusterMentionClosestFollowingTokenDistanceItem());
		register(new ClusterMentionClosestFollowingInSameSentenceFactoryItem());
		register(new ClusterMentionClosestFollowingMentionGenderItem());
		register(new ClusterMentionClosestFollowingMentionNumberItem());
		register(new ClusterMentionClosestFollowingMentionPersonItem());
		
		register(new ClusterMentionClosestFollowingFollowedByCoordConjItem());
		register(new ClusterMentionClosestFollowingPreceededByCoordConjItem());
		register(new ClusterMentionClosestFollowingFollowedBySubordConjItem());
		register(new ClusterMentionClosestFollowingPreceededBySubordConjItem());
//		register(new ClusterMentionClosestFollowingFollowedByRelativeItem());
		register(new ClusterMentionClosestFollowingIsReflexivePossesiveItem());
		register(new ClusterMentionClosestFollowingMentionCaseItem());
		register(new ClusterMentionClosestFollowingMentionDistanceItem());
		register(new ClusterMentionClosestFollowingMentionTypeItem());
		register(new ClusterMentionClosestFollowingPredicateDistanceItem());
		
			// ------ Rest ----
		register(new ClusterMentionGenderMatchFactoryItem());
		register(new ClusterMentionNumberMatchFactoryItem());
		register(new ClusterMentionPersonMatchFactoryItem());
	}
	
	private static class FactoryHolder {
        private static final FeatureFactory FACTORY = new FeatureFactory();
    }
	public static FeatureFactory getFactory(){
		return FactoryHolder.FACTORY;
	}
	
	private HashMap<String, Class<?>> prefixToClass;
	private HashMap<Pair<Class<?>, String>, IFeatureFactoryItem<?, ?>> features;
	
	
	public <T extends Object> void register(IFeatureFactoryItem<T, ?> feature){
		String name = feature.createFeature().getName();
		features.put(new ImmutablePair<Class<?>, String>(getPrefixClass(extractPrefix(name)), name), feature);
	}
		
	@SuppressWarnings("unchecked")
	public <T extends Object> AbstractFeature<T, ?> getFeature(Class<T> cls, String name){
		if( features.get(new ImmutablePair<Class<T>, String>(cls, name)) == null) System.out.println("No such feature: "+ name);
		return ((IFeatureFactoryItem<T, ?>) features.get(new ImmutablePair<Class<T>, String>(cls, name))).createFeature();
	}
	
	@SuppressWarnings("unchecked")
	public AbstractFeature<?, ?> getFeature(String name){
		String prefix = extractPrefix(name);
		Class<?> cls = getPrefixClass(prefix);
		return getFeature(cls, name);
	}
	
	private String extractPrefix(String name){
		String prefix = name.split(PREFIX_SEPARATOR)[0];
		return prefix;
	}
	
	public Class<?> getFeatureClass(String feature){
		return getPrefixClass(extractPrefix(feature));
	}
	
	public Class<?> getPrefixClass(String prefix){
		return prefixToClass.get(prefix);
	}
	
}