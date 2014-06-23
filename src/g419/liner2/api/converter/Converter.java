package g419.liner2.api.converter;


import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;

/**
 * Created by michal on 6/3/14.
 */
public abstract class Converter {

    public void apply(Document doc){
        for(AnnotationSet sentenceAnnotations: doc.getChunkings().values()){
            apply(sentenceAnnotations);
        }

    }

    abstract public void apply(AnnotationSet sentenceAnnotations);


}
