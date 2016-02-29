package g419.corpus.io.writer.tei;

import java.util.ArrayList;
import java.util.List;

import g419.corpus.io.writer.tei.TEILex;
import g419.corpus.structure.Tag;

public class Interps{

    List<TEILex> lexemes = null;
    String disamb = null;
    int disambIdx = 0;

    public Interps(ArrayList<Tag> tags){
        tagsToTEI(tags);
    }
    
    public List<TEILex> getLexemes(){
    	return this.lexemes;
    }

    public int getDisambIdx(){
    	return this.disambIdx;
    }
    
    public String getDisamb(){
    	return this.disamb;
    }
    
    public void tagsToTEI(ArrayList<Tag> tags){
        lexemes = new ArrayList<TEILex>();
        int msdIdx = 0;
        for (Tag tag: tags){
            String base = tag.getBase();
            String[] ctag = tag.getCtag().split(":");
            String TEIctag = ctag[0];
            String msd;
            if (ctag.length > 1){
                msd =  tag.getCtag().substring(TEIctag.length()+1);
            }
            else{
                msd = "";
            }

            if (disamb == null && tag.getDisamb()){
                disambIdx = msdIdx;
                disamb = base+":"+TEIctag+":"+msd;
            }

            boolean foundMatch = false;
            for(TEILex lex: lexemes){
                if (lex.match(base, TEIctag)){
                    foundMatch = true;
                    lex.addMsd(msd, msdIdx++);
                    break;
                }
            }
            if (!foundMatch){
                TEILex newLex = new TEILex(base, TEIctag);
                newLex.addMsd(msd, msdIdx++);
                lexemes.add(newLex);
            }
        }

        if (disamb == null){
            TEILex firstLex = lexemes.get(0);
            disamb = firstLex.base+":"+firstLex.ctag+":"+firstLex.msdList.get(0).getFirst();
        }
    }
}
