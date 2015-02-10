package g419.crete.api.features.factory;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.factory.item.AnnotationGenderFeatureFactoryItem;
import g419.crete.api.features.factory.item.AnnotationNumberFeatureFactoryItem;
import g419.crete.api.features.factory.item.AnnotationPersonFeatureFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingInSameSentenceFactoryItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionGenderItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionNumberItem;
import g419.crete.api.features.factory.item.ClusterMentionClosestPreceedingMentionPersonItem;
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
		
		// ----------------------- CLUSTER FEATURES ----------------------------
		register(new ClusterMentionCountFeatureFactoryItem());
		register(new ClusterTermFrequencyFeatureFactoryItem());
		register(new ClusterSentenceFrequencyFeatureFactoryItem());
		
		// ----------------------- MENTION_CLUSTER PAIR FEATURES -------
		register(new ClusterMentionClosestPreceedingTokenDistanceItem());
		register(new ClusterMentionClosestPreceedingInSameSentenceFactoryItem());
		register(new ClusterMentionClosestPreceedingMentionGenderItem());
		register(new ClusterMentionClosestPreceedingMentionNumberItem());
		register(new ClusterMentionClosestPreceedingMentionPersonItem());
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