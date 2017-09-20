package g419.liner2.api.features.annotations;


import g419.corpus.structure.Annotation;
import g419.liner2.api.tools.SentenceTraverse;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureSubstList extends AnnotationAtomicFeature {

    /**
     * 
     */
    public AnnotationFeatureSubstList(){
    }

	@Override
	public String generate(Annotation an) {
		String value = "NULL";
		SentenceTraverse st = new SentenceTraverse(an.getSentence());
		st.setPointer(an.getBegin());
		if ( st.backwardUnitFindBase(":") > 0 
				&& st.backwardAny()
				&& st.matchesByPos("subst") ){
			value = st.getCurrentToken().getDisambTag().getBase();
		}
		
	    return value;
	}
    
}
