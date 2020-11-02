package g419.liner2.core.converter;


import com.fasterxml.jackson.databind.JsonMappingException;
import g419.corpus.structure.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class AnnotationCclRelToConlluRelConverter extends Converter {



  public void apply(final Sentence s) { throw new RuntimeException("Not implemented"); }

  public void apply(final Document doc) {

    RelationSet relations = doc.getRelations("Semantic relations");
//    System.out.println(" Retrieved "+relations.size());
    relations.forEach( this::assignRelationToToken);
  }

  private void assignRelationToToken(Relation r) {

    String sidFrom = r.getAnnotationFrom().getSentence().getId();
    String sidTo = r.getAnnotationTo().getSentence().getId();

    System.out.println("sid1 =" + sidFrom + " sidTo =" + sidTo);

    if (!sidFrom.equals(sidTo)) {
      log.error("DOC ID: " + r.getDocument().getName() + " " + r.getType() + ":[ <" + r.getAnnotationFrom().getSentence().getId() + ">" + r.getAnnotationFrom().getText() + " ->  <" + r.getAnnotationTo().getSentence().getId() + ">" + r.getAnnotationTo().getText() + " ] ");
    } else {
      RelationDesc rd = RelationDesc.from(r);

      System.out.println("Checking relation :"+r);
      System.out.println("Checking rd :"+rd);


      Sentence s = r.getAnnotationFrom().getSentence();
      Token t = s.getTokens().get(r.getAnnotationFrom().getTokens().first());

      if(t.getAttributeIndex().getIndex("misc")==-1) {
        t.getAttributeIndex().addAttribute("misc");
        t.setAttributeValue("misc","{}");
      }
      String misc = t.getAttributeValue("misc");

      System.out.println("misc = "+misc);
      Map<String, Object> miscMap = null;
      try {
      try {
        miscMap =
                new ObjectMapper().readValue(misc, HashMap.class);
      } catch( JsonMappingException jme) {
        miscMap = new HashMap();
      }

        if (!miscMap.containsKey("nam_rels")) {
          miscMap.put("nam_rels", new ArrayList<RelationDesc>());
        }
        List<RelationDesc> rels = (List<RelationDesc>) miscMap.get("nam_rels");
        rels.add(rd);
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(miscMap);

        System.out.println("jsonOut = "+json);

        t.setAttributeValue("misc",json);
      } catch( Exception e) {
        e.printStackTrace();
      }


/*
     //System.out.println("RELSTRING = " + relString);
      Sentence s = r.getAnnotationFrom().getSentence();

      if ( t.getAttributeIndex().getIndex("nam_rel") !=-1) {
        oldValue = t.getAttributeValue("nam_rel");
      } else {
        t.getAttributeIndex().addAttribute("nam_rel");
      }
      Token t = s.getTokens().get(r.getAnnotationFrom().getTokens().first());
      String oldValue="";
      if ( t.getAttributeIndex().getIndex("nam_rel") !=-1) {
        oldValue = t.getAttributeValue("nam_rel");
      } else {
        t.getAttributeIndex().addAttribute("nam_rel");
      }
      String newValue=oldValue+ relStringDesc;
      //System.out.println("newValue = "+newValue);
      t.setAttributeValue("nam_rel",newValue);
    }
  }

*/
    }
  }
}
