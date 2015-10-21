package g419.tools.maltfeature;

import java.util.HashSet;
import java.util.Set;

import g419.liner2.api.tools.parser.MaltSentence;

/**
 * 
 * @author czuk
 *
 */
public abstract class MaltPatternNode{
	
    String label = null;

    public MaltPatternNode(String label){
        this.label = label;
    }

    
    public String getLabel(){
    	return this.label;
    }
            
    /**
     * 
     * @param sentence
     * @param tokenIdx
     * @return
     */
    public abstract boolean check(MaltSentence sentence, int tokenIdx);
    
    /**
     * Zwraca podzbiór indeksów tokenów, które spełniają warunek wierzchołka.
     * @param sentence
     * @param indecies
     * @return
     */
    public Set<Integer> filter(MaltSentence sentence, Set<Integer> indecies){
    	Set<Integer> newSet = new HashSet<Integer>();
    	for ( Integer i : indecies ){
    		if ( this.check(sentence, i)){
    			newSet.add(i);
    		}
    	}
    	return newSet;
    }
}