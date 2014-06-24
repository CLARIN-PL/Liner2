package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class IsNumberFeature extends TokenFeature{
	
	
	public IsNumberFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		  try  
		  {  
		    Double.parseDouble(token.getAttributeValue(index.getIndex("orth")));  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
		    return "0";  
		  }  
		  return "1";		
	}
}
