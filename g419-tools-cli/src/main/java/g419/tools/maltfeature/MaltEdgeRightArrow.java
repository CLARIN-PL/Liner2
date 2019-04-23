package g419.tools.maltfeature;

import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;

import java.util.HashSet;
import java.util.Set;

/**
 * @author czuk
 */
public class MaltEdgeRightArrow extends MaltPatternEdge {

  public MaltEdgeRightArrow(final String relationType) {
    relation = relationType;
  }

  @Override
  public Set<Integer> findNodes(final MaltSentence sentence, final int tokenIndex) {
    final Set<Integer> nodes = new HashSet<>();
    final MaltSentenceLink link = sentence.getLink(tokenIndex);
    if (link != null
        && link.getRelationType().equals(relation)
        && link.getTargetIndex() >= 0) {
      nodes.add(link.getTargetIndex());
    }
    return nodes;
  }

  @Override
  public String toString() {
    return " --(" + relation + ")--> ";
  }


}
