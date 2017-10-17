//package g419.liner2.api.features.tokens;
//
//import g419.corpus.structure.Token;
//import g419.corpus.structure.TokenAttributeIndex;
//
//import java.util.Arrays;
//import java.util.Set;
//import java.util.Map;
//
//import org.maltparser.core.helper.HashMap;
//import org.maltparser.core.helper.HashSet;
//
//
//
//public class BroadClassFeature extends TokenFeature{
//	private static final Map<String, Set<String>> BROAD_CLASSES = new HashMap<String, Set<String>>(){{
//			put("verb", new HashSet<String>(Arrays.asList(new String[]{"pact","ppas","winien","praet","bedzie","fin","impt","aglt","ger","imps","inf","pant","pcon"})));
//			put("noun", new HashSet<String>(Arrays.asList(new String[]{"subst", "depr", "xxs", "ger", "ppron12", "ppron3"})));
//			put("pron", new HashSet<String>(Arrays.asList(new String[]{"ppron12", "ppron3", "siebie"})));
//		}
//	};
//	
//	public BroadClassFeature(String name) {
//		super(name);
//	}
//	@Override
//	public String generate(Token token, TokenAttributeIndex index) {
//		
//	}
//			
//
//}
