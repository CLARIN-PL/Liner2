package g419.liner2.api.converter;


import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

/**
 * Created by michal on 6/3/14.
 */
public abstract class Converter {

    public abstract void finish(Document doc);

    public void apply(Document doc){
        start(doc);
        for(Sentence sent: doc.getSentences()){
            apply(sent);
        }
        finish(doc);

    }

    abstract public void start(Document doc);

    abstract public void apply(Sentence sentence);


}
