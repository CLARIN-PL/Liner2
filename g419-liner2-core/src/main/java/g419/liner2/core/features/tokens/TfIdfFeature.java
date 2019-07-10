package g419.liner2.core.features.tokens;

import g419.liner2.core.tools.BaseCount;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;


public class TfIdfFeature extends TokenInDocumentFeature {

  public TfIdfFeature(String name) {
      super(name);
  }

  @Override
  public void generate(Document document) {
    BaseCount baseCount = new BaseCount(document);
    int thisFeatureIdx = document.getAttributeIndex().getIndex(this.getName());

    float tokenNumber = 0;
    for (Paragraph paragraph: document.getParagraphs()) {
      for (Sentence sentence: paragraph.getSentences()) {
        tokenNumber += sentence.getTokenNumber();
        }
      }

    for(Paragraph paragraph : document.getParagraphs()){
      for(Sentence sentence : paragraph.getSentences()){
        for(Token token : sentence.getTokens()){
	      final String base = token.getAttributeValue("base");
	      float idf = Float.parseFloat(token.getAttributeValue("key_value_idf_base"));
	      float tf = baseCount.getBaseCount(base);
	      float normTf = tf / tokenNumber;
	      float tfIdf = normTf * idf;
	      token.setAttributeValue(thisFeatureIdx, tfIdf > 0.05 ? "1" : "0");
        }
      }
    }
  }
}
