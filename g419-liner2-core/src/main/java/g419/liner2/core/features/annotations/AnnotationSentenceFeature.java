package g419.liner2.core.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.Map;
import java.util.Set;

public abstract class AnnotationSentenceFeature extends AnnotationFeature{

	public abstract Map<Annotation,String> generate(Sentence sent, Set<Annotation> sentenceAnnotations);
	
}
