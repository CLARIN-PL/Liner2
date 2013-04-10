package liner2.features.tokens;

import liner2.structure.Token;

public class SuffixFeature extends ATokenFeature{
	
	private int n;
	
	public SuffixFeature(String name){
		super(name);
		this.n = Integer.parseInt(name.substring(name.length()-1));
	}
	
	public String generate(Token t){
		String orth = t.getAttributeValue(0);
		if (orth.length() >= n)
			return orth.substring(orth.length()-n);
		else{
			StringBuilder sb = new StringBuilder(orth);
			 for(int i = 0; i < (n - orth.length()); i++)
				 sb.insert(0,"_");
			return sb.toString();
		}
	}


}
