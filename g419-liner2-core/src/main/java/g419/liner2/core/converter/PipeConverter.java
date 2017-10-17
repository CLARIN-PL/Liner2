package g419.liner2.core.converter;


import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

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
    public void finish(Document doc) {
        for(Converter c: pipe){
            c.finish(doc);
        }

    }

    @Override
    public void start(Document doc) {
        for(Converter c: pipe){
            c.start(doc);
        }
    }

    @Override
    public void apply(Sentence sentence) {
        for(Converter c: pipe){
            c.apply(sentence);
        }
    }
}
