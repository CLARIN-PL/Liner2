package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;

import java.util.HashSet;
import java.util.Set;

/**
 * @author czuk
 */
public class MaltEdgeLeftArrow extends MaltPatternEdge {

  public MaltEdgeLeftArrow(String relationType) {
    this.relation = relationType;
  }

  @Override
  public Set<Integer> findNodes(MaltSentence sentence, int tokenIndex) {
    Set<Integer> nodes = new HashSet<Integer>();
    for (MaltSentenceLink link : sentence.getLinksByTargetIndex(tokenIndex)) {
      if (link.getSourceIndex() >= 0 && link.getRelationType().equals(this.relation)) {
        nodes.add(link.getSourceIndex());
      }
    }
    return nodes;
  }

  @Override
  public String toString() {
    return " <--(" + this.relation + ")-- ";
  }

}