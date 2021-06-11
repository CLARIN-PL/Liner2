package g419.serel.structure.patternMatch;

import g419.corpus.structure.RelationDesc;
import g419.serel.ruleTree.PatternMatch;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class PatternMatchSingleResult {

  public String docName;
  public int sentenceNumber;
  public PatternMatch patternMatch;
  public String relationType;

  // idki (nie indeksy !!!) do elementów które składają się na "rozwiązanie" matcha - wszystkie dla tego całego jednego rozwiązania
  //public ArrayList<Integer> idsList;
  public LinkedHashSet<Integer> idsList;
  public PatternMatchExtraInfo patternMatchExtraInfo;


  public PatternMatchSingleResult() {}

  public String getRelationType() {
    return relationType;
  }


  public PatternMatchSingleResult(final LinkedHashSet<Integer> _idsList,
                                  final PatternMatchExtraInfo _pmei,
                                  final String _relType) {
    this.idsList = _idsList;
    this.patternMatchExtraInfo = _pmei;
    this.relationType = _relType;
  }


  public PatternMatchSingleResult(final PatternMatchSingleResult pmsr) {
    this.idsList = (LinkedHashSet<Integer>) pmsr.idsList.clone();
    this.patternMatchExtraInfo = new PatternMatchExtraInfo(pmsr.patternMatchExtraInfo);
    this.relationType = pmsr.relationType;
    this.docName = pmsr.docName;
    this.sentenceNumber = pmsr.sentenceNumber;
    this.patternMatch = pmsr.patternMatch;
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
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + "\ttype=" + getRelationType() + ",\tidsList=" + idsList + ",\t\trole:[" + patternMatchExtraInfo.description() + "]" /* + " NEs=" + namedEntitySet */;
    return result;
  }


  public String descriptionLong() {
    final String result = "docName= " + docName + ",\t\tsentnId=" + sentenceNumber + "\ttype=" + getRelationType() + ",\tidsList=" + idsList + ",\t\trole:[" + patternMatchExtraInfo.description() + "]" +
        "\t\t\t" + patternMatchExtraInfo.getSentence().toStringDecorated(idsList, 1);
    return result;
  }

  public boolean haveNotCommonId(final PatternMatchSingleResult pmsr) {

    final Optional<Integer> commonElement = this.idsList.stream().filter(pmsr.idsList::contains).findAny();
    if (commonElement.isPresent()) {
      return false;
    }
    return true;
  }


  public void concatenateWith(final PatternMatchSingleResult pmsr) {

    idsList.addAll(pmsr.idsList);
    //namedEntitySet.addAll(pmsr.namedEntitySet);
    patternMatchExtraInfo.getRoleMap().putAll(pmsr.patternMatchExtraInfo.getRoleMap());
    patternMatchExtraInfo.getToken2tagNE().putAll(pmsr.patternMatchExtraInfo.getToken2tagNE());
  }


  public boolean isTheSameAs(final PatternMatchSingleResult pmsr) {

    if (!this.getRelationType().equals(pmsr.getRelationType())) {
      return false;
    }

    if (!this.docName.equals(pmsr.docName) || this.sentenceNumber != pmsr.sentenceNumber
    ) {
      return false;
    }

    if (!(this.patternMatchExtraInfo.getRole("e1").namedEntity.equals(pmsr.patternMatchExtraInfo.getRole("e1").namedEntity))) {
      return false;
    }

    if (!(this.patternMatchExtraInfo.getRole("e2").namedEntity.equals(pmsr.patternMatchExtraInfo.getRole("e2").namedEntity))) {
      return false;
    }

    if (
        !(this.idsList.equals(pmsr.idsList))
    ) {
      return false;
    }


//    */
//    if (
//        !(new HashSet(this.idsList).equals(new HashSet(pmsr.idsList)))
//    ) {
//      return false;
//    }
//    */
//
//
//
///*
//    if (!(patternMatchExtraInfo.getRole("e1").namedEntity.equals(patternMatchExtraInfo.getRole("e2").namedEntity))) {
//      return true;
//    }
//*/
//
////    System.out.println(" COMPARING TWO PMSRS");
////    System.out.println(" this = " + this.description());
////    System.out.println(" pmsr = " + pmsr.description());


    if (
        this.patternMatchExtraInfo.getRoleE1Ids().equals(pmsr.patternMatchExtraInfo.getRoleE1Ids())
            &&
            this.patternMatchExtraInfo.getRoleE2Ids().equals(pmsr.patternMatchExtraInfo.getRoleE2Ids())
    ) {
//      System.out.println("  TRUE");
      return true;
    }
//    System.out.println("  FALSE");
    return false;
  }

  public boolean isTheSameAs(final RelationDesc rd) {

//    if (docName.equals("documents/00102158")) {
//      System.out.println("RD   = " + rd.toStringFull());
//      System.out.println("PMSR = " + this.description());
//    }

    if (!this.getRelationType().equals(rd.getType())) {
      return false;
    }

    if (!this.patternMatchExtraInfo.getRole("e1").namedEntity.equals(rd.getFromType())) {
      return false;
    }

    if (!this.patternMatchExtraInfo.getRole("e2").namedEntity.equals(rd.getToType())) {
      return false;
    }


    final Set<Integer> fromIds = this.patternMatchExtraInfo.getRoleE1Ids();
//    System.out.println("From ids = " + fromIds);
//    System.out.println(" cond =" + fromIds.contains(rd.getFromTokenId()));

    final Set<Integer> targetIds = this.patternMatchExtraInfo.getRoleE2Ids();
//    System.out.println("target ids = " + targetIds);
//    System.out.println(" cond =" + targetIds.contains(rd.getToTokenId()));


    if (
        this.docName.equals(rd.getSentence().getDocument().getName())
            &&
            (this.sentenceNumber == rd.getSentenceIndex())
            &&
            fromIds.contains(rd.getFromTokenId())
            &&
            targetIds.contains(rd.getToTokenId())
    ) {
//      System.out.println("Returning true !!!");
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
      result = p1.getRelationType().compareTo(p2.getRelationType());
      if (result != 0) {
        return result;
      }

      return result;
    }
  }


  public RelationDesc getAsRelationDesc() {

    final RelationDesc relationDesc = RelationDesc.builder()
        .type(getRelationType())
        .fromTokenId(patternMatchExtraInfo.getRoleE1MinId())
        .fromType(patternMatchExtraInfo.getNEForRole("e1"))
        .toTokenId(patternMatchExtraInfo.getRoleE2MinId())
        .toType(patternMatchExtraInfo.getNEForRole("e2"))
        .multiSentence(false)
        .build();

    return relationDesc;
  }


}
