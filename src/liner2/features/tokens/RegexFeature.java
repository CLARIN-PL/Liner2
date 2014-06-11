package liner2.features.tokens;

import java.util.regex.Pattern;

import liner2.structure.Tag;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class RegexFeature extends TokenFeature{
	
	private Pattern pattern;
	
	public RegexFeature(String name){
		super(name);
		this.pattern = Pattern.compile(name.substring(6));
	}
	
	public String generate(Token t, TokenAttributeIndex index){
		String orth = t.getAttributeValue(index.getIndex("orth"));
		if (pattern.matcher(orth).find())
			return "1";
		else
			return "O";
	}

	
	/*public static void main(String args[]){
		RegexFeature s = new RegexFeature("regex:^\\d+\\W\\d+$");
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	    attributeIndex.addAttribute("orth");
	    attributeIndex.addAttribute("base");
	    attributeIndex.addAttribute("ctag");
		Tag tag = new Tag("", "", true);
		Token token = new Token("a213:54", tag, attributeIndex);
		System.out.println(s.generate(token, attributeIndex));
	}*/

}
