package g419.liner2.api.converter;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;

import java.util.HashSet;

/**
 * Created by michal on 6/3/14.
 */
public class AnnotationRemoveNestedConverter extends Converter{
    @Override
    public void apply(AnnotationSet sentenceAnnotations) {
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
            sentenceAnnotations.removeChunk(ann);
        }
    }

    private HashSet<Annotation> getOneType(String type, AnnotationSet as){
        HashSet<Annotation> oneType = new HashSet<Annotation>();
        for(Annotation ann: as.chunkSet()){
            if(ann.getType().equals(type)){
                oneType.add(ann);
            }
        }
        return oneType;
    }

    private HashSet<String> getTypes(AnnotationSet as){
        HashSet<String> types = new HashSet<String>();
        for(Annotation ann: as.chunkSet()){
            types.add(ann.getType());
        }
        return types;
    }
}
