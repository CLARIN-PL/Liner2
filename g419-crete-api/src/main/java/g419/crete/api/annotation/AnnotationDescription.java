package g419.crete.api.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class AnnotationDescription {

//	private final Map<String, String> configuration;
	private final List<Pattern> patterns;
	private final List<AnnotationDescriptionFeature> features;
	
	private static final String FEATURE_NEGATION = "!";
	
	private static final String FEATURE_ANNOTATION_LENGTH = "len";
	private static final String FEATURE_ANNOTATION_TYPE = "type_regex";
	
	public static class AnnotationDescriptionFeature{
		private final String name;
		private final List<String> values;
		private final boolean negated;
		
		public AnnotationDescriptionFeature(String nam, List<String> val, boolean neg){
			name = nam;
			values = val;
			negated = neg;
			// Check if expected value is negated
			if(negated) values.remove(0);
		}
		
		public String getName(){
			return name;
		}
		
		public boolean check(Annotation annotation){
			if(values == null || values.size() <= 0) return false;
			if(FEATURE_ANNOTATION_LENGTH.equals(name)){
				try{
					int length = Integer.parseInt(values.get(0));
					return length == annotation.getTokens().size();
				}
				catch(NumberFormatException ex){
					System.out.println("Invalid format for annotation length specified");
					return false;
				}
			}
			else{
				// Token feature
				Token mainToken = getAnnotationMainToken(annotation);
				String feature = mainToken.getAttributeValue(name);
				
				// If token has not defined feature - return false
				if(feature ==  null || "".equals(feature.trim())) return false;
				
				// Set default return value to false
				boolean tokenHasFeature = false;
				
				for(String value : values){
					if(feature.equalsIgnoreCase(value)){
						tokenHasFeature = true;
						break;
					}
				}
				
				return negated ? !tokenHasFeature : tokenHasFeature;
			}
		}
	}
	
	public AnnotationDescription(Map<String, List<String>> config){
		patterns = new ArrayList<Pattern>();
		for(String patternString : config.get(FEATURE_ANNOTATION_TYPE))
			patterns.add(Pattern.compile(patternString));
		config.remove(FEATURE_ANNOTATION_TYPE);
		
		features = new ArrayList<AnnotationDescriptionFeature>(); 
		for(Entry<String, List<String>> configEntry : config.entrySet()){
			String name = configEntry.getKey();
			List<String> values = configEntry.getValue();
			boolean negated = values.get(0) == FEATURE_NEGATION && values.size() > 1;
			features.add(new AnnotationDescriptionFeature(name, values, negated));
		}
	}
	
	public List<Pattern> getPatterns(){
		return this.patterns;
	}
	
	private static Token getAnnotationMainToken(Annotation annotation){
		if(annotation.hasHead()) return annotation.getSentence().getTokens().get(annotation.getHead());
		return annotation.getSentence().getTokens().get(annotation.getTokens().first());
	}
	
	public boolean match(Annotation annotation){
		for(AnnotationDescriptionFeature feature : features)
			if(!feature.check(annotation))
				return false;
		
		return true;
	}
}
