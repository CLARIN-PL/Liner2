package g419.liner2.api.filter;

import g419.corpus.structure.Annotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilterHasVowel extends Filter {

	private static String hasVowel = "(a|ą|e|ę|y|u|i|o|ó|A|Ą|E|Ę|Y|U|I|O|Ó)";
	private static Pattern pattern = null;
	
	public FilterHasVowel(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
		this.appliesTo.add("CITY_NAM");
		
		pattern = Pattern.compile(hasVowel);
	}
	
	@Override
	public String getDescription() {
		return "Has at least one vowel";
	}

	@Override
	public Annotation pass(Annotation chunk, CharSequence charSeq) {
		Matcher m = pattern.matcher(charSeq);
		if ( m.find() )
			return chunk;
		else
			return null;
	}

}
