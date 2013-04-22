package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Arrays;

import liner2.structure.Token;

public class ClassFeature extends TokenFeature{
	
	private ArrayList<String> possible_classes = new ArrayList<String>(Arrays.asList("subst", "depr", "num", "numcol", "adj", "adja", "adjp", "adv", "ppron12",
 			"ppron3", "siebie", "fin", "bedzie", "aglt", "praet", "impt", "imps",
 			"inf",  "pcon", "pant", "ger", "pact", "ppas",  "winien", "pred", "prep",
 			"conj", "qub", "xxs", "xxx", "ign", "interp"));
	
	public ClassFeature(String name){
		super(name);
	}
	
	
	public String generate(Token token){
		String ctag = token.getAttributeValue(2);
		if(ctag != null)
			for (String val: ctag.split(":")){
				if (this.possible_classes.contains(val))
					return val;
			}
		return null;
	}
	

}