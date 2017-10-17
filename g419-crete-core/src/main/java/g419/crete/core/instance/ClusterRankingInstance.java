package g419.crete.core.instance;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.structure.Cluster;
import g419.crete.core.structure.IHaveFeatures;
import g419.crete.core.structure.Mention;

import java.util.Arrays;
import java.util.List;

public class ClusterRankingInstance extends AbstractCreteInstance<Integer> {

	private Mention mention;
	private Cluster cluster;
	
	
	public ClusterRankingInstance(Annotation mention, AnnotationCluster cluster, Integer label, List<String> features){
		super(label, features);
		this.mention = new Mention(mention);
		this.cluster = new Cluster(cluster);
	}
	
	public Cluster getCluster(){
		return this.cluster;
	}
	
	@Override
	protected void extractFeatures() {
//		this.features = new SubtractableSparseVector(3);
//		this.features.set(0, getLabel());
//		this.features.set(1, getLabel());
//		this.features.set(2, getLabel());
	}
	
	public void setFeatures(float f1, float f2 , float f3){
//		this.features = new SubtractableSparseVector(3);
//		this.features.set(0, f1);
//		this.features.set(1, f2);
//		this.features.set(2, f3);
	}

	public String toString(){
		return String.format("<<(%s, %s) , < %d >>", mention, cluster, label);
	}

	@Override
	public List<IHaveFeatures<?>> getComponents() {
		return Arrays.asList(new IHaveFeatures<?>[]{this.mention, this.cluster});
	}
}
