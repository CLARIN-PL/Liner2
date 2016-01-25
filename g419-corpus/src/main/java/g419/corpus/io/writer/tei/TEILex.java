package g419.corpus.io.writer.tei;

import java.util.ArrayList;
import java.util.List;

import g419.corpus.io.writer.tei.Pair;

public class TEILex{

    String base;
    String ctag;
    List<Pair<String, Integer>> msdList;
    boolean disamb;
    int disambMsdIdx;

    public TEILex(String base, String ctag){
        this.base = base;
        this.ctag = ctag;
        msdList = new ArrayList<Pair<String, Integer>>();
        disamb = false;
    }

    public String getBase(){
    	return this.base;
    }
    
    public String getCtag(){
    	return this.ctag;
    }
    
    public boolean isDisamb(){
        return disamb;
    }
    
    public List<Pair<String, Integer>> getMsds(){
    	return this.msdList;
    }

    public void setDisambTrue(int msdIdx){
        if(!disamb){
            disamb = true;
            disambMsdIdx = msdIdx;
        }
    }


    public void addMsd(String msd, int idx){
        msdList.add(new Pair<String, Integer>(msd, idx));
    }

    public boolean match(String base, String ctag){
        return this.base.equals(base) && this.ctag.equals(ctag);
    }

    public int msdSize() {
        return msdList.size();
    }
        
}
