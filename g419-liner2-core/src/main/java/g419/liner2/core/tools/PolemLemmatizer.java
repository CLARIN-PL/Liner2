package g419.liner2.core.tools;

import g419.liner2.core.lib.LibLoaderPolem;
import g419.polem.CascadeLemmatizer;

public class PolemLemmatizer {

    final CascadeLemmatizer lemmatizer;

    public PolemLemmatizer(){
        LibLoaderPolem.load();
        lemmatizer = CascadeLemmatizer.assembleLemmatizer();
    }


    public String lemmatize(final String orths, final String bases, final String ctags){
        return lemmatize(orths, bases, ctags);
    }

    public String lemmatize(final String orths, final String bases, final String ctags, final boolean debug){
        return lemmatizer.lemmatizeS(orths, bases, ctags, debug);
    }
}
