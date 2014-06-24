package g419.liner2.api.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.HashMap;
import java.util.HashSet;

public abstract class AnnotationSentenceFeature {

	public abstract HashMap<Annotation,String> generate(Sentence sent, HashSet<Annotation> sentenceAnnotations);
	
}
