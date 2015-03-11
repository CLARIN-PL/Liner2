package g419.crete.api.features.enumvalues;

public enum MentionType {
	NAMED_ENTITY,
	AGP,
	PRONOUN,
	NULL_VERB;
	
	public static MentionType fromValue(String val){
		if("ne".equals(val)) return NAMED_ENTITY;
		else if ("agp".equals(val)) return AGP;
		else if ("pron".equals(val)) return PRONOUN;
		else return NULL_VERB;
	}
}
