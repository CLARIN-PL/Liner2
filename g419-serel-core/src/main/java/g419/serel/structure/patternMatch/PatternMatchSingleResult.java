package g419.serel.structure.patternMatch;

import java.util.List;

public class PatternMatchSingleResult {

  public String docName;
  public int sentenceNumber;

  public PatternMatchSingleResult(final List<Integer> _idsList, final PatternMatchExtraInfo _pmei) {
    this.tree = _idsList;
    this.patternMatchExtraInfo = _pmei;
  }

  List<Integer> tree;
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


}
