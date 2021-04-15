package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ParseTree {

  List<SentenceLink> links = new LinkedList<>();

  public List<SentenceLink> getLinks() { return links; }

  abstract public Sentence getSentence();

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
/*
  public Set<Integer> getParentIndexesForIndexesSet(final Set<Integer> indexSet) {
    return
        getLinks().stream()
            .filter(link -> indexSet.contains(link.getSourceIndex()))
            .map(link -> link.getTargetIndex()).collect(Collectors.toSet());
  }
*/

  public Pair<List<SentenceLink>, List<SentenceLink>>
  getPathBetween(final int index1, final int index2) {
    final List<SentenceLink> parents1 = getParentsAscending(index1);
    final List<SentenceLink> parents2 = getParentsAscending(index2);
    final Pair<Integer, Integer> indexes = findIndexesToLowestCommonLink(parents1, parents2);
    //uff, it's good we have link from root to outside ...

    if (indexes == null) {
      return null;
    }

    return Pair.of(
        indexes.getLeft() == 0 ? new ArrayList<>() : parents1.subList(0, indexes.getLeft()),
        indexes.getRight() == 0 ? new ArrayList<>() : parents2.subList(0, indexes.getRight())
    );
  }

  public List<SentenceLink> getParentsAscending(final int index) {
    return getParentsAscending(index, new LinkedList<>());
  }

  List<SentenceLink> getParentsAscending(final int index, final List<SentenceLink> accumulated) {
    final Optional<SentenceLink> optOutLink = getLinksBySourceIndex(index);

    if (!optOutLink.isPresent()) {
      return accumulated;
    }
    final SentenceLink outLink = optOutLink.get();
    accumulated.add(outLink);
    return getParentsAscending(outLink.getTargetIndex(), accumulated);
  }

  /**
   * @param list1 - ordered list of all parents for first element, from element to ROOT
   * @param list2 - ordered list of all parents for second element, from element to ROOT
   * @return - indexes in above lists to first element that is _exactly_ the same ( source
   * and target and type are the same ) on both lists when travelling from element(s) to ROOT
   */
  public Pair<Integer, Integer> findIndexesToLowestCommonLink(final List<SentenceLink> list1,
                                                              final List<SentenceLink> list2) {
    for (int i = 0; i < list1.size(); i++) {
      for (int j = 0; j < list2.size(); j++) {
        if (list1.get(i).isTheSameAs(list2.get(j))) {
          return Pair.of(i, j);
        }
      }
    }

    // it should never come here !
    return null;
  }

  /**
   * Used to display structure of ParseTree on screen
   */
  private class TreeNode {

    final String name;
    final List<ParseTree.TreeNode> children = Lists.newArrayList();
    String relationWithParent = "";
    int index;

    public TreeNode(final String name) {
      this.name = name;
    }

    public TreeNode(final String name, final int i) {
      this(name);
      this.index = i;
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


    public void print(final PrintWriter pw) {
      pw.println("ROOT");
      print(pw, "", true);
      pw.println();
    }

    private void print(final PrintWriter pw, final String prefix, final boolean isTail) {
      pw.println(String.format("%s%s──(%s)── %s", prefix, isTail ? "└" : "├", relationWithParent, name));
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).print(pw, prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1)
            .print(pw, prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }

  public void printAsTree() {
    printAsTreeWithIndex(new PrintWriter(System.out));
  }

  public void printAsTreeWithIndex(final PrintWriter pw) {
    final List<ParseTree.TreeNode> nodes = new LinkedList<>();

    for (int i = 0; i < getSentence().getTokens().size(); i++) {

      final Token t = getSentence().getTokens().get(i);
      final TreeNode tn = new TreeNode(String.format("%s                    [%s] [%s]", t.getOrth(), " ", " " + i), i);
      nodes.add(tn);
    }

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




