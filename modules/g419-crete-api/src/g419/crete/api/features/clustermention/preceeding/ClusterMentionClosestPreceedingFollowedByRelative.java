package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingFollowedByRelative extends AbstractClusterMentionFeature<Boolean>{
	
	public static final int lookupDistance = 2;
	
	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		
		TokenAttributeIndex ai  = closestPreceeding.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = closestPreceeding.getSentence().getTokens();
		
		int inputIndex = closestPreceeding.getEnd();
		int searchEnd = Math.min(inputIndex + lookupDistance, closestPreceeding.getSentence().getTokens().size());
		
		for(int i = inputIndex; i < searchEnd; i++)
			if("który".equalsIgnoreCase(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_followed_by_relative";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}
	

}
