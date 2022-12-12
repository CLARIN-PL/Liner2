package g419.corpus.structure;

import java.util.HashSet;
import java.util.Set;

public class NodeToken extends Token {

  private NodeToken parent = null;
  private Set<NodeToken> children = new HashSet<NodeToken>();

  public NodeToken(TokenAttributeIndex attrIdx) {
    super(attrIdx);
  }

  public void setParent(NodeToken parent) {
    this.parent = parent;
  }

  public void addChild(NodeToken child) {
    this.children.add(child);
  }

  public NodeToken getParent() {
    return this.parent;
  }

  public Set<NodeToken> getChildren() {
    return this.children;
  }
}
