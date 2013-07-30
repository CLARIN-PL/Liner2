package liner2.features.annotations;


import liner2.structure.Annotation;
import liner2.structure.Sentence;
import java.util.HashMap;

public abstract class AnnotationSentenceFeature {

	public abstract HashMap<Annotation,String> generate(Sentence sent);
	
}
