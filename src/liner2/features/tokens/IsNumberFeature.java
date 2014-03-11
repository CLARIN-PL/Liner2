package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

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
