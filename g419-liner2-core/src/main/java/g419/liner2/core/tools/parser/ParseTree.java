package g419.liner2.core.tools.parser;

import g419.corpus.structure.Sentence;
import org.apache.commons.lang3.tuple.Pair;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParseTree {


  List<SentenceLink> links = new LinkedList<>();

  public List<SentenceLink> getLinks() { return links; }

  /**
   * Zwraca listę linków wskazujących na token o wskazanym indeksie.
   *
   * @param index
   * @return
   */

   public List<SentenceLink> getLinksByTargetIndex(final int index) {
    return getLinks().stream()
        .filter(link -> link.getTargetIndex() == index)
        .collect(Collectors.toList());
  }

   public Optional<SentenceLink> getLinksBySourceIndex(final int index) {
    return getLinks().stream()
        .filter(link -> link.getSourceIndex() == index)
        .findFirst();
  }

   public Pair<List<SentenceLink>,List<SentenceLink>>
  getPathBetween(int index1, int index2) {
    List<SentenceLink> parents1 = getParentsAscending(index1);
    List<SentenceLink> parents2 = getParentsAscending(index2);
    Pair<Integer,Integer> indexes = findIndexesToLowestCommonLink(parents1,parents2);

    if (indexes == null)
      return null;

    return Pair.of(
        parents1.subList(0,indexes.getLeft()+1),
        parents2.subList(0,indexes.getRight()+1)
    );
  }

   public List<SentenceLink> getParentsAscending(final int index) {
    return getParentsAscending(index,new LinkedList<>());
  }

   List<SentenceLink> getParentsAscending(final int index, List<SentenceLink> accumulated) {
    Optional<SentenceLink> optOutLink = getLinksBySourceIndex(index);

    if(!optOutLink.isPresent()) {
      return accumulated;
    }
    SentenceLink outLink = optOutLink.get();
    accumulated.add(outLink);
    return getParentsAscending(outLink.getTargetIndex(),accumulated);
  }

   public Pair<Integer,Integer> findIndexesToLowestCommonLink(List<SentenceLink> list1,
                                                             List<SentenceLink> list2) {
    for(int i=0;i<list1.size();i++)
      for(int j=0;j<list2.size();j++)
      {
        if(list1.get(i).isTheSameAs(list2.get(j)))
          return Pair.of(i,j);
      }

    // it should never come here !
    return null;
  }

}
