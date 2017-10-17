package g419.crete.core.annotation;

import java.util.List;

public class ConfigurableAnnotationSelectorItem extends AbstractAnnotationSelectorFactoryItem {

	@Override
	public AbstractAnnotationSelector getSelector(List<AnnotationDescription> annotationDescriptions) {
		return new ConfigurableAnnotationSelector(annotationDescriptions);
	}

}