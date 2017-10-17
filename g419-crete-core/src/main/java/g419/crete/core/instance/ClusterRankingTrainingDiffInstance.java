package g419.crete.core.instance;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.factory.FeatureFactory;
import g419.crete.core.structure.Cluster;
import g419.crete.core.structure.IHaveFeatures;
import g419.crete.core.structure.Mention;

import java.util.Arrays;
import java.util.List;

public class ClusterRankingTrainingDiffInstance extends AbstractCreteInstance<Integer>{

	private Mention mention;
	private Cluster clusterFirst;
	private Cluster clusterSecond;
	
	public ClusterRankingTrainingDiffInstance(Annotation mention, AnnotationCluster cluster1, AnnotationCluster cluster2, Integer label, List<String> features) {
		super(label, features);
		this.mention = new Mention(mention);
		this.clusterFirst = new Cluster(cluster1);
		this.clusterSecond = new Cluster(cluster2);
	}
	
	public Mention getMention(){return mention;}
	public Cluster getClusterFirst(){return clusterFirst;}
	public Cluster getClusterSecond(){return clusterSecond;}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void extractFeatures() {
		FeatureFactory factory = FeatureFactory.getFactory(); 

		for(String featureName : featureNames){
			
			if(Annotation.class.equals(factory.getPrefixClass(featureName))){
				mention.addFeature((AbstractFeature<Annotation, ?>)factory.getFeature(featureName));
			}
			
			else if(AnnotationCluster.class.equals(factory.getPrefixClass(featureName))){
				// TODO: ensure distinct instances of features
				clusterFirst.addFeature((AbstractFeature<AnnotationCluster, ?>)factory.getFeature(featureName));
				clusterSecond.addFeature((AbstractFeature<AnnotationCluster, ?>)factory.getFeature(featureName));
			}
		}
		
		mention.generateFeatures();
		clusterFirst.generateFeatures();
		clusterSecond.generateFeatures();
		
	}

	@Override
	public List<IHaveFeatures<?>> getComponents() {
		return Arrays.asList(new IHaveFeatures<?>[]{this.mention, this.clusterFirst, this.clusterSecond});
	}

}
