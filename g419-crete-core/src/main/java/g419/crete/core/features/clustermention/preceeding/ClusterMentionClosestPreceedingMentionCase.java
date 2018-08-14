package g419.crete.core.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.features.enumvalues.Case;
import g419.crete.core.structure.AnnotationUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class ClusterMentionClosestPreceedingMentionCase extends AbstractClusterMentionFeature<Case>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);if(closestPreceeding == null){
			this.value = Case.OTHER;
			return;
		}
		
		if(!closestPreceeding.hasHead()) closestPreceeding.assignHead();
		
		TokenAttributeIndex ai = closestPreceeding.getSentence().getAttributeIndex();
		Token headToken = closestPreceeding.getSentence().getTokens().get(closestPreceeding.getHead());
		
		this.value = Case.fromValue(ai.getAttributeValue(headToken, "case"));
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_case";
	}

	@Override
	public Class<Case> getReturnTypeClass() {
		return Case.class;
	}
	
	@Override
	public List<Case> getAllValues(){
		return Arrays.asList(Case.values());
	}

}
