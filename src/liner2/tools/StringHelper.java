package liner2.tools;

public class StringHelper {

	public static String implode(String[] strings){
		String str = "";
		for (String s : strings)
			str += (str.length()>0 ? "," : "") + s;
		return str;
	}
	
}
