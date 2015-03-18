package g419.crete.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationFeatureFollowingConjunctionByLike extends AbstractFeature<Annotation, Boolean>{

	public static final Set<String> byLikeConjunctions = new HashSet<String>(Arrays.asList("by", "aby", "ażeby", "żeby", "coby"));
	private final int lookupDistance = 2;
	
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = false;
		
		TokenAttributeIndex ai  = input.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = input.getSentence().getTokens();
		
		int inputIndex = input.getEnd();
		int searchEnd = Math.min(inputIndex + lookupDistance, input.getSentence().getTokens().size());
		
		for(int i = inputIndex; i < searchEnd; i++)
			if(byLikeConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
		
	}

	@Override
	public String getName() {
		return "annotation_following_by_like_conjunction";
	}

	@Override
	public Class<Annotation> getInputTypeClass() {
		return Annotation.class;
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}

}
