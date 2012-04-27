package liner2.chunker;

import java.util.ArrayList;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.tools.ParameterException;

/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */

public class HeuristicChunker extends Chunker {
	
	private String ATTR_PERSON_FIRST_NAM = "person_first_nam";
	private String ATTR_PERSON_LAST_NAM = "person_last_nam";
	private String ATTR_CITY_NAM_NAM = "person_last_nam";
	private String ATTR_COUNTRY_NAM = "person_last_nam";
	private String ATTR_ROAD_NAM = "person_last_nam";
	
	private ArrayList<String> rules = null;
	
	public HeuristicChunker() {}
	
	public HeuristicChunker(String[] rules) throws ParameterException {
		this.rules = new ArrayList<String>();
		for (String r : rules) {
			if ((r.equals("general-ign-dict")) ||
				(r.equals("general-camel-base")) ||
				(r.equals("person")) ||
				(r.equals("city")) ||
				(r.equals("road")) ||
				(r.equals("road-prefix")))
				this.rules.add(r);
			else
				throw new ParameterException("HeuristicChunker: unknown heuristic " + r);
		}
	}
	
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		if (ruleActive("general-ign-dict")) chunking.union(ruleGeneralIgnDict(sentence));
		if (ruleActive("general-camel-base")) chunking.union(ruleGeneralCamelBase(sentence));
		if (ruleActive("road-prefix")) chunking.union(ruleRoadPrefix(sentence));

		if (ruleActive("person")){
			chunking.union(rulePersonPanFirstLastNoun(sentence));
			chunking.union(rulePersonPanInitialLast(sentence));
			chunking.union(rulePersonFirstLastMaiden(sentence));
			chunking.union(rulePersonNounFirstLast(sentence));
			chunking.union(rulePersonNounFirstInitialLast(sentence));
		}

		if (ruleActive("city")){
			chunking.union(ruleCityPrefix(sentence));
			chunking.union(ruleCityPostal(sentence));
		}
		
		if (ruleActive("road")){
			chunking.union(ruleRoadPrefixNumber(sentence));
		}
		
