package g419.corpus.structure;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Builder
@Data
@Slf4j
public class RelationDesc {

  public static final String REL_STRING_DESC_SEPARATOR = ":";
  public static final String REL_STRING_DESC_ENTRY_END = "#";

  Sentence sentence;
  int sentenceIndex;

  String type;
  int fromTokenId;
  //TreeSet fromTokensTree;
  String fromType;
  int toTokenId;
  //TreeSet toTokensTree;
  String toType;
  boolean multiSentence;

  @Override
  public String toString() {
    final StringBuffer relString = new StringBuffer(this.getType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getFromTokenId()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getFromType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getToTokenId()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getToType());
    if (multiSentence) {
      relString.append(REL_STRING_DESC_SEPARATOR + "true");
    }
    //relString.append(REL_STRING_DESC_ENTRY_END);

    return relString.toString();

  }

  public String toStringFull() {
    final StringBuffer relString = new StringBuffer();

    relString.append("docName= " + this.sentence.getDocument().name + "\t\t");

    relString.append(this.getType() + REL_STRING_DESC_SEPARATOR);


    relString.append((this.getFromTokenId()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getFromType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getToTokenId()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getToType());
    if (multiSentence) {
      relString.append(REL_STRING_DESC_SEPARATOR + "true");
    }


    //relString.append("\t\t" + sentence.toString());
    relString.append("\t\t" + this.getSentenceDecorated());
    //relString.append(REL_STRING_DESC_ENTRY_END);

    return relString.toString();

  }

  public String getSentenceDecorated() {
    return sentence.toStringDecorated(new LinkedHashSet<>
        (Arrays.asList(this.getFromTokenId(), this.getToTokenId())));
  }


  static public RelationDesc from(final Relation r) {

    boolean _multiSentence = false;

    final Sentence sentenceFrom = r.getAnnotationFrom().getSentence();
    final Sentence sentenceTo = r.getAnnotationTo().getSentence();

    if (!(sentenceFrom.toString().equals(sentenceTo.toString()))) {
      _multiSentence = true;
    }

    final RelationDesc relationDesc = RelationDesc.builder()
        .type(r.getType())
        .fromTokenId(r.getAnnotationFrom().getTokens().first() + 1)
        .fromType(r.getAnnotationFrom().getType())
        .toTokenId(r.getAnnotationTo().getTokens().first() + 1)
        .toType(r.getAnnotationTo().getType())
        .multiSentence(_multiSentence)
        .build();

    relationDesc.setSentence(r.getAnnotationFrom().getSentence());

    return relationDesc;
  }

  static public RelationDesc from(final String s) {
    final String[] parts = s.split(":");
    boolean _multiSentence = false;
    if (parts.length == 4) {
      _multiSentence = Boolean.parseBoolean(parts[6]);
    }

    final RelationDesc relationDesc = RelationDesc.builder()
        .type(parts[0])
        .fromTokenId(Integer.valueOf(parts[1]))
        .fromType(parts[2])
        .toTokenId(Integer.valueOf(parts[3]))
        .toType(parts[4])
        .multiSentence(_multiSentence)
        .build();


    return relationDesc;
  }

  public boolean isTheSameAs(final RelationDesc rd) {

    return (
        this.getType().equals(rd.getType())
            &&
            (this.getFromTokenId() == rd.getFromTokenId())
            &&
            (this.getToTokenId() == rd.getToTokenId())
            &&
            (this.getFromType().equals(rd.getFromType()))
            &&
            (this.getToType().equals(rd.getToType()))
            &&
            (this.getSentenceIndex() == rd.getSentenceIndex())
            &&
            (this.getSentence().getDocument().getName().equals(rd.getSentence().getDocument().getName()))
    );
    // TODO check multiSentence scenario

  }

  public static class SortByDocument implements Comparator<RelationDesc> {

    @Override
    public int compare(final RelationDesc rd, final RelationDesc rd2) {
      int result = rd.getSentence().getDocument().getName().compareTo(rd2.getSentence().getDocument().getName());
      if (result != 0) {
        return result;
      }
      result = rd.sentenceIndex - rd2.sentenceIndex;
      if (result != 0) {
        return result;
      }
      result = rd.getType().compareTo(rd2.getType());
      if (result != 0) {
        return result;
      }
      result = rd.getFromTokenId() - rd2.getFromTokenId();
      if (result != 0) {
        return result;
      }
      result = rd.getToTokenId() - rd2.getToTokenId();
      if (result != 0) {
        return result;
      }

      return result;
    }
  }


  // for stream processing
  public boolean isNotNested() {
    return !isNested();
  }

  public boolean isNested() {

    final Set<Integer> sourceAnnotationIds = sentence.getBoiTokensIdsForTokenAndName(
        sentence.getTokenById(this.getFromTokenId()),
        this.getFromType());
    final Set<Integer> targetAnnotationIds = sentence.getBoiTokensIdsForTokenAndName(
        sentence.getTokenById(this.getToTokenId()),
        this.getToType());


    final Optional<Integer> commonElement = sourceAnnotationIds.stream().filter(targetAnnotationIds::contains).findAny();
    if (commonElement.isPresent()) {
      return true;
    }
    return false;
  }


}
