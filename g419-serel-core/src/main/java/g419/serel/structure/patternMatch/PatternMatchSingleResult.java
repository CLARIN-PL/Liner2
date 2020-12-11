package g419.serel.structure.patternMatch;

import java.util.ArrayList;

public class PatternMatchSingleResult {

  public String docName;
  public int sentenceNumber;

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


  @Override
  public String toString() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\tpmei:" + patternMatchExtraInfo;
    return result;
  }

  public String description() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\tpmei:" + patternMatchExtraInfo.description();
    return result;
  }

  public void concatenateWith(final PatternMatchSingleResult pmsr) {
    tree.addAll(pmsr.tree);
    patternMatchExtraInfo.getRoleMap().putAll(pmsr.patternMatchExtraInfo.getRoleMap());
  }


}
