package g419.toolbox.wordnet.struct;

public class LexicalRelation {

  private LexicalUnit parent = null;
  private LexicalUnit child = null;
  private String relation = null;

  public LexicalRelation(LexicalUnit parent, LexicalUnit child, String relation) {
    this.parent = parent;
    this.child = child;
    this.relation = relation;
  }

  public LexicalUnit getParent() {
    return this.parent;
  }

  public LexicalUnit getChild() {
    return this.child;
  }

  public String getRelation() {
    return this.relation;
  }

}
