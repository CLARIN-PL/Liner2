package g419.liner2.api.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AnnotationSentenceFeature extends AnnotationFeature{

	public abstract Map<Annotation,String> generate(Sentence sent, Set<Annotation> sentenceAnnotations);
	
}
