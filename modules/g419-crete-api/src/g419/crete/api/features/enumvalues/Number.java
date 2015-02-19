package g419.crete.api.features.enumvalues;

public enum Number {
	SINGULARIS,
	PLURALIS,
	UNDEFINED;
	
	public static Number fromValue(String val){
		if("sg".equals(val)) return SINGULARIS;
		else if ("pl".equals(val)) return PLURALIS;
		else return UNDEFINED;
	}
}
