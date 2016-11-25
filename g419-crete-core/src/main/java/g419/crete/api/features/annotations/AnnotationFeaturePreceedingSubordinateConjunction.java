package g419.crete.api.features.annotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.AbstractFeature;

public class AnnotationFeaturePreceedingSubordinateConjunction extends AbstractFeature<Annotation, Boolean>{

	public static final Set<String> subordinateConjunctions = new HashSet<String>(Arrays.asList("aby", "ażeby", "aniżeli",
		    "bo", "bowiem", "by", "byle",
		    "choć", "chociaż", "choćby", "czy", "czego",
		    "dlatego że", "dopóki",
		    "iżby", "ilekroć",
		    "jak", "jak gdyby", "jakby", "jako że", "jeżeli", "jeśli", "jakkolwiek",
		    "gdy", "gdyby", "gdyż",
		    "kiedy", "który", "którego", "które", "która", "którą",
		    "mimo że",
		    "niż", "niżeli", "niźli",
		    "odkąd",
		    "ponieważ", "podczas gdy", "pomimo że",
		    "skoro",
		    "że", "żeby"));
	
	private final int lookupDistance;
	
	public AnnotationFeaturePreceedingSubordinateConjunction(int lookup) {
		this.lookupDistance = lookup;
	}
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = false;
		
		TokenAttributeIndex ai  =input.getSentence().getAttributeIndex();
		List<Token> tokens = input.getSentence().getTokens();
		
		int inputIndex = input.getBegin();
		int searchStart = Math.max(0, inputIndex - lookupDistance);
		
		for(int i = searchStart; i < inputIndex; i++)
			if(subordinateConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
		
	}

	@Override
	public String getName() {
		return "annotation_preceeding_subordinate_conjunction"+lookupDistance;
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
