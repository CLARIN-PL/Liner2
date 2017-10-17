package g419.crete.core.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternAnnotationSelector extends AbstractAnnotationSelector{

	private List<Pattern> types;
	
	public PatternAnnotationSelector(String[] patterns){
		types = new ArrayList<>();
		for(String pattern : patterns) types.add(Pattern.compile(pattern));
	}
	
	@Override
	public List<Annotation> selectAnnotations(Document document) {
		return document.getAnnotations(types);
	}

	@Override
	public boolean matches(Annotation annotation) {
		return types.contains(annotation.getType());
	}

}
