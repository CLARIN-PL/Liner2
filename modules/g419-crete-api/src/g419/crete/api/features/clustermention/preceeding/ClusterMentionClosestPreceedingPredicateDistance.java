package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingPredicateDistance extends AbstractClusterMentionFeature<Integer> {

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
			
		List<Token> tokensPreceeding = AnnotationUtil.tokensBetweenAnnotations(closestPreceeding, mention, cluster.getDocument());
		
		int count = 0;
		for(Token t: tokensPreceeding) 
			if(t.getAttributeValue("pos") == "verb")
				count++;
		
		this.value = count;
		
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_predicate_distance";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
