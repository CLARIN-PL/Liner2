package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureText extends AbstractAnnotationPairFeature<String> {

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		TokenAttributeIndex ai = firstAnnotation.getSentence()
				.getAttributeIndex();
		String firstText = firstAnnotation.getText();
		String secondText = secondAnnotation.getText();

		this.value = firstText + "  <----> " + secondText;
	}

	@Override
	public String getName() {
		return "annotationpair_text";
	}

	@Override
	public Class<String> getReturnTypeClass() {
		return String.class;
	}

}
