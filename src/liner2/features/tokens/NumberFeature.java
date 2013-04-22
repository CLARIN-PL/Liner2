package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Arrays;

import liner2.structure.Token;

public class NumberFeature extends TokenFeature{
	
	private ArrayList<String> possible_numbers = new ArrayList<String>(Arrays.asList("sg", "pl"));
	
	public NumberFeature(String name){
		super(name);
	}
	
	public String generate(Token token){
		String ctag = token.getAttributeValue(2);
		if(ctag != null)
			for (String val: ctag.split(":")){
				if (this.possible_numbers.contains(val))
					return val;
			}
		return null;
	}
	

}
