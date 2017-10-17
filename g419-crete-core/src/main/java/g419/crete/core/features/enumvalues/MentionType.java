package g419.crete.core.features.enumvalues;

import java.util.Arrays;
import java.util.Set;

import org.maltparser.core.helper.HashSet;

public enum MentionType {
	NAMED_ENTITY,
	AGP,
	PRONOUN,
	NULL_VERB,
	NONE;
	//tokenBase == "on" || tokenBase == "mu" || tokenBase == "jej" || tokenBase == "ten" || tokenBase == "tu"
	private static final HashSet<String> pronounBases = new HashSet<String>(Arrays.asList(new String[]{
			"on", "mu", "jej", "ten", "tu", "tam", "mój", "twój", "nasz", "wasz"
	}));
	
	public static boolean isPronounBase(String baseForm){return pronounBases.contains(baseForm);}
	
	public static MentionType fromValue(String val){
		if("ne".equals(val)) return NAMED_ENTITY;
		else if ("agp".equals(val)) return AGP;
		else if ("pron".equals(val)) return PRONOUN;
		else return NULL_VERB;
	}
}
