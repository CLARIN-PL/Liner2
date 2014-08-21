package g419.liner2.api.converter;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by michal on 6/3/14.
 */
public class AnnotationRemoveNestedConverter extends Converter{

    @Override
    public void finish(Document doc) {

    }

    @Override
    public void apply(Sentence sentence) {
        LinkedHashSet<Annotation> sentenceAnnotations = sentence.getChunks();
        HashSet<Annotation> to_remove = new HashSet<Annotation>();
        for(String type: getTypes(sentenceAnnotations)){
            HashSet<Annotation> oneType = getOneType(type, sentenceAnnotations);
            for(Annotation candidate: oneType){
                for(Annotation ann: oneType){
                    if(ann != candidate && ann.getTokens().containsAll(candidate.getTokens())){
                        to_remove.add(candidate);
                        break;
                    }

                }
            }
        }
        for(Annotation ann: to_remove){
        	if ( this.overlaps(ann, sentenceAnnotations) )
        		sentenceAnnotations.remove(ann);
        }
    }
    
    /**
     * Checks if given annotation is nested with another annotation of the same type.
     * @param ann -- annotation to check
     * @param set -- set of annoations
     * @return
     */
    private boolean overlaps(Annotation ann, Set<Annotation> set){
    	for (Annotation ann2 : set){
    		if ( ann != ann2 
    				&& ann.getType().equals(ann2.getType())
    				&& ann2.getTokens().containsAll(ann.getTokens()) )
    		return true;
    	}
    	return false;
    }

    private HashSet<Annotation> getOneType(String type, LinkedHashSet<Annotation> as){
        HashSet<Annotation> oneType = new HashSet<Annotation>();
        for(Annotation ann: as){
            if(ann.getType().equals(type)){
                oneType.add(ann);
            }
        }
        return oneType;
    }

    private HashSet<String> getTypes(LinkedHashSet<Annotation> as){
        HashSet<String> types = new HashSet<String>();
        for(Annotation ann: as){
            types.add(ann.getType());
        }
        return types;
    }
}
