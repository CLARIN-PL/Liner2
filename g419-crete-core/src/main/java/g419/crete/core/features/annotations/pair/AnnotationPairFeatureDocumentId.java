package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureDocumentId extends AbstractAnnotationPairFeature<String> {

	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		
		String firstDoc = firstAnnotation.getSentence().getDocument().getName();
		String secondDoc = secondAnnotation.getSentence().getDocument().getName();
		
		this.value = (firstDoc.equalsIgnoreCase(secondDoc)?firstDoc:firstDoc + secondDoc);
	}

	@Override
	public String getName() {
		return "annotationpair_docid";
	}

	@Override
	public Class<String> getReturnTypeClass() {
		return String.class;
	}

}
