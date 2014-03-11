package liner2.features.tokens;

import java.util.ArrayList;

import liner2.structure.Sentence;
import liner2.structure.Token;

public class Agr1Feature extends TokenInSentenceFeature{
	
	public Agr1Feature(String name){
		super(name);
	}
	
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().addAttribute(this.getName()); 
		int caseIdx = sentence.getAttributeIndex().addAttribute("case");
		int numberIdx = sentence.getAttributeIndex().addAttribute("number");
		int genderIdx = sentence.getAttributeIndex().addAttribute("gender");
		ArrayList<Token> tokens = sentence.getTokens();
		tokens.get(0).setAttributeValue(thisFeatureIdx, "NULL");
		for (int i=1; i<sentence.getTokenNumber(); i++){
			String caseValThis = tokens.get(i).getAttributeValue(caseIdx);
			String numberValThis = tokens.get(i).getAttributeValue(numberIdx);
			String genderValThis = tokens.get(i).getAttributeValue(genderIdx);
			String caseValPrev = tokens.get(i-1).getAttributeValue(caseIdx);
			String numberValPrev = tokens.get(i-1).getAttributeValue(numberIdx);
			String genderValPrev = tokens.get(i-1).getAttributeValue(genderIdx);
			
			if(caseValThis == null || caseValPrev == null
			|| numberValThis == null || numberValPrev == null
			|| genderValThis == null || genderValPrev == null)
				tokens.get(i).setAttributeValue(thisFeatureIdx, "NULL");
			else if(caseValThis.equals(caseValPrev)
					&& numberValThis.equals(numberValPrev)
					&& genderValThis.equals(genderValPrev))
				tokens.get(i).setAttributeValue(thisFeatureIdx, "1");
			else
				tokens.get(i).setAttributeValue(thisFeatureIdx, "0");
		}
		
	}
}
