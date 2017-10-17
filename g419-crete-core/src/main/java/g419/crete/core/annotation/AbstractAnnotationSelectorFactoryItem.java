package g419.crete.core.annotation;

import java.util.List;

public abstract class AbstractAnnotationSelectorFactoryItem {
	abstract public AbstractAnnotationSelector getSelector(List<AnnotationDescription> annotationDescriptions);
}
