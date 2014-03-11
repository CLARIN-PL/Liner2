package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Arrays;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class CaseFeature extends TokenFeature{
		
		private ArrayList<String> possible_cases = new ArrayList<String>(Arrays.asList("nom", "gen", "dat", "acc", "inst", "loc", "voc"));
		
		public CaseFeature(String name){
			super(name);
		}
		
		public String generate(Token token, TokenAttributeIndex index){
			String ctag = token.getAttributeValue(index.getIndex("ctag"));
			if(ctag != null)
				for (String val: ctag.split(":")){
					if (this.possible_cases.contains(val))
						return val;	
				}
			return null;
		}
		

}
	