		return chunking;
	}	
	
	/**
	 * Checks, whether the given heuristic is in use.
	 * this.rules = null means, that all are used.
	 */	
	private boolean ruleActive(String rule) {
		if (this.rules == null)
			return true;
		else if (this.rules.indexOf(rule) > -1)
			return true;
		else
			return false;
	}
	
	/**
	 * HEURISTICS FUNCTIONS
	 */
	 
	private Chunking ruleGeneralIgnDict(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex attributeIndex = sentence.getAttributeIndex();

		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (!attributeIndex.getAttributeValue(tokens.get(i), "class").equals("ign"))
				continue;
			
			int feature = -1;
			for (int j = 0; j < attributeIndex.getLength(); j++) {
				if (!attributeIndex.getName(j).endsWith("_nam"))
					continue;
				
				// sprawdź, czy jest "B" dla jednego typu i "O" dla wszystkich innych
				if (token.getAttributeValue(j).equals("B")) {
					if (feature == -1)
						feature = j;
					else {
						feature = -1;
						break;
					}
				}
				else if (token.getAttributeValue(j).equals("I")) {
					feature = -1;
					break;
				}
			}
			
			// utwórz anotację
			if (feature > -1) {
				int k = i;
				while ((k+1 < tokens.size()) && 
					(tokens.get(k+1).getAttributeValue(feature).equals("I")))
					k++;
				chunking.addChunk(new Chunk(i, k, 
					attributeIndex.getName(feature).toUpperCase(), sentence));
			}

		}
		
		return chunking;
	}
	
	private Chunking ruleGeneralCamelBase(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex attributeIndex = sentence.getAttributeIndex();

		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (!attributeIndex.getAttributeValue(token, "base").matches("\\p{Lu}\\p{Ll}*"))
				continue;
			
			int feature = -1;
			for (int j = 0; j < attributeIndex.getLength(); j++) {
				if (!attributeIndex.getName(j).endsWith("_nam"))
					continue;
				
				// sprawdź, czy jest "B" dla jednego typu i "O" dla wszystkich innych
				if (token.getAttributeValue(j).equals("B")) {
					if (feature == -1)
						feature = j;
					else {
						feature = -1;
						break;
					}
				}
				else if (token.getAttributeValue(j).equals("I")) {
					feature = -1;
					break;
				}
			}
			
			// utwórz anotację
			if (feature > -1) {
				int k = i;
				while ((k+1 < tokens.size()) && 
					(tokens.get(k+1).getAttributeValue(feature).equals("I")))
					k++;
				chunking.addChunk(new Chunk(i, k, 
					attributeIndex.getName(feature).toUpperCase(), sentence));
			}

		}
		
		return chunking;
	}
	
	private Chunking ruleRoadPrefix(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex attributeIndex = sentence.getAttributeIndex();
		
		if (attributeIndex.getIndex("road_prefix")<0 || attributeIndex.getIndex("road_nam")<0)
			return chunking;
		
		for (int i = 1; i < tokens.size(); i++) {
			if (!attributeIndex.getAttributeValue(tokens.get(i-1), "road_prefix").equals("B")) {
				if (!attributeIndex.getAttributeValue(tokens.get(i-1), "class").equals("interp"))
					continue;
				else if (i == 1)
					continue;
				else if (!attributeIndex.getAttributeValue(tokens.get(i-2), "road_prefix").equals("B"))
					continue;
			}
			if (!attributeIndex.getAttributeValue(tokens.get(i), "road_nam").equals("B"))
				continue;
			int k = i;
			while ((k+1 < tokens.size()) &&
				(attributeIndex.getAttributeValue(tokens.get(k+1), "road_nam").equals("I")))
				k++;
			chunking.addChunk(new Chunk(i, k, "ROAD_NAM", sentence));
		}
		
		return chunking;
	}
	
	/**
	 * Pan | person_first_nam | person_last_nam | person_noun
	 * @param sentence
	 * @return
	 */
	private Chunking rulePersonPanFirstLastNoun(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexBase = ai.getIndex("base");

		if ( indexPersonLastNam < 0 || indexPersonFirstNam < 0 || indexPersonNoun < 0
				|| indexBase < 0 ){
			return chunking;
		}

		for (int i = 0; i + 2 < tokens.size(); i++ ){
			if ( ( tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pan")
					|| tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pani") 
					)
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("B")
					){
				if ( i + 3 == tokens.size()
						|| tokens.get(i+3).getAttributeValue(indexPersonNoun).equals("B") ){
					chunking.addChunk(new Chunk(i+1, i+1, "PERSON_FIRST_NAM", sentence));
					chunking.addChunk(new Chunk(i+2, i+2, "PERSON_LAST_NAM", sentence));
					i += 2;
				}
			}
				
		}
		
		return chunking;
	}
	
	/**
	 * Pan | X | . | X | . | person_last_nam
	 * @param sentence
	 * @return
	 */
	private Chunking rulePersonPanInitialLast(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPattern = ai.getIndex("pattern");
		int indexBase = ai.getIndex("base");
		int indexOrth = ai.getIndex("orth");

		if ( indexPersonLastNam < 0 || indexPattern < 0 || indexBase < 0
				|| indexOrth < 0 ){
			return chunking;
		}

		for (int i = 0; i + 5 < tokens.size(); i++ ){
			if ( ( tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pan")
					|| tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pani") 
					)
					&& tokens.get(i+1).getAttributeValue(indexPattern).equals("ALL_UPPER")
					&& tokens.get(i+1).getAttributeValue(indexOrth).length() == 1
					&& tokens.get(i+2).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+3).getAttributeValue(indexPattern).equals("ALL_UPPER")
					&& tokens.get(i+3).getAttributeValue(indexOrth).length() == 1
					&& tokens.get(i+4).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+5).getAttributeValue(indexPersonLastNam).equals("B")					
					){
					chunking.addChunk(new Chunk(i+5, i+5, "PERSON_LAST_NAM", sentence));
					i += 5;
				}				
		}
		
		return chunking;
	}
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private Chunking rulePersonFirstLast(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");

		if ( indexPersonLastNam < 0 || indexPersonFirstNam < 0 ){
			return chunking;
		}

		for (int i = 0; i + 1 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i).getAttributeValue(indexPersonLastNam).equals("O")
					&& tokens.get(i+1).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("O")
					&&  ( i + 2 == tokens.size() || tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("O") )
					){
						chunking.addChunk(new Chunk(i, i, "PERSON_FIRST_NAM", sentence));
						chunking.addChunk(new Chunk(i+1, i+1, "PERSON_LAST_NAM", sentence));						
					}
		}

		return chunking;
	}

	/**
	 * 
	 * person_first_nam | person_last_nam | - | person_last_nam
	 * @param sentence
	 * @return
	 */
	private Chunking rulePersonFirstLastMaiden(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexStartsWithLowerCase = ai.getIndex("starts_with_lower_case");
		int indexOrth = ai.getIndex("orth");
		
		if ( indexPersonLastNam < 0 || indexPersonFirstNam < 0 || indexPersonNoun < 0
				|| indexStartsWithLowerCase < 0 || indexOrth < 0){
			return chunking;
		}
		
		for (int i = 0; i + 4 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexOrth).equals("-")
					&& tokens.get(i+3).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+3).getAttributeValue(indexPersonNoun).equals("O")
					&& ( i + 5 == tokens.size() || tokens.get(i+5).getAttributeValue(indexStartsWithLowerCase).equals("1") )
					){
						chunking.addChunk(new Chunk(i, i, "PERSON_FIRST_NAM", sentence));
						chunking.addChunk(new Chunk(i+1, i+1, "PERSON_LAST_NAM", sentence));						
						chunking.addChunk(new Chunk(i+3, i+3, "PERSON_LAST_NAM", sentence));						
					}
		}

		return chunking;
	}
	
	/**
	 * dyrektor Jan Nowak
	 */
	private Chunking rulePersonNounFirstLast(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexStartsWithUpperCase = ai.getIndex("starts_with_upper_case");
		int indexPersonNoun = ai.getIndex("person_noun");

		if ( indexPersonLastNam < 0 || indexPersonFirstNam < 0 || indexPersonNoun < 0
				|| indexPersonNoun < 0 ){
			return chunking;
		}

		for (int i = 0; i + 2 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonNoun).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonFirstNam).equals("O")
					&& ( i+3 == tokens.size() || tokens.get(i+3).getAttributeValue(indexStartsWithUpperCase).equals("0"))
					){
				chunking.addChunk(new Chunk(i+1, i+1, "PERSON_FIRST_NAM", sentence));
				chunking.addChunk(new Chunk(i+2, i+2, "PERSON_LAST_NAM", sentence));										
			}
		}
		
		return chunking;
	}	
	
	/**
	 * dyrektor Jan K. Nowak
	 */
	private Chunking rulePersonNounFirstInitialLast(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexPattern = ai.getIndex("pattern");
		int indexOrth = ai.getIndex("orth");

		if ( indexPersonLastNam < 0 || indexPersonFirstNam < 0 || indexPersonNoun < 0
				|| indexPattern < 0 || indexOrth < 0){
			return chunking;
		}

		for (int i = 0; i + 4 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonNoun).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPattern).equals("ALL_UPPER")
					&& tokens.get(i+2).getAttributeValue(indexOrth).length() == 1
					&& tokens.get(i+3).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+4).getAttributeValue(indexPersonLastNam).equals("B")
					){
				chunking.addChunk(new Chunk(i+1, i+1, "PERSON_FIRST_NAM", sentence));
				chunking.addChunk(new Chunk(i+4, i+4, "PERSON_LAST_NAM", sentence));										
			}
		}
		
		return chunking;
	}
	
	private Chunking ruleCityPrefix(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexBase = ai.getIndex("base");
		int indexCityNam = ai.getIndex("city_nam");
		int indexCase = ai.getIndex("case");
	
		if ( indexBase < 0 || indexCityNam < 0 || indexCase < 0){
			return chunking;
		}

		for (int i = 0; i + 1 < tokens.size(); i++ ){
			if ( ( tokens.get(i).getAttributeValue(indexBase).equals("gmina")
					|| tokens.get(i).getAttributeValue(indexBase).equals("gmin") 
					|| tokens.get(i).getAttributeValue(indexBase).equals("miasto") )
					&& tokens.get(i+1).getAttributeValue(indexCityNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexCase).equals("nom")
					&& !tokens.get(i+1).getAttributeValue(indexBase).equals("miasto")) {
				int n = i + 1;
				while (n + 1 < tokens.size() && tokens.get(n+1).getAttributeValue(indexCityNam).equals("I")) n++;
				chunking.addChunk(new Chunk(i+1, n, "CITY_NAM", sentence));
			}
		}
		
		return chunking;
	}
	
	/**
	 * Sample:
	 *   66-400 Gorzów Wlkp.
	 * @param sentence
	 * @return
	 */
	private Chunking ruleCityPostal(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexOrth = ai.getIndex("orth");
		int indexCityNam = ai.getIndex("city_nam");
		int indexPattern = ai.getIndex("pattern");

		if ( indexOrth < 0 || indexCityNam < 0 || indexPattern < 0){
			return chunking;
		}
		
		for (int i = 0; i + 3 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPattern).equals("DIGITS")
					&& tokens.get(i).getAttributeValue(indexOrth).length() == 2
					&& tokens.get(i+1).getAttributeValue(indexOrth).equals("-")
					&& tokens.get(i+2).getAttributeValue(indexPattern).equals("DIGITS")
					&& tokens.get(i+2).getAttributeValue(indexOrth).length() == 3
					&& tokens.get(i+3).getAttributeValue(indexCityNam).equals("B")
					) {
				int n = i + 3;
				while (n + 1 < tokens.size() && tokens.get(n+1).getAttributeValue(indexCityNam).equals("I")) n++;
				chunking.addChunk(new Chunk(i+3, n, "CITY_NAM", sentence));
				i = n;
			}
		}
		
		return chunking;
	}
	
	private Chunking ruleRoadPrefixNumber(Sentence sentence){
		Chunking chunking = new Chunking(sentence);
		ArrayList<Token> tokens = sentence.getTokens();
		AttributeIndex ai = sentence.getAttributeIndex();
		
		int indexOrth = ai.getIndex("orth");
		int indexPattern = ai.getIndex("pattern");

		if ( indexOrth < 0 || indexPattern < 0){
			return chunking;
		}
		
		for ( int i=0; i + 3 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexOrth).toLowerCase().equals("ul")
					&& tokens.get(i+1).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+2).getAttributeValue(indexPattern).equals("UPPER_INIT")
					&& tokens.get(i+3).getAttributeValue(indexPattern).equals("DIGITS")){
				chunking.addChunk(new Chunk(i+2, i+2, "ROAD_NAM", sentence));
				
				if ( i + 6 < tokens.size() 
						&& tokens.get(i+4).getAttributeValue(indexOrth).equals("/")
						&& tokens.get(i+5).getAttributeValue(indexPattern).equals("UPPER_INIT")
						&& tokens.get(i+6).getAttributeValue(indexPattern).equals("DIGITS")){
					chunking.addChunk(new Chunk(i+5, i+5, "ROAD_NAM", sentence));					
				}
			}
		}
		return chunking;
	}
}
