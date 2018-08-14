package g419.crete.core.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ConfigurableAnnotationSelector extends AbstractAnnotationSelector{
	
	private final List<AnnotationDescription> descriptions;
	private final List<Pattern> patterns;
	
	public ConfigurableAnnotationSelector(List<AnnotationDescription> annotationDescriptions){
		descriptions = annotationDescriptions;
		Set<Pattern> patternSet = new HashSet<Pattern>();
		for(AnnotationDescription description : descriptions)
			patternSet.addAll(description.getPatterns());
		
		patterns = new ArrayList<Pattern>(patternSet);
	}
	
	@Override
	public List<Annotation> selectAnnotations(Document document) {
		ArrayList<Annotation> selectedAnnotations = new ArrayList<Annotation>();
		for(Annotation annotation : document.getAnnotations(patterns))
			if(matches(annotation))
					selectedAnnotations.add(annotation);
		
		return selectedAnnotations;
	}

	@Override
	public boolean matches(Annotation annotation) {
		boolean typeMatch = patterns.stream().anyMatch(p -> p.matcher(annotation.getType()).matches());
		if(!typeMatch) return false;

		boolean descriptionMatch = false;
		for(AnnotationDescription description : descriptions)
			if(description.match(annotation))
				descriptionMatch = true;

		return typeMatch && descriptionMatch;
	}

}