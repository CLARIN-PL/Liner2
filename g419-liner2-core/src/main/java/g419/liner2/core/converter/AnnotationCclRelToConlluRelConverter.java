package g419.liner2.core.converter;


import g419.corpus.structure.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnotationCclRelToConlluRelConverter extends Converter {

  public static final String REL_STRING_SEPARATOR = ":";
  public static final String REL_STRING_ENTRY_END = ",";


  @Override
  public void apply(final Sentence s) { throw new RuntimeException("Not implemented"); }

  @Override
  public void apply(final Document doc) {

    final RelationSet relations = doc.getRelations("Semantic relations");
//    System.out.println(" Retrieved "+relations.size());
    relations.forEach(this::assignRelationToToken);
    relations.forEach(this::assignTokenRelationsToMisc);
  }

  private void assignRelationToToken(final Relation r) {

    final String sidFrom = r.getAnnotationFrom().getSentence().getId();
    final String sidTo = r.getAnnotationTo().getSentence().getId();

    //System.out.println("sid1 =" + sidFrom + " sidTo =" + sidTo);

    if (!sidFrom.equals(sidTo)) {
      log.error("DOC ID: " + r.getDocument().getName() + " " + r.getType() + ":[ <" + r.getAnnotationFrom().getSentence().getId() + ">" + r.getAnnotationFrom().getText() + " ->  <" + r.getAnnotationTo().getSentence().getId() + ">" + r.getAnnotationTo().getText() + " ] ");
    } else {
      final StringBuffer relString = new StringBuffer(r.getType() + REL_STRING_SEPARATOR);
      relString.append((r.getAnnotationFrom().getTokens().first() + 1) + REL_STRING_SEPARATOR);
      relString.append(r.getAnnotationFrom().getType() + REL_STRING_SEPARATOR);
      relString.append((r.getAnnotationTo().getTokens().first() + 1) + REL_STRING_SEPARATOR);
      relString.append(r.getAnnotationTo().getType());
      //relString.append(REL_STRING_ENTRY_END);

      //System.out.println("RELSTRING = " + relString);
      final Sentence s = r.getAnnotationFrom().getSentence();
      final Token t = s.getTokens().get(r.getAnnotationFrom().getTokens().first());

      String oldValue = "";
      if (t.getAttributeIndex().getIndex("nam_rel") != -1) {
        oldValue = t.getAttributeValue("nam_rel");
        if (!oldValue.isEmpty()) {
          oldValue += ",";
        }
      } else {
        t.getAttributeIndex().addAttribute("nam_rel");
      }
      final String newValue = oldValue + relString.toString();
      //System.out.println("newValue = "+newValue);
      t.setAttributeValue("nam_rel", newValue);
    }
  }

  private void assignTokenRelationsToMisc(final Relation r) {


    final Sentence s = r.getAnnotationFrom().getSentence();
    final Token t = s.getTokens().get(r.getAnnotationFrom().getTokens().first());
    try {

      final int index = t.getAttributeIndex().getIndex("nam_rel");
      if (index != -1) {
        String value = t.getAttributeValue("nam_rel");
        if (value != null) {  // jeśli jest null to znaczy już to dla tego tokenu robiliśmy
          value = "nam_rels=[" + value + "]";
          if (t.getAttributeIndex().getIndex("misc") == -1) {
            t.getAttributeIndex().addAttribute("misc");
          } else {
            final String misc = t.getAttributeValue("misc");
            if (!misc.isEmpty()) {
              value = misc + "|" + value;
            }
          }
          t.setAttributeValue("misc", value);
          t.setAttributeValue("nam_rel", null);
        }
      }
    } catch (final Throwable th) {
      System.out.println("Problem with token " + t + " from sentence " + s);
      th.printStackTrace();
    }


  }


}
