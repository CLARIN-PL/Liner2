package g419.liner2.core.converter;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g419.corpus.structure.*;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationCclRelToConlluBoiConverter extends Converter {

  private String annotationsTypeStartsWith;

  @Override
  public void start(final Document doc) {
    this.annotationsTypeStartsWith = "nam_";
  }


  @Override
  public void apply(final Sentence sentence) {
    enhanceTokensWithRel(sentence);
  }

  private void enhanceTokensWithRel(Sentence s) {

    // find rels that are in this sentence

    Map<Integer,List<String>> boiDescs = new HashMap<>();
    List<Annotation> anns = s.getChunks().stream().filter(a -> a.getType().startsWith(annotationsTypeStartsWith)).collect(Collectors.toList());
    anns.stream().forEach(ann->produceboiDescs(s,ann, boiDescs));
    s.getTokens().stream().forEach(t->enhanceTokensWithBOI(s, boiDescs));
  }


  private void produceboiDescs(Sentence s, Annotation ann, Map<Integer,List<String>> boiDescs) {
//    System.out.println("Producing start for ann= "+ann);
//    System.out.println("begin= "+ann.getBegin()+ "   end = "+ann.getEnd());
//    System.out.println("tbegin= "+s.getTokens().get(ann.getBegin())+ "   end = "+s.getTokens().get(ann.getEnd()));

    List<String> boiDescList = boiDescs.get(ann.getBegin());
    if(boiDescList == null ) {
      boiDescList =  new LinkedList<>();
      boiDescs.put(ann.getBegin(),boiDescList);
    }
    boiDescList.add("B-"+ann.getType());

    for (int i = ann.getBegin() + 1; i <= ann.getEnd(); i++) {
      boiDescList = boiDescs.get(i);
      if(boiDescList == null ) {
        boiDescList =  new LinkedList<>();
        boiDescs.put(i,boiDescList);
      }
      boiDescList.add("I-" + ann.getType());
    }
  }

  private void enhanceTokensWithBOI(Sentence s, Map<Integer,List<String>> boiDescs ) {
    //System.out.println("Enhancing ....");
    //s.getAttributeIndex().addAttribute("boi");
    for(int i=0;i<s.getTokens().size() ; i++ ) {
      List<String> value;
      List<String> list = boiDescs.get(i);
      if( (list == null) || (list.size()==0) ) {
        List<String> l = new ArrayList<>();
        l.add("0");
        value = l;
      } else {
        value = list;
      }
        //value = list.stream().collect(Collectors.joining("#"));


      //s.getTokens().get(i).setAttributeValue("boi",value);

      Token t = s.getTokens().get(i);

      if(t.getAttributeIndex().getIndex("misc")==-1) {
        t.getAttributeIndex().addAttribute("misc");
        t.setAttributeValue("misc","{}");
      }
      String misc = t.getAttributeValue("misc");

      //System.out.println("boimisc = "+misc);
      Map<String, Object> miscMap = null;
      try {
        try {
          miscMap =
                  new ObjectMapper().readValue(misc, HashMap.class);
        } catch( JsonMappingException jme) {
          miscMap = new HashMap();
        }
        miscMap.put("bois", value);
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(miscMap);

        //System.out.println("boisjsonOut = "+json);

        t.setAttributeValue("misc",json);
      } catch( Exception e) {
        e.printStackTrace();
      }


    }
  }



}
