package g419.liner2.api.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.Arrays;


public class ClassFeature extends TokenFeature{
	
	private ArrayList<String> possible_classes = new ArrayList<String>(Arrays.asList("subst", "depr", "num", "numcol", "adj", "adja", "adjp", "adv", "ppron12",
 			"ppron3", "siebie", "fin", "bedzie", "aglt", "praet", "impt", "imps",
 			"inf",  "pcon", "pant", "ger", "pact", "ppas",  "winien", "pred", "prep",
 			"conj", "qub", "xxs", "xxx", "ign", "interp"));
	
	public ClassFeature(String name){
		super(name);
	}
	
	
	public String generate(Token token, TokenAttributeIndex index){
		String ctag = token.getAttributeValue(index.getIndex("ctag"));
		if(ctag != null)
			for (String val: ctag.split(":")){
				if (this.possible_classes.contains(val))
					return val;
			}
		return null;
	}
	

}