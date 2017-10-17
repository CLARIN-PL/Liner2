package g419.tools.maltfeature;

import java.util.Set;

import g419.liner2.core.tools.parser.MaltSentence;

/**
 * 
 * @author czuk
 *
 */
public abstract class MaltPatternEdge{
	
    String relation = null;

    /**
     * Zwraca indeksy tokenów, które znajdują się po drugiej stronie krawędzi dla zadanego
     * węzła początkowego.
     * @param sentence
     * @param tokenIndex
     * @return
     */
    public abstract Set<Integer> findNodes(MaltSentence sentence, int tokenIndex);
    
}
