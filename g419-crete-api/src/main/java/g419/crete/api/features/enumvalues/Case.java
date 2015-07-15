package g419.crete.api.features.enumvalues;

public enum Case {
	NOMINATIVE,
	ACCUSATIVE,
	GENITIVE,
	DATIVE,
	INSTRUMENTAL,
	LOCATIVE,
	VOCATIVE,
	OTHER;
	
	public static Case fromValue(String value){
		if("nom".equals(value)) return NOMINATIVE;
		else if ("acc".equals(value)) return ACCUSATIVE;
		else if ("gen".equals(value)) return GENITIVE;
		else if ("dat".equals(value)) return DATIVE;
		else if ("inst".equals(value)) return INSTRUMENTAL;
		else if ("loc".equals(value)) return LOCATIVE;
		else if ("voc".equals(value)) return VOCATIVE;
		
		else return OTHER;
	}
}
