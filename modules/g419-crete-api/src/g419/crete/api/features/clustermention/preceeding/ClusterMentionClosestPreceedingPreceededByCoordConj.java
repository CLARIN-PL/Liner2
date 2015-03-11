package g419.crete.api.features.clustermention.following;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingPreceededByCoordConj extends AbstractClusterMentionFeature<Boolean>{

	public static final Set<String> coordinateConjunctions = new HashSet<String>(Arrays.asList(
			"a", "aczkolwiek", "albo", "ale", "ani", "bądź", "czy", "czyli",
		    "dlatego", "i", "jednak", "lecz", "lub", "mianowicie", "natomiast", "ni",
		    "oraz", "przeto", "tedy", "toteż", "tudzież", "więc", "zatem", "zaś"
	    ));
	
	public static final int lookupDistance = 2;
	
	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		this.value = false;
		
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		
		TokenAttributeIndex ai  = closestPreceeding.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = closestPreceeding.getSentence().getTokens();
		
		int inputIndex = closestPreceeding.getBegin();
		int searchStart = Math.max(0, inputIndex - lookupDistance);
		
		for(int i = searchStart; i < inputIndex; i++)
			if(coordinateConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_preceeded_by_coord_conj";
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
