package liner2.features.tokens;

import liner2.structure.Token;

public class PrefixFeature extends TokenFeature{
	
	private int n;
	
	public PrefixFeature(String name){
		super(name);
		this.n = Integer.parseInt(name.substring(name.length()-1));
	}
	
	public String generate(Token t){
		String orth = t.getAttributeValue(0);
		if (orth.length() >= n)
			return t.getAttributeValue(0).substring(0,n);
		else{
			 StringBuilder sb = new StringBuilder(orth);
			 for(int i = 0; i < (n - orth.length()); i++)
				 sb.append("_");
			return sb.toString();
		}
	}

}
