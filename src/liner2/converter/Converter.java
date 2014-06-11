package liner2.converter;

import liner2.structure.AnnotationSet;
import liner2.structure.Document;

import java.util.ArrayList;
import java.util.regex.Pattern;

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
