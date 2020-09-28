package g419.liner2.core.converter;


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

    Map<Integer,List<String>> boiStrings = new HashMap<>();
    List<Annotation> anns = s.getChunks().stream().filter(a -> a.getType().startsWith(annotationsTypeStartsWith)).collect(Collectors.toList());
    anns.stream().forEach(ann->produceBOIStrings(s,ann, boiStrings));
    s.getTokens().stream().forEach(t->enhanceTokensWithBOI(s, boiStrings));
  }


  private void produceBOIStrings(Sentence s, Annotation ann, Map<Integer,List<String>> boiStrings) {
//    System.out.println("Producing start for ann= "+ann);
//    System.out.println("begin= "+ann.getBegin()+ "   end = "+ann.getEnd());
//    System.out.println("tbegin= "+s.getTokens().get(ann.getBegin())+ "   end = "+s.getTokens().get(ann.getEnd()));

    List<String> boiStringList = boiStrings.get(ann.getBegin());
    if(boiStringList == null ) {
      boiStringList =  new LinkedList<>();
      boiStrings.put(ann.getBegin(),boiStringList);
    }
    boiStringList.add("B-" + ann.getType());

    for (int i = ann.getBegin() + 1; i <= ann.getEnd(); i++) {
      boiStringList = boiStrings.get(i);
      if(boiStringList == null ) {
        boiStringList =  new LinkedList<>();
        boiStrings.put(i,boiStringList);
      }
      boiStringList.add("I-" + ann.getType());
    }
  }

  private void enhanceTokensWithBOI(Sentence s, Map<Integer,List<String>> boiStrings ) {
    s.getAttributeIndex().addAttribute("boi");
    for(int i=0;i<s.getTokens().size() ; i++ ) {
      String value;
      List<String> list = boiStrings.get(i);
      if( (list == null) || (list.size()==0) )
        value = "O";
      else
        value = list.stream().collect(Collectors.joining("#"));

      s.getTokens().get(i).setAttributeValue("boi",value);
    }
  }



}
