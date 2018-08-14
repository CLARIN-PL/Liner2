package g419.crete.core.features.annotations;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.core.features.AbstractFeature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnotationFeaturePreceedingCoordinateConjunction extends AbstractFeature<Annotation, Boolean>{

	public static final Set<String> coordinateConjunctions = new HashSet<String>(Arrays.asList(
			"a", "aczkolwiek", "albo", "ale", "ani", "bądź", "czy", "czyli",
		    "dlatego", "i", "jednak", "lecz", "lub", "mianowicie", "natomiast", "ni",
		    "oraz", "przeto", "tedy", "toteż", "tudzież", "więc", "zatem", "zaś"
	    ));
	
	private final int lookupDistance = 2;
	
	
	@Override
	public void generateFeature(Annotation input) {
		this.value = false;
		
		TokenAttributeIndex ai  =input.getSentence().getAttributeIndex();
		List<Token> tokens = input.getSentence().getTokens();
		
		int inputIndex = input.getBegin();
		int searchStart = Math.max(0, inputIndex - lookupDistance);
		
		for(int i = searchStart; i < inputIndex; i++)
			if(coordinateConjunctions.contains(ai.getAttributeValue(tokens.get(i), "base")))
				this.value = true;
		
	}

	@Override
	public String getName() {
		return "annotation_preceeding_coordinate_conjunction";
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
