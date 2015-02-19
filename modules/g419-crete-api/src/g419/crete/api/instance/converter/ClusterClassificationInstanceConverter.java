//package g419.crete.api.instance.converter;
//
//import g419.corpus.structure.Annotation;
//import g419.corpus.structure.AnnotationCluster;
//import g419.crete.api.features.IFeature;
//import g419.crete.api.features.representation.FeatureRepresentation;
//import g419.crete.api.instance.ClusterClassificationInstance;
//import g419.crete.api.instance.representation.IRepresentation;
//
//import java.util.List;
//
//import weka.core.Instance;
//
//public class ClusterClassificationInstanceConverter  extends AbstractCreteInstanceConverter<ClusterClassificationInstance, Instance>{
//
//	@Override
//	public Instance convertSingleInstance(ClusterClassificationInstance instance) {
//		List<IFeature<Annotation, ?>>mentionFeatures = instance.getMention().extractFeatures();
//		List<IFeature<AnnotationCluster, ?>> clusterFeatures = instance.getCluster().extractFeatures();
//		
//		FeatureRepresentation repr = FeatureRepresentation.getRepresentation();
//		IRepresentation<Instance> totalRepr = repr.initializeRepresentation(instance, Instance.class);
//        
//        for(IFeature<Annotation, ?> mentionFeature : mentionFeatures)
//        	totalRepr.add(repr.represent(mentionFeature, Instance.class));
//        
//        for(IFeature<AnnotationCluster, ?> clusterFeature : clusterFeatures)
//        	totalRepr.add(repr.represent(clusterFeature, Instance.class));
//        
//		return totalRepr.get();		
//	}
//
//	@Override
//	public Class<Instance> getRepresentationClass() {
//		return Instance.class;
//	}
//
//}
