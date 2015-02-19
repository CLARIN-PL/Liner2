package g419.crete.api.features.clustermention;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.enumvalues.Gender;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionGender extends AbstractClusterMentionFeature<Gender>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		
		if(closestPreceeding == null){
			this.value = Gender.UNDEFINED;
		}
		else{
			closestPreceeding.assignHead();
			Token headToken = closestPreceeding.getSentence().getTokens().get(closestPreceeding.getHead());
			
			TokenAttributeIndex ai = closestPreceeding.getSentence().getAttributeIndex();
					
			this.value = Gender.fromValue(ai.getAttributeValue(headToken, "gender"));
		}
		
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_gender";
	}

	@Override
	public Class<Gender> getReturnTypeClass() {
		return Gender.class;
	}
	
	@Override
	public int getSize() {
		return Gender.values().length;
	}

	@Override
	public List<Gender> getAllValues(){
		return Arrays.asList(Gender.values());
	}
	
}
