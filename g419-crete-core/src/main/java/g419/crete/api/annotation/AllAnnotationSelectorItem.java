package g419.crete.api.annotation;

import java.util.List;

public class AllAnnotationSelectorItem extends AbstractAnnotationSelectorFactoryItem{

	@Override
	public AbstractAnnotationSelector getSelector(List<AnnotationDescription> annotationDescriptions) {
		return new AllAnnotationSelector();
	}

}
