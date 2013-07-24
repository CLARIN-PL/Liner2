package liner2.features;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.features.annotations.AnnotationFeature;
import liner2.features.annotations.AnnotationFeatureContextBase;
import liner2.structure.Annotation;

public class AnnotationFeatureGenerator {

	private List<AnnotationFeature> features = new ArrayList<AnnotationFeature>();
	
	private Pattern patternBase = Pattern.compile("\\p{L}+:(-?[0-9]+)");
	
	/**
	 * 
	 * @param features — array with feature definitions
	 */
	public AnnotationFeatureGenerator(List<String> features){
		for ( String feature : features ){
			Matcher matcherBase = this.patternBase.matcher(feature); 
			if ( matcherBase.find() ){				
				this.features.add(new AnnotationFeatureContextBase(Integer.parseInt(matcherBase.group(1))));
				continue;
			}
		}
	}

	public List<String> generate(Annotation ann){
		List<String> features = new ArrayList<String>();
		for (AnnotationFeature afg : this.features)
			features.add(afg.generate(ann));
		return features;		
	}
	
	public int getFeaturesCount(){
		return this.features.size();
	}
}