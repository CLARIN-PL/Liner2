package g419.liner2.core.features.tokens;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;


public class TfIdfFeature extends TokenInDocumentFeature {

  public TfIdfFeature(String name) {
    super(name);
  }

  BaseNumberFeature baseNumberGenerator = new BaseNumberFeature("base_number");

  @Override
  public void generate(Document document) {
    this.baseNumberGenerator.generate(document);
    int thisFeatureIdx = document.getAttributeIndex().getIndex(this.getName());

    float tokenNumber = (float) document.getTokenNumber();
    
    for(Paragraph paragraph : document.getParagraphs()){
      for(Sentence sentence : paragraph.getSentences()){
        for(Token token : sentence.getTokens()){
	  final String base = token.getAttributeValue("base");
	  float idf = Float.parseFloat(token.getAttributeValue("key_value_idf_base"));
	  float tf = (float) this.baseNumberGenerator.getBaseCount(base);
	  float normTf = tf / tokenNumber;
	  float tfIdf = normTf * idf;
	  token.setAttributeValue(thisFeatureIdx, tfIdf > 0.05 ? "1" : "0");
	}
      }
    }
  }

}
