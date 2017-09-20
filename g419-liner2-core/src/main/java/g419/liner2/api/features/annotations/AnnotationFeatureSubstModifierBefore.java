package g419.liner2.api.features.annotations;


import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;

/**
 * @author Michał Marcińczuk
 */
public class AnnotationFeatureSubstModifierBefore extends AnnotationAtomicFeature {

    /**
     * 
     */
    public AnnotationFeatureSubstModifierBefore(){
    }

	@Override
	public String generate(Annotation an) {
		String value = "NULL";
		
		List<Token> tokens = an.getSentence().getTokens();
		
		if ( ( an.getHeadToken().getDisambTag().getCase().equals("nom")
				|| an.getHeadToken().getDisambTag().getPos().equals("adj") )
				&& an.getBegin() > 0 
				&& tokens.get(an.getBegin()-1).getDisambTag().getPos().equals("subst") ){
			value = tokens.get(an.getBegin()-1).getDisambTag().getBase();
		}
		
	    return value;
	}
    
}
