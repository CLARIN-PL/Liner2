package g419.crete.api.instance;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.factory.FeatureFactory;
import g419.crete.api.structure.Cluster;
import g419.crete.api.structure.IHaveFeatures;
import g419.crete.api.structure.Mention;
import g419.crete.api.structure.MentionClusterPair;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class ClusterClassificationInstance extends AbstractCreteInstance<Integer>{
	
	protected Integer POSITIVE_LABEL = 1;
	protected Integer NEGATIVE_LABEL = -1;
	
	private Mention mention;
	public Mention getMention(){return mention;}
	
	private Cluster cluster;
	public Cluster getCluster(){return cluster;}
	
	private MentionClusterPair mentionClusterPair;
	public MentionClusterPair getMentionClusterPair(){return mentionClusterPair;}
	
	private double weight;
	public double getWeigth(){return this.weight;}
	public void setWeight(double weight){this.weight = weight;}
	
	public ClusterClassificationInstance(Annotation mention, AnnotationCluster cluster){
		this.mention = new Mention(mention);
		this.cluster = new Cluster(cluster);
		this.mentionClusterPair = new MentionClusterPair(new ImmutablePair<Annotation, AnnotationCluster>(mention, cluster));
	}
	
	public ClusterClassificationInstance(Annotation mention, AnnotationCluster cluster, Integer label, List<String> features){
		super(label, features);
		this.mention = new Mention(mention);
		this.cluster = new Cluster(cluster);
		this.mentionClusterPair = new MentionClusterPair(new ImmutablePair<Annotation, AnnotationCluster>(mention, cluster));
		extractFeatures();
	}
	
	@Override
	public String toString(){
		return mention.getHolder().toString() + " -- ??? --> " + cluster.getHolder().toString() + "\n";
	}

	
	@Override
	@SuppressWarnings("unchecked")
	protected void extractFeatures() {
		FeatureFactory factory = FeatureFactory.getFactory(); 

		for(String featureName : featureNames){
			
			if(Annotation.class.equals(factory.getFeatureClass(featureName))){
				mention.addFeature((AbstractFeature<Annotation, ?>)factory.getFeature(featureName));
			}
			
			else if(AnnotationCluster.class.equals(factory.getFeatureClass(featureName))){
				cluster.addFeature((AbstractFeature<AnnotationCluster, ?>)factory.getFeature(featureName));
			}
			
			else if(mentionClusterPair.getHolder().getClass().equals(factory.getFeatureClass(featureName))){
				mentionClusterPair.addFeature((AbstractFeature<Pair<Annotation, AnnotationCluster>, ?>)factory.getFeature(featureName));
			}
		}
		
		mention.generateFeatures();
		cluster.generateFeatures();
		mentionClusterPair.generateFeatures();
	}


	@Override
	public List<IHaveFeatures<?>> getComponents() {
		return Arrays.asList(new IHaveFeatures<?>[]{this.mention, this.cluster, this.mentionClusterPair});
	}
}