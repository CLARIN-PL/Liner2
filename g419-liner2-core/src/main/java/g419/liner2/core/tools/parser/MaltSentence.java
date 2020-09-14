package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import g419.corpus.structure.*;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
public class MaltSentence extends ParseTree {
  //List<SentenceLink> links = Lists.newArrayList();
  private final Map<String, String> posMapping;
  private String[] maltData;
  private final Set<Annotation> annotations;
  private final Sentence sentence;

  public MaltSentence(final Sentence sent, final Map<String, String> posMapping) {
    this.posMapping = posMapping;
    maltData = convertToCoNLL(sent).stream()
        .map(r -> String.join("\t", Arrays.asList(r)))
        .toArray(String[]::new);
    annotations = sent.getChunks();
    sentence = sent;
  }

  public void setMaltDataAndLinks(final String[] output) {
    maltData = output;
    links = IntStream.range(0, output.length)
        .mapToObj(i -> new ImmutablePair<>(i, output[i].split("\t")))
        .map(p -> new SentenceLink(p.getKey(), Integer.valueOf(p.getRight()[8]) - 1, p.getRight()[9]))
        .collect(Collectors.toList());
  }

  public String[] getMaltData() {
    return maltData;
  }

  public Sentence getSentence() {
    return this.sentence;
  }

  public SentenceLink getLink(final int index) {
    return index >= this.links.size() ? null : this.links.get(index);
  }


  public Set<Annotation> getAnnotations() {
    return annotations;
  }

  private List<String[]> convertToCoNLL(final Sentence sent) {
    final List<String[]> tokens = sent.getTokens().stream()
        .map(this::tokenToConll)
        .collect(Collectors.toList());
    IntStream.range(0, tokens.size()).forEach(n -> tokens.get(n)[0] = String.valueOf(n + 1));
    return tokens;
  }

  private String[] tokenToConll(final Token token) {
    final TokenAttributeIndex attributes = token.getAttributeIndex();
    final String ctag = token.getAttributeValue(attributes.getIndex("ctag"));
    final List<String> ctagElements = Arrays.asList(ctag.split(":"));
    final String[] tokData = Stream.generate(() -> "_").limit(8).toArray(String[]::new);
    tokData[0] = String.valueOf(0);
    tokData[1] = token.getAttributeValue(attributes.getIndex("orth"));
    tokData[2] = token.getAttributeValue(attributes.getIndex("base"));
    tokData[3] = posMapping.get(ctagElements.get(0));
    tokData[4] = ctagElements.get(0);
    tokData[5] = getDefaultIfEmpty(String.join("|", ctagElements.subList(1, ctagElements.size())));
    return tokData;
  }

  private static String getDefaultIfEmpty(final String str) {
    return str == null || str.isEmpty() ? "_" : str;
  }

  private class TreeNode {

    final String name;
    final List<TreeNode> children = Lists.newArrayList();
    String relationWithParent = "";

    public TreeNode(final String name) {
      this.name = name;
    }

    public void addChild(final TreeNode node) {
      children.add(node);
    }

    public void setRelationWithParent(final String relation) {
      relationWithParent = relation;
    }

    public List<TreeNode> getChildren() {
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

  public void printAsTree(PrintWriter pw) {
    final List<TreeNode> nodes = sentence.getTokens().stream()
        .map(t -> String.format("%s                    [%s]", t.getOrth(), t.getDisambTag().toString()))
        .map(TreeNode::new)
        .collect(Collectors.toList());
    links.stream()
        .filter(l -> l.sourceIndex > -1 && l.targetIndex > -1)
        .forEach(l -> {
          nodes.get(l.targetIndex).addChild(nodes.get(l.sourceIndex));
          nodes.get(l.sourceIndex).setRelationWithParent(l.relationType);
        });
    IntStream.range(0, nodes.size())
        .filter(n -> links.get(n).targetIndex == -1)
        .mapToObj(nodes::get)
        .forEach(node -> node.print(pw));
  }






}
