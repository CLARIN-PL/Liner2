package g419.serel.tools;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Document;
import g419.corpus.structure.NodeToken;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.tools.parser.*;
import java.util.HashMap;

public class ComboParseTreeGenerator implements ParseTreeGenerator {

  Document document;

  public ComboParseTreeGenerator(Document doc) {
    this.document = doc;
  }

//
//  public ComboParseTreeGenerator(String fileName) throws Exception{
//    try ( final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(fileName, "conllu") )
//    {
//      document = reader.nextDocument();
//    }
//
////    for (Sentence s : document.getParagraphs().get(0).getSentences() ) {
////      for(Token t: s.getTokens()) {
////        System.out.println("T= "+t);
////        System.out.println("T = ["+t.getAttributeValue("id")+":"+t.getAttributeValue("head")+ "]");
////      }
////    }
//
//  }

 public ParseTree generate(Sentence sourceSentence) throws Exception {

   Sentence sentence = document.getSentences().get( sourceSentence.getOrd());

   ParseTree parseTree = new ParseTree();
   for (int i=0;i<sentence.getTokens().size();i++) {
     NodeToken nodeToken = (NodeToken)sentence.getTokens().get(i);
     if(nodeToken.getParent()!=null) {
       int parentIndex = Integer.parseInt(nodeToken.getParent().getAttributeValue("id"));
       int sourceIndex = Integer.parseInt(nodeToken.getAttributeValue("id"));
       String relationType = nodeToken.getAttributeValue(7); //DEPREL
       SentenceLink sl = new SentenceLink(sourceIndex, parentIndex,relationType);
       parseTree.getLinks().add(sl);
     }
   }

   return parseTree;
 }




}
