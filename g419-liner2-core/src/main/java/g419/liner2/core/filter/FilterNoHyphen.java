package g419.liner2.core.filter;

import g419.corpus.structure.Annotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilterNoHyphen extends Filter {

	private static String hasSymbol = "([-])";
	private static Pattern pattern = null;
	
	public FilterNoHyphen(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
	
		pattern = Pattern.compile(hasSymbol);
	}
	
	@Override
	public String getDescription() {
		return "Does not have hyphen";
	}

	@Override
	public Annotation pass(Annotation chunk, CharSequence charSeq) {
		Matcher m = pattern.matcher(charSeq);
		if ( !m.find() )
			return chunk;
		else
			return null;
	}

}
