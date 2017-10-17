package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.TokenAttributeIndex;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureStringDistance extends  AbstractAnnotationPairFeature<Float>{

	private final StringDistance measure;
	private final  String measureName;
	private final boolean base;
	
	public AnnotationPairFeatureStringDistance(StringDistance measure, String measureName, boolean base){
		this.measure = measure;
		this.measureName = measureName;
		this.base = base;
	}
	
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		Annotation firstAnnotation = input.getLeft();
		Annotation secondAnnotation = input.getRight();
		TokenAttributeIndex ai = firstAnnotation.getSentence().getAttributeIndex();
		String  firstText, secondText;
		if(this.base){
			firstText = firstAnnotation.getBaseText();
			secondText = secondAnnotation.getBaseText();
		}
		else{
			firstText = firstAnnotation.getText();
			secondText =secondAnnotation.getText();
		}
		
		double m = this.measure.distance(firstText, secondText);
		if(m != m) m = 0.0;
		this.value = new Float(m);
	}

	@Override
	public String getName() {
		return "annotationpair_stringdistance_" + this.measureName + (this.base?"_base":"");
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

		  
	
	
}
