package g419.liner2.core.features.annotations;


import java.util.HashSet;
import java.util.Set;

import g419.corpus.structure.Annotation;
import g419.liner2.core.tools.SentenceTraverse;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureVerbPredicate extends AnnotationAtomicFeature {

	private Set<String> posVerb = new HashSet<String>(); 
	
    /**
     * 
     */
    public AnnotationFeatureVerbPredicate(){
    	this.posVerb.add("fin");
    	this.posVerb.add("bedzie");
    	this.posVerb.add("praet");
    	this.posVerb.add("impt");
    	this.posVerb.add("imps");
    	this.posVerb.add("winien");
    	this.posVerb.add("pred");
    }

	@Override
	public String generate(Annotation an) {
		String value = "NULL";		
		SentenceTraverse st = new SentenceTraverse(an.getSentence());
		st.setPointer(an.getEnd() + 1);
		if ( an.getHeadToken().getDisambTag().getCase().equals("nom")
				&& st.matchesByPos(this.posVerb)){
			value = st.getCurrentToken().getDisambTag().getBase();
		}
	    return value;
	}
    
}
