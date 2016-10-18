package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionPreceedingEntityRecency extends AbstractClusterMentionFeature<Float>{

	private Integer sentences;
	
	public ClusterMentionPreceedingEntityRecency(int sentences){
		this.sentences = sentences;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
			Annotation mention = input.getLeft();
			AnnotationCluster cluster = input.getRight();
			
			int mentionSentenceOrd = mention.getSentence().getOrd();
			int startSentenceOrd = Math.max(0, mentionSentenceOrd - this.sentences);
		
			this.value = 0.0f;
			for(Annotation clusterMention: cluster.getAnnotations()){
				if(clusterMention.getSentence().getOrd() >= startSentenceOrd && clusterMention.getSentence().getOrd() <= mentionSentenceOrd){
					this.value += 1.0f;
				}
			}
	}

	@Override
	public String getName() {
		return "clustermention_preceeding_entity_recency" + sentences;
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
