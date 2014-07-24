package g419.liner2.api.converter;


import g419.corpus.structure.Annotation;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by michal on 6/3/14.
 */
public class PipeConverter extends Converter{
    ArrayList<Converter> pipe;

    public PipeConverter(ArrayList<Converter> converters){
        pipe = converters;
    }
    @Override
    public void apply(LinkedHashSet<Annotation> sentenceAnnotations) {
        for(Converter c: pipe){
            c.apply(sentenceAnnotations);
        }
    }
}
