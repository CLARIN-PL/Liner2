package liner2.converter;

import liner2.structure.AnnotationSet;

import java.util.ArrayList;

/**
 * Created by michal on 6/3/14.
 */
public class PipeConverter extends Converter{
    ArrayList<Converter> pipe;

    public PipeConverter(ArrayList<Converter> converters){
        pipe = converters;
    }
    @Override
    public void apply(AnnotationSet sentenceAnnotations) {
        for(Converter c: pipe){
            c.apply(sentenceAnnotations);
        }
    }
}
