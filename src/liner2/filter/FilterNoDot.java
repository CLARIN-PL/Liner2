package liner2.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.structure.Chunk;

public class FilterNoDot extends Filter {

	private static String hasSymbol = "([.])";
	private static Pattern pattern = null;
	
	public FilterNoDot(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
	
		pattern = Pattern.compile(hasSymbol);
	}
	
	@Override
	public String getDescription() {
		return "Does not have dot";
	}

	@Override
	public Chunk pass(Chunk chunk, CharSequence charSeq) {
		Matcher m = pattern.matcher(charSeq);
		if ( !m.find() )
			return chunk;
		else
			return null;
	}

}
