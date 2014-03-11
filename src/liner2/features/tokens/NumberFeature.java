package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Arrays;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class NumberFeature extends TokenFeature{
	
	private ArrayList<String> possible_numbers = new ArrayList<String>(Arrays.asList("sg", "pl"));
	
	public NumberFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		String ctag = token.getAttributeValue(index.getIndex("ctag"));
		if(ctag != null)
			for (String val: ctag.split(":")){
				if (this.possible_numbers.contains(val))
					return val;
			}
		return null;
	}
	

}
