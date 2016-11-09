package g419.crete.api.features.enumvalues;

public enum Person{
	PRIMA, 
	SECUNDA, 
	TERTIA,
	UNDEFINED;
	
	public static Person fromValue(String value){
		if("pri".equals(value)) return PRIMA;
		if("sec".equals(value)) return SECUNDA;
		return TERTIA;
	}
}