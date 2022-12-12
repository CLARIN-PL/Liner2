package g419.serel.tools;

import g419.corpus.structure.Document;
import g419.corpus.structure.NodeToken;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.ComboSentence;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.liner2.core.tools.parser.SentenceLink;

public class ComboParseTreeGenerator implements ParseTreeGenerator {

  Document document;

  public ComboParseTreeGenerator(Document doc) {
    this.document = doc;
  }

 public ParseTree generate(Sentence sourceSentence) throws Exception {

   Sentence sentence = document.getSentences().get( sourceSentence.getOrd());

   ComboSentence comboSentence = new ComboSentence(sentence);
   for (int i=0;i<sentence.getTokens().size();i++) {
     NodeToken nodeToken = (NodeToken)sentence.getTokens().get(i);
     int parentIndex = -1;
     if(nodeToken.getParent()!=null) {
       parentIndex = Integer.parseInt(nodeToken.getParent().getAttributeValue("id")) - 1;
     }
     int sourceIndex = i;
     //int sourceIndex = Integer.parseInt(nodeToken.getAttributeValue("id"))-1;

     String relationType = nodeToken.getAttributeValue(7); //DEPREL
     SentenceLink sl = new SentenceLink(sourceIndex, parentIndex,relationType);
     comboSentence.getLinks().add(sl);
   }
   return comboSentence;
 }




}
