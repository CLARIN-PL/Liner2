package g419.toolbox.wordnet.struct;

public class LexicalRelation {

  private final LexicalUnit parent;
  private final LexicalUnit child;
  private final String relation;

  public LexicalRelation(final LexicalUnit parent, final LexicalUnit child, final String relation) {
    this.parent = parent;
    this.child = child;
    this.relation = relation;
  }

  public LexicalUnit getParent() {
    return parent;
  }

  public LexicalUnit getChild() {
    return child;
  }

  public String getRelation() {
    return relation;
  }

}
