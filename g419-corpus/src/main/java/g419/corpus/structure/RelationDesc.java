package g419.corpus.structure;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Arrays;

@Builder
@Data
@Slf4j
public class RelationDesc {

  public static final String REL_STRING_DESC_SEPARATOR = ":";
  public static final String REL_STRING_DESC_ENTRY_END = "#";

  Sentence sentence;
  int sentenceIndex;

  String type;
  int fromTokenIndex;
  //TreeSet fromTokensTree;
  String fromType;
  int toTokenIndex;
  //TreeSet toTokensTree;
  String toType;

  @Override
  public String toString() {
    final StringBuffer relString = new StringBuffer(this.getType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getFromTokenIndex()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getFromType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getToTokenIndex()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getToType());
    //relString.append(REL_STRING_DESC_ENTRY_END);

    return relString.toString();

  }

  public String toStringFull() {
    final StringBuffer relString = new StringBuffer();

    relString.append("docName= " + this.sentence.getDocument().name + "\t\t");

    relString.append(this.getType() + REL_STRING_DESC_SEPARATOR);


    relString.append((this.getFromTokenIndex()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getFromType() + REL_STRING_DESC_SEPARATOR);
    relString.append((this.getToTokenIndex()) + REL_STRING_DESC_SEPARATOR);
    relString.append(this.getToType());

    //relString.append("\t\t" + sentence.toString());
    relString.append("\t\t" + this.getSentenceDecorated());
    //relString.append(REL_STRING_DESC_ENTRY_END);

    return relString.toString();

  }

  public String getSentenceDecorated() {
    return sentence.toStringDecorated(new ArrayList<>
        (Arrays.asList(this.getFromTokenIndex(), this.getToTokenIndex())));
  }


  static public RelationDesc from(final Relation r) {
    final RelationDesc relationDesc = RelationDesc.builder()
        .type(r.getType())
        .fromTokenIndex(r.getAnnotationFrom().getTokens().first() + 1)
        .fromType(r.getAnnotationFrom().getType())
        .toTokenIndex(r.getAnnotationTo().getTokens().first() + 1)
        .toType(r.getAnnotationTo().getType()).build();

    relationDesc.setSentence(r.getAnnotationFrom().getSentence());

    return relationDesc;
  }


  /*
  static public RelationDesc from(final Relation r) {


    final RelationDesc relationDesc = RelationDesc.builder()
        .type(r.getType())
        .fromTokenIndex(r.getAnnotationFrom().getHead() + 1) // może być także first from tokens
        .fromTokensTree(r.getAnnotationFrom().getTokens()) // !!! inna numeracja - nie ma +1
        .fromType(r.getAnnotationFrom().getType())
        .toTokenIndex(r.getAnnotationTo().getHead() + 1)  // może być także  first from tokens
        .toTokensTree(r.getAnnotationTo().getTokens()) // !!! inna numeracja - nie ma +1
        .toType(r.getAnnotationTo().getType()).build();


    relationDesc.setSentence(r.getAnnotationFrom().getSentence());

    return relationDesc;
  }
  */


  static public RelationDesc from(final String s) {
    final String[] parts = s.split(":");
    final RelationDesc relationDesc = RelationDesc.builder()
        .type(parts[0])
        .fromTokenIndex(Integer.valueOf(parts[1]))
        .fromType(parts[2])
        .toTokenIndex(Integer.valueOf(parts[3]))
        .toType(parts[4]).build();


    return relationDesc;
  }

  public boolean isTheSameAs(final RelationDesc rd) {

    return (
        this.getType().equals(rd.getType())
            &&
            (this.getFromTokenIndex() == rd.getFromTokenIndex())
            &&
            (this.getToTokenIndex() == rd.getToTokenIndex())
            &&
            (this.getFromType().equals(rd.getFromType()))
            &&
            (this.getToType().equals(rd.getToType()))
            &&
            (this.getSentenceIndex() == rd.getSentenceIndex())
            &&
            (this.getSentence().getDocument().getName().equals(rd.getSentence().getDocument().getName()))
    );

  }


}
