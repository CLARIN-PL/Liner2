package g419.tools.maltfeature;

import java.util.HashSet;
import java.util.Set;

import g419.liner2.api.tools.parser.MaltSentence;
import g419.liner2.api.tools.parser.MaltSentenceLink;

/**
 * 
 * @author czuk
 *
 */
public class MaltEdgeRightArrow extends MaltPatternEdge {

	public MaltEdgeRightArrow(String relationType) {
		this.relation = relationType;
	}
	
	@Override
	public Set<Integer> findNodes(MaltSentence sentence, int tokenIndex){
		Set<Integer> nodes = new HashSet<Integer>();
		MaltSentenceLink link = sentence.getLink(tokenIndex);
		if ( link != null 
				&& link.getRelationType().equals(this.relation)
				&& link.getTargetIndex() >=0 ){
			nodes.add(link.getTargetIndex());
		}
		return nodes;
	}
	
	@Override
    public String toString(){
        return " --(" + this.relation + ")--> ";
    }


}
