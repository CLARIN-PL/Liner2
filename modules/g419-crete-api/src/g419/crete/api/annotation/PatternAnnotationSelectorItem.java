package g419.crete.api.annotation;

import java.util.List;

public class PatternAnnotationSelectorItem extends AbstractAnnotationSelectorFactoryItem{

	@Override
	public AbstractAnnotationSelector getSelector(List<AnnotationDescription> annotationDescriptions) {
		return new PatternAnnotationSelector(null);
	}

}
