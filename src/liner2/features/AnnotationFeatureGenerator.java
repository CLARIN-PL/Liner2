package liner2.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.LinerOptions;
import liner2.features.annotations.*;
import liner2.structure.Annotation;
import liner2.structure.Sentence;
import org.maltparser.MaltParserService;

public class AnnotationFeatureGenerator {

	private List<AnnotationFeature> features = new ArrayList<AnnotationFeature>();
    private List<AnnotationSentenceFeature> sentenceFeatures = new ArrayList<AnnotationSentenceFeature>();
	
	private Pattern patternBase = Pattern.compile("base:(-?[0-9]+)");
    private Pattern patternDict = Pattern.compile("dict:([^:]*)");
    private Pattern patternMalt = Pattern.compile("malt:([^:]*)(-?[0-9]+)(.*)");
    MaltParserService malt = null;
	
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
            Matcher matcherDict = this.patternDict.matcher(feature);
            if ( matcherDict.find() ){
                this.features.add(new AnnotationFeatureDict(matcherDict.group(1)));
                continue;
            }
            Matcher matcherMalt = this.patternMalt.matcher(feature);
            if ( matcherDict.find() ){
                this.sentenceFeatures.add(new AnnotationFeatureMalt(matcherMalt.group(1),Integer.parseInt(matcherMalt.group(2)),matcherMalt.group(3)));
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

    public List<HashMap<Annotation,String>> generate(Sentence sent){
        List<HashMap<Annotation,String>> features = new ArrayList<HashMap<Annotation, String>>();
        for (AnnotationSentenceFeature afg : this.sentenceFeatures)
            features.add(afg.generate(sent));
        return features;
    }
	
	public int getFeaturesCount(){
		return this.features.size()+this.sentenceFeatures.size();
	}
}