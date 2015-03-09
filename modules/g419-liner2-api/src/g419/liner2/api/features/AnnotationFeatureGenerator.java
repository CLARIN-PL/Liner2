package g419.liner2.api.features;

import g419.corpus.structure.*;
import g419.liner2.api.features.annotations.*;
import g419.liner2.api.features.annotations.AnnotationAtomicFeature;
import g419.liner2.api.tools.MaltSentence;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnnotationFeatureGenerator {

	private List<AnnotationAtomicFeature> features = new ArrayList<AnnotationAtomicFeature>();
    private List<AnnotationFeatureMalt> maltFeatures = new ArrayList<AnnotationFeatureMalt>();
    private List<AnnotationSentenceFeature> sentenceFeatures = new ArrayList<AnnotationSentenceFeature>();

	private Pattern patternBase = Pattern.compile("base:(-?[0-9]*)$");
    private Pattern patternDict = Pattern.compile("dict:([^:]*):([^:]*)$");
    private Pattern patternMalt = Pattern.compile("malt:([^:]*):([0-9]*):(base|relation)$");
    private Pattern patternClosestBase = Pattern.compile("closest-base:(-?[0-9]*):([a-z]+)$");
    private Pattern patternNeFirstBase = Pattern.compile("ne-first-base:(-?[0-9]*):([a-z]+)$");
    private Pattern patternHead = Pattern.compile("head:(.+)$");

    public ArrayList<String> fetureNames = new ArrayList<String>();

	/**
	 *
	 * @param features — array with feature definitions
	 */
	public AnnotationFeatureGenerator(List<String> features){
		for ( String feature : features ){
			Matcher matcherBase = this.patternBase.matcher(feature);
			if ( matcherBase.find() ){
                AnnotationFeatureContextBase f = new AnnotationFeatureContextBase(Integer.parseInt(matcherBase.group(1)));
                f.setFeatureName(feature);
				this.features.add(f);
                fetureNames.add(f.name);
                continue;
			}
            Matcher matcherDict = this.patternDict.matcher(feature);
            if ( matcherDict.find() ){
                AnnotationFeatureDict f = new AnnotationFeatureDict(matcherDict.group(2), matcherDict.group(1));
                f.setFeatureName(feature);
                this.features.add(f);
                fetureNames.add(f.name);
                continue;
            }
            Matcher matcherHead = this.patternHead.matcher(feature);
            if(matcherHead.find()){
                AnnotationFeatureHeadValue f = new AnnotationFeatureHeadValue(matcherHead.group(1));
                f.setFeatureName(feature);
                this.features.add(f);
                fetureNames.add(f.name);
                continue;
            }
            Matcher matcherMalt = this.patternMalt.matcher(feature);
            if ( matcherMalt.find() ){
                AnnotationFeatureMalt f =new AnnotationFeatureMalt(matcherMalt.group(1), Integer.parseInt(matcherMalt.group(2)), matcherMalt.group(3));
                f.setFeatureName(feature);
                this.maltFeatures.add(f);
                fetureNames.add(f.name);
                continue;
            }
            Matcher matcherClosestBase = this.patternClosestBase.matcher(feature);
            if ( matcherClosestBase.find() ){
                AnnotationFeatureClosestBase f = new AnnotationFeatureClosestBase(matcherClosestBase.group(2), Integer.parseInt(matcherClosestBase.group(1)));
                f.setFeatureName(feature);
                this.sentenceFeatures.add(f);
                fetureNames.add(f.name);
                continue;
            }
            Matcher matcherNeFirstBase = this.patternNeFirstBase.matcher(feature);
            if ( matcherNeFirstBase.find() ){
                AnnotationFeatureNeFirstBase f = new AnnotationFeatureNeFirstBase(matcherNeFirstBase.group(2), Integer.parseInt(matcherNeFirstBase.group(1)));
                f.setFeatureName(feature);
                this.sentenceFeatures.add(f);
                fetureNames.add(f.name);
            }
		}
	}

	public List<String> generate(Annotation ann){
		List<String> features = new ArrayList<String>();
		for (AnnotationAtomicFeature afg : this.features)
			features.add(afg.generate(ann));
		return features;
	}

    public LinkedHashMap<String, HashMap<Annotation, String>> generate(Sentence sent, LinkedHashSet<Annotation> sentenceAnnotations){
        LinkedHashMap<String, HashMap<Annotation, String>> features = new LinkedHashMap<String, HashMap<Annotation, String>>();
        for (AnnotationAtomicFeature afg : this.features){
            HashMap<Annotation, String> atomicFeatureVals = new HashMap<Annotation, String>();
            for(Annotation ann: sentenceAnnotations){
                atomicFeatureVals.put(ann, afg.generate(ann));
            }
            features.put(afg.name, atomicFeatureVals);
        }

        MaltSentence maltSent;
        if(!this.maltFeatures.isEmpty()){
            maltSent = new MaltSentence(sent, sentenceAnnotations);
            for (AnnotationFeatureMalt afg : this.maltFeatures)
            features.put(afg.name, afg.generate(maltSent.getMaltData(), maltSent.getAnnotationIndices()));
        }
        for (AnnotationSentenceFeature afg : this.sentenceFeatures)
        features.put(afg.name, afg.generate(sent, sentenceAnnotations));
        return features;
    }

	public int getFeaturesCount(){
		return this.features.size()+this.maltFeatures.size()+this.sentenceFeatures.size();
	}





}