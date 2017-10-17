package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.Arrays;


public class PersonFeature extends TokenFeature{
	
	private ArrayList<String> possible_persons = new ArrayList<String>(Arrays.asList("pri", "sec", "ter"));
	
	public PersonFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		String ctag = token.getAttributeValue(index.getIndex("ctag"));
		if(ctag != null)
			for (String val: ctag.split(":")){
				if (this.possible_persons.contains(val))
					return val;
			}
		return null;
	}
	

}
