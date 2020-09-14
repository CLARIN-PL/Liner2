package g419.serel.tools;

import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.ParseTreeGenerator;

public class MaltParseTreeGenerator implements ParseTreeGenerator {

  private MaltParser malt;

  public MaltParseTreeGenerator(MaltParser m) {
    this.malt = m;
  }

 public ParseTree generate(Sentence sentence) throws Exception {

   final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
   malt.parse(maltSentence);

   return maltSentence;
 }

}
