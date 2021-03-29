package g419.serel.structure.patternMatch;

import g419.corpus.structure.RelationDesc;
import g419.serel.ruleTree.PatternMatch;
import java.util.ArrayList;
import java.util.HashSet;

public class PatternMatchSingleResult {

  public String docName;
  public int sentenceNumber;
  public PatternMatch patternMatch;

  public PatternMatchSingleResult() {}

  public String getType() {
    return patternMatch.getRelationType();
  }


  public PatternMatchSingleResult(final ArrayList<Integer> _idsList, final PatternMatchExtraInfo _pmei) {
    this.tree = _idsList;
    this.patternMatchExtraInfo = _pmei;
  }


  public PatternMatchSingleResult(final PatternMatchSingleResult pmsr) {
    this.tree = (ArrayList<Integer>) pmsr.tree.clone();
    this.patternMatchExtraInfo = new PatternMatchExtraInfo(pmsr.patternMatchExtraInfo);
  }


  public ArrayList<Integer> tree;
  PatternMatchExtraInfo patternMatchExtraInfo;

  public int size() {
    return this.tree.size();
  }


//  @Override
//  public String toString() {
//    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\trole:[" + patternMatchExtraInfo+"]";
//    return result;
//  }

  public String description() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\t\trole:[" + patternMatchExtraInfo.description() + "]";
    return result;
  }


  public String descriptionLong() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\t\trole:[" + patternMatchExtraInfo.description() + "]" +
        "\t\t\t" + patternMatchExtraInfo.getSentence().toStringDecorated(tree, 1);
    return result;
  }

  public void concatenateWith(final PatternMatchSingleResult pmsr) {
    tree.addAll(pmsr.tree);
    patternMatchExtraInfo.getRoleMap().putAll(pmsr.patternMatchExtraInfo.getRoleMap());
  }

  public boolean isTheSameAs(final PatternMatchSingleResult pmsr) {

    if (!this.docName.equals(pmsr.docName) || this.sentenceNumber != pmsr.sentenceNumber
    ) {
      return false;
    }

    if (
        new HashSet(this.tree).equals(new HashSet(pmsr.tree))
    ) {
      return true;
    }

    return false;
  }

  public boolean isTheSameAs(final RelationDesc rd) {
    if (
        this.docName.equals(rd.getSentence().getDocument().getName())
            &&
            (this.sentenceNumber == rd.getSentenceIndex())
            &&
            this.tree.contains(rd.getFromTokenIndex() - 1)
            &&
            this.tree.contains(rd.getToTokenIndex() - 1)
    ) {
      return true;
    }

    return false;
  }

  /*
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PatternMatchSingleResult that = (PatternMatchSingleResult) o;
    return sentenceNumber == that.sentenceNumber &&
        docName.equals(that.docName) &&
        tree.equals(that.tree);
  }

  @Override
  public int hashCode() {
    return Objects.hash(docName, sentenceNumber, tree);
  }

   */
}
