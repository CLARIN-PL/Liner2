package g419.crete.api.features.annotations;

import java.util.ArrayList;
import java.util.Set;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;

public class AnnotationFeaturePreceedingConjunctionByLike extends AbstractFeature<Annotation, Boolean>{

	public static final Set<String> byLikeConjunctions = null;
	private final int lookupDistance = 2;
	
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = false;
		
		TokenAttributeIndex ai  =input.getSentence().getAttributeIndex();
		ArrayList<Token> tokens = input.getSentence().getTokens();
		
		int inputIndex = input.getBegin();
		int searchStart = Math.max(0, inputIndex - lookupDistance);
		
		for(int i = searchStart; i < inputIndex; i++)
			if(byLikeConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
		
	}

	@Override
	public String getName() {
		return "annotation_preceeding_by_like_conjunction";
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
