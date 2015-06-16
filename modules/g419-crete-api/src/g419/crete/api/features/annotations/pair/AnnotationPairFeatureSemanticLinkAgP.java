package g419.crete.api.features.annotations.pair;

import g419.corpus.structure.Annotation;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureSemanticLinkAgP extends AbstractAnnotationPairFeature<Float>{

	public int getNamedEntitySynsetId(String channelName){
		return -1;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		
		Annotation named = input.getLeft();
		Annotation agp = input.getRight();
		
		assert(named.getType().startsWith("nam"));
		assert(agp.getType().equalsIgnoreCase("anafora_wyznacznik"));
		
		int namedSynsetId = getNamedEntitySynsetId(named.getType());
		
	}

	@Override
	public String getName() {
		return "annotationpair_semantic_link";
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
