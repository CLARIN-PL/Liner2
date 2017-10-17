package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class StructureFeature extends TokenFeature{
	private boolean packed;
	
	public StructureFeature(String name){
		super(name);
		this.packed = name.charAt(name.length()-1) == 'P';		
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		String pattern = "";
		for (int i = 0, n = orth.length(); i < n; i ++){
			int codePoint = orth.codePointAt(i);
			if (Character.isLetter(codePoint)){
				if (Character.isUpperCase(codePoint))
					pattern += "X";
				else 
					pattern += "x";
			}
			else if (Character.isDigit(codePoint))
				pattern += "d";
			else 
				pattern += "-";
		}
		if (this.packed){
			pattern = pattern.replaceAll("x+", "x");
			pattern = pattern.replaceAll("X+", "X");
			pattern = pattern.replaceAll("d+", "d");
			pattern = pattern.replaceAll("-+", "-");
		}
		
		return pattern;
	}
	
	/*public static void main(String args[]){
		StructureFeature s = new StructureFeature("struct");
		StructureFeature sP = new StructureFeature("structP");
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
        attributeIndex.addAttribute("orth");
        attributeIndex.addAttribute("base");
        attributeIndex.addAttribute("ctag");
		Tag tag = new Tag("", "", true);
		Token token = new Token("123...abCDEfg:OOoo0982:4567", tag, attributeIndex);
		System.out.println(s.generate(token, attributeIndex));
		System.out.println(sP.generate(token, attributeIndex));
	}*/
}
