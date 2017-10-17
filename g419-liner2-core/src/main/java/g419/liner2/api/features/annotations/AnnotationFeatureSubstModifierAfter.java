package g419.liner2.api.features.annotations;


import java.util.HashSet;
import java.util.Set;

import g419.corpus.structure.Annotation;
import g419.liner2.api.tools.SentenceTraverse;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureSubstModifierAfter extends AnnotationAtomicFeature {

	private Set<String> nameSubstJoiner = new HashSet<String>(); 
	private Set<String> caseNomInst = new HashSet<String>();
	
    /**
     * 
     */
    public AnnotationFeatureSubstModifierAfter(){
    	this.nameSubstJoiner.add("być");
    	this.nameSubstJoiner.add("to");
    	this.nameSubstJoiner.add("jest");
    	this.nameSubstJoiner.add("-");
    	this.nameSubstJoiner.add("–");
    	this.nameSubstJoiner.add("—");
    	this.nameSubstJoiner.add("―");
    	
    	this.caseNomInst.add("nom");
    	this.caseNomInst.add("inst");
    }

	@Override
	public String generate(Annotation an) {
		String value = null;
		
		SentenceTraverse st = new SentenceTraverse(an.getSentence());
		st.setPointer(an.getEnd()+1);
		if ( st.consumeByBase(this.nameSubstJoiner) 
				&& st.forwardUnitPosMatches("adj") >= 0
				&& st.matchesByPos("subst")
				&& st.matchesByCase(this.caseNomInst)){
			value = st.getCurrentToken().getDisambTag().getBase();
		}
		
	    return value;
	}
    
}
