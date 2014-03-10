package liner2.features.tokens;

import liner2.structure.Token;

public class IsNumberFeature extends TokenFeature{
	
	
	public IsNumberFeature(String name){
		super(name);
	}
	
	public String generate(Token token){
		  try  
		  {  
		    double d = Double.parseDouble(token.getAttributeValue(0));  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
		    return "0";  
		  }  
		  return "1";		
	}
}
