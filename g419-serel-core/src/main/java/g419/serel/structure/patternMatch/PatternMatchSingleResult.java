package g419.serel.structure.patternMatch;

import g419.corpus.structure.RelationDesc;
import g419.serel.ruleTree.PatternMatch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class PatternMatchSingleResult {

  public String docName;
  public int sentenceNumber;
  public PatternMatch patternMatch;
  public String relationType;
  public Set<String> namedEntitySet = new HashSet<>();

  // idki (nie indeksy !!!) do elementów które składają się na "rozwiązanie" matcha - wszystkie dla tego całego jednego rozwiązania
  public ArrayList<Integer> idsList;

  PatternMatchExtraInfo patternMatchExtraInfo;


  public PatternMatchSingleResult() {}

  public String getType() {
    return relationType;
  }


  public PatternMatchSingleResult(final ArrayList<Integer> _idsList,
                                  final PatternMatchExtraInfo _pmei,
                                  final String _relType) {
    this.idsList = _idsList;
    this.patternMatchExtraInfo = _pmei;
    this.relationType = _relType;
  }


  public PatternMatchSingleResult(final PatternMatchSingleResult pmsr) {
    this.idsList = (ArrayList<Integer>) pmsr.idsList.clone();
    this.patternMatchExtraInfo = new PatternMatchExtraInfo(pmsr.patternMatchExtraInfo);
    this.relationType = pmsr.relationType;
  }


  public int size() {
    return this.idsList.size();
  }


//  @Override
//  public String toString() {
//    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + ",\tindexTree=" + tree + ",\trole:[" + patternMatchExtraInfo+"]";
//    return result;
//  }

  public String description() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + "\ttype=" + getType() + ",\tidsList=" + idsList + ",\t\trole:[" + patternMatchExtraInfo.description() + "]" /* + " NEs=" + namedEntitySet */;
    return result;
  }


  public String descriptionLong() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + "\ttype=" + getType() + ",\tidsList=" + idsList + ",\t\trole:[" + patternMatchExtraInfo.description() + "]" +
        "\t\t\t" + patternMatchExtraInfo.getSentence().toStringDecorated(idsList, 1);
    return result;
  }

  public void concatenateWith(final PatternMatchSingleResult pmsr) {
    idsList.addAll(pmsr.idsList);
    //namedEntitySet.addAll(pmsr.namedEntitySet);
    patternMatchExtraInfo.getRoleMap().putAll(pmsr.patternMatchExtraInfo.getRoleMap());
    // TOREVERT
    // patternMatchExtraInfo.getToken2tagNE().putAll(pmsr.patternMatchExtraInfo.getToken2tagNE());
  }

  public boolean isTheSameAs(final PatternMatchSingleResult pmsr) {

    if (!this.getType().equals(pmsr.getType())) {
      return false;
    }

    if (!this.docName.equals(pmsr.docName) || this.sentenceNumber != pmsr.sentenceNumber
    ) {
      return false;
    }


    if (
        new HashSet(this.idsList).equals(new HashSet(pmsr.idsList))
    ) {
      return true;
    }

    return false;
  }

  public boolean isTheSameAs(final RelationDesc rd) {

//    if (docName.equals("documents/00102158")) {
//      System.out.println("RD   = " + rd.toStringFull());
//      System.out.println("PMSR = " + this.description());
//    }

    if (!this.getType().equals(rd.getType())) {
      return false;
    }


    final Set<Integer> anchorsIDs = this.patternMatchExtraInfo.getAnchorIds();

    if (
        this.docName.equals(rd.getSentence().getDocument().getName())
            &&
            (this.sentenceNumber == rd.getSentenceIndex())
            &&
            //anchorsIDs.contains(rd.getFromTokenId())
            this.idsList.contains(rd.getFromTokenId())
            &&
            //anchorsIDs.contains(rd.getToTokenId())
            this.idsList.contains(rd.getToTokenId())
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

  public static class SortByDocument implements Comparator<PatternMatchSingleResult> {

    @Override
    public int compare(final PatternMatchSingleResult p1, final PatternMatchSingleResult p2) {
      int result = p1.docName.compareTo(p2.docName);
      if (result != 0) {
        return result;
      }
      result = p1.sentenceNumber - p2.sentenceNumber;
      if (result != 0) {
        return result;
      }
      result = p1.getType().compareTo(p2.getType());
      if (result != 0) {
        return result;
      }

      return result;
    }
  }


}
