//package g419.crete.api.instance.converter;
//
//import edu.berkeley.compbio.jlibsvm.util.SparseVector;
//import g419.corpus.structure.Annotation;
//import g419.corpus.structure.AnnotationCluster;
//import g419.crete.api.features.IFeature;
//import g419.crete.api.features.representation.FeatureRepresentation;
//import g419.crete.api.instance.ClusterRankingTrainingDiffInstance;
//import g419.crete.api.instance.representation.SubtractableSparseVector;
//
//public class ClusterRankingTrainingDiffInstanceConverter extends AbstractCreteInstanceConverter<ClusterRankingTrainingDiffInstance, SparseVector>{
//
//	@Override
//	public SparseVector convertSingleInstance(ClusterRankingTrainingDiffInstance instance) {
//		SubtractableSparseVector intermediateInstance = new SubtractableSparseVector(0);
//		SubtractableSparseVector clusterFirstVector = new SubtractableSparseVector(0);
//		SubtractableSparseVector clusterSecondVector = new SubtractableSparseVector(0);
//		
//		for(IFeature<Annotation, ?> feature : instance.getMention().extractFeatures())
//			intermediateInstance.concat(FeatureRepresentation.getRepresentation().represent(feature, SubtractableSparseVector.class));
//		
//		for(IFeature<AnnotationCluster, ?> feature : instance.getClusterFirst().extractFeatures())
//			clusterFirstVector.concat(FeatureRepresentation.getRepresentation().represent(feature, SubtractableSparseVector.class));
//		
//		for(IFeature<AnnotationCluster, ?> feature : instance.getClusterSecond().extractFeatures())
//			clusterSecondVector.concat(FeatureRepresentation.getRepresentation().represent(feature, SubtractableSparseVector.class));
//	
//		clusterFirstVector.minus(clusterSecondVector);
//		intermediateInstance.concat(clusterFirstVector);
//		
//		return intermediateInstance;
//	}
//
//	@Override
//	public Class<SparseVector> getRepresentationClass() { return SparseVector.class;}
//
//}
