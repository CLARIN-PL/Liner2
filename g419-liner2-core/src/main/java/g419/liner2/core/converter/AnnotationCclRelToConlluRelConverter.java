package g419.liner2.core.converter;


import g419.corpus.structure.*;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AnnotationCclRelToConlluRelConverter extends Converter {

  public static final String REL_STRING_SEPARATOR = ":";
  public static final String REL_STRING_ENTRY_END = "#";


  public void apply(final Sentence s) { throw new RuntimeException("Not implemented"); }

  public void apply(final Document doc) {

    RelationSet relations = doc.getRelations("Semantic relations");
//    System.out.println(" Retrieved "+relations.size());
    relations.forEach( this::assignRelationToToken);
  }

  private void assignRelationToToken(Relation r) {

    String sidFrom = r.getAnnotationFrom().getSentence().getId();
    String sidTo = r.getAnnotationTo().getSentence().getId();

    //System.out.println("sid1 =" + sidFrom + " sidTo =" + sidTo);

    if (!sidFrom.equals(sidTo)) {
      log.error("DOC ID: "+r.getDocument().getName()+" "+r.getType()+   ":[ <"+r.getAnnotationFrom().getSentence().getId()+">"+ r.getAnnotationFrom().getText()+" ->  <"+ r.getAnnotationTo().getSentence().getId()+">"+   r.getAnnotationTo().getText()+" ] " );
    } else {
      StringBuffer relString = new StringBuffer(r.getType() + REL_STRING_SEPARATOR);
      relString.append((r.getAnnotationFrom().getTokens().first()+1) + REL_STRING_SEPARATOR);
      relString.append(r.getAnnotationFrom().getType() + REL_STRING_SEPARATOR);
      relString.append((r.getAnnotationTo().getTokens().first()+1) + REL_STRING_SEPARATOR);
      relString.append(r.getAnnotationTo().getType() + REL_STRING_SEPARATOR);
      relString.append(REL_STRING_ENTRY_END);

     //System.out.println("RELSTRING = " + relString);
      Sentence s = r.getAnnotationFrom().getSentence();

      Token t = s.getTokens().get(r.getAnnotationFrom().getTokens().first());

      String oldValue="";
      if ( t.getAttributeIndex().getIndex("nam_rel") !=-1) {
        oldValue = t.getAttributeValue("nam_rel");
      } else {
        t.getAttributeIndex().addAttribute("nam_rel");
      }
      String newValue=oldValue+ relString.toString();
      //System.out.println("newValue = "+newValue);
      t.setAttributeValue("nam_rel",newValue);
    }
  }

}
