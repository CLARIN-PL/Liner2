package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import g419.corpus.structure.Sentence;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ParseTree {

  List<SentenceLink> links = new LinkedList<>();

  public List<SentenceLink> getLinks() { return links; }

  abstract public Sentence getSentence() ;

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



  private class TreeNode {

    final String name;
    final List<ParseTree.TreeNode> children = Lists.newArrayList();
    String relationWithParent = "";

    public TreeNode(final String name) {
      this.name = name;
    }

    public void addChild(final ParseTree.TreeNode node) {
      children.add(node);
    }

    public void setRelationWithParent(final String relation) {
      relationWithParent = relation;
    }

    public List<ParseTree.TreeNode> getChildren() {
      return children;
    }



    public void print(PrintWriter pw) {
      pw.println("ROOT");
      print(pw,"", true);
      pw.println();
    }

    private void print(final PrintWriter pw,final String prefix, final boolean isTail) {
      pw.println(String.format("%s%s──(%s)── %s", prefix, isTail ? "└" : "├", relationWithParent, name));
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).print(pw,prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1)
            .print(pw,prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }

  public void printAsTree() {
    printAsTree(new PrintWriter(System.out));
  }

  public void printAsTree(PrintWriter pw ) {
    final List<ParseTree.TreeNode> nodes = getSentence().getTokens().stream()

        //.map(t -> String.format("%s                    [%s]", t.getOrth(), t.getDisambTag().toString()))
        .map(t -> String.format("%s                    [%s]", t.getOrth()," "))
        .map(TreeNode::new)
        .collect(Collectors.toList());

    links.stream()
        .filter(l -> l.sourceIndex > -1 && l.targetIndex > -1)
        .forEach(l -> {
          nodes
              .get(l.targetIndex)
              .addChild(nodes
                  .get(l.sourceIndex));
          nodes.get(l.sourceIndex).setRelationWithParent(l.relationType);
        });

    IntStream.range(0, nodes.size())
        .filter(n -> links.get(n).targetIndex == -1)
        .mapToObj(nodes::get)
        .forEach(node -> node.print(pw));
  }

}




