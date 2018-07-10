package g419.liner2.core.chunker;

import g419.corpus.structure.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;



/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */

public class HeuristicChunker extends Chunker {
	
	private Pattern romanNumer = Pattern.compile("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
	private ArrayList<String> rules = null;
	
	public HeuristicChunker() {}
	
	public HeuristicChunker(String[] rules) throws Exception {
		this.rules = new ArrayList<String>();
		for (String r : rules) {
			if ((r.equals("general-ign-dict")) ||
				(r.equals("general-camel-base")) ||
				(r.equals("person")) ||
				(r.equals("city")) ||
				(r.equals("road")) ||
				(r.equals("road-prefix")) ||
				(r.equals("nam"))
				)
				this.rules.add(r);
			else
				throw new Exception("HeuristicChunker: unknown heuristic " + r);
		}
	}
	
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		if (ruleActive("general-ign-dict")) 
			chunking.union(ruleGeneralIgnDict(sentence));
		if (ruleActive("general-camel-base")) 
			chunking.union(ruleGeneralCamelBase(sentence));
		if (ruleActive("road-prefix")) 
			chunking.union(ruleRoadPrefix(sentence));

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
		
		if (ruleActive("nam")){
			chunking.union(ruleNamUpperCamelCase(sentence));
			chunking.union(ruleNamParanthesis(sentence));
			chunking.union(ruleNamAllUpper(sentence));
		}
		
		return chunking;
	}	
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet ruleNamUpperCamelCase(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
		
		for (int i = 0; i < tokens.size(); i++) {
			if (attributeIndex.getAttributeValue(tokens.get(i), "pattern").equals("UPPER_CAMEL_CASE")) {
				chunking.addChunk(new Annotation(i, "NAM", sentence));
			}			
		}
		
		return chunking;	
	}

	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet ruleNamParanthesis(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
		
		for (int i = 1; i < tokens.size(); i++) {
			String orth = attributeIndex.getAttributeValue(tokens.get(i), "orth"); 
			if ( i+2 < tokens.size() 
					&& ( orth.equals("„") || orth.equals("“") || orth.equals("\"")
							|| orth.equals("&quot;") ) 
					&& attributeIndex.getAttributeValue(tokens.get(i+1), "starts_with_upper_case").equals("1") ){
				
				int j = i+1;
				boolean ends = false;
				while ( j < tokens.size() && !ends){
					String orth2 = attributeIndex.getAttributeValue(tokens.get(j), "orth");
					ends = orth2.equals("”") || orth2.equals("\"") || orth.equals("&quot;");
					j++;
				}
				if (ends && i+1 <= j-2 && j-i < 5 ){
					/* i+1 do j-2 to tekst w cudzysłowiu */ 
					chunking.addChunk(new Annotation(i+1, j-2, "NAM", sentence));
					i = j-1;
				}				
			}			
		}
		
		return chunking;	
	}
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet ruleNamAllUpper(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		int iHasLoweCase = ai.getIndex("has_lower_case");
		int iPattern = ai.getIndex("pattern");
		int iOrth = ai.getIndex("orth");
		
		boolean hasLowerCase = false;
		int k = 0;
		while ( k < tokens.size() && hasLowerCase == false ){
			hasLowerCase = tokens.get(k).getAttributeValue(iHasLoweCase).equals("1");
			k++;
		}
		
		if ( !hasLowerCase ){
			return chunking;
		}
		
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).getAttributeValue(iPattern).equals("ALL_UPPER")
					&& tokens.get(i).getAttributeValue(iOrth).length() > 2 ) {
				int j = i;
				while ( j < tokens.size() 
						&& tokens.get(j).getAttributeValue(iPattern).equals("ALL_UPPER")
						&& tokens.get(j).getAttributeValue(iOrth).length() > 2 ){
					j++;
				}
				/**
				 * Sprawdź, czy kolejne słowo to nie "wiek", aby odrzucić liczby rzymskie
				 */
				if ( j - i > 1 
						|| this.romanNumer.matcher(
								ai.getAttributeValue(tokens.get(i), "orth")).find() == false ){
					chunking.addChunk(new Annotation(i, j-1, "NAM", sentence));
				}
			}			
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
	 
	private AnnotationSet ruleGeneralIgnDict(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();

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
				chunking.addChunk(new Annotation(i, k, 
					attributeIndex.getName(feature).toUpperCase(), sentence));
			}

		}
		
		return chunking;
	}
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet ruleGeneralCamelBase(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();

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
				chunking.addChunk(new Annotation(i, k, 
					attributeIndex.getName(feature).toUpperCase(), sentence));
			}

		}
		
		return chunking;
	}
	
	private AnnotationSet ruleRoadPrefix(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex attributeIndex = sentence.getAttributeIndex();
		
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
			chunking.addChunk(new Annotation(i, k, "ROAD_NAM", sentence));
		}
		
		return chunking;
	}
	
	/**
	 * Pan | person_first_nam | person_last_nam | person_noun
	 * @param sentence
	 * @return
	 */
	private AnnotationSet rulePersonPanFirstLastNoun(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexBase = ai.getIndex("base");
		
		for (int i = 0; i + 2 < tokens.size(); i++ ){
			if ( ( tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pan")
					|| tokens.get(i).getAttributeValue(indexBase).toLowerCase().equals("pani") 
					)
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("B")
					){
				if ( i + 3 == tokens.size()
						|| tokens.get(i+3).getAttributeValue(indexPersonNoun).equals("B") ){
					chunking.addChunk(new Annotation(i+1, "PERSON_FIRST_NAM", sentence));
					chunking.addChunk(new Annotation(i+2, "PERSON_LAST_NAM", sentence));
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
	private AnnotationSet rulePersonPanInitialLast(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPattern = ai.getIndex("pattern");
		int indexBase = ai.getIndex("base");
		int indexOrth = ai.getIndex("orth");
		
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
					chunking.addChunk(new Annotation(i+5, "PERSON_LAST_NAM", sentence));
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
	private AnnotationSet rulePersonFirstLast(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		
		for (int i = 0; i + 1 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i).getAttributeValue(indexPersonLastNam).equals("O")
					&& tokens.get(i+1).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("O")
					&&  ( i + 2 == tokens.size() || tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("O") )
					){
						chunking.addChunk(new Annotation(i, "PERSON_FIRST_NAM", sentence));
						chunking.addChunk(new Annotation(i+1, "PERSON_LAST_NAM", sentence));						
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
	private AnnotationSet rulePersonFirstLastMaiden(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexStartsWithLowerCase = ai.getIndex("starts_with_lower_case");
		int indexOrth = ai.getIndex("orth");
		
		for (int i = 0; i + 4 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexOrth).equals("-")
					&& tokens.get(i+3).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+3).getAttributeValue(indexPersonNoun).equals("O")
					&& ( i + 5 == tokens.size() || tokens.get(i+5).getAttributeValue(indexStartsWithLowerCase).equals("1") )
					){
						chunking.addChunk(new Annotation(i, "PERSON_FIRST_NAM", sentence));
						chunking.addChunk(new Annotation(i+1, "PERSON_LAST_NAM", sentence));						
						chunking.addChunk(new Annotation(i+3, "PERSON_LAST_NAM", sentence));						
					}
		}

		return chunking;
	}
	
	/**
	 * dyrektor Jan Nowak
	 */
	private AnnotationSet rulePersonNounFirstLast(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexStartsWithUpperCase = ai.getIndex("starts_with_upper_case");
		int indexPersonNoun = ai.getIndex("person_noun");
		
		for (int i = 0; i + 2 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonNoun).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonLastNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPersonFirstNam).equals("O")
					&& ( i+3 == tokens.size() || tokens.get(i+3).getAttributeValue(indexStartsWithUpperCase).equals("0"))
					){
				chunking.addChunk(new Annotation(i+1, "PERSON_FIRST_NAM", sentence));
				chunking.addChunk(new Annotation(i+2, "PERSON_LAST_NAM", sentence));										
			}
		}
		
		return chunking;
	}	
	
	/**
	 * dyrektor Jan K. Nowak
	 */
	private AnnotationSet rulePersonNounFirstInitialLast(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexPersonLastNam = ai.getIndex("person_last_nam");
		int indexPersonFirstNam = ai.getIndex("person_first_nam");
		int indexPersonNoun = ai.getIndex("person_noun");
		int indexPattern = ai.getIndex("pattern");
		int indexOrth = ai.getIndex("orth");
		
		for (int i = 0; i + 4 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexPersonNoun).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexPersonFirstNam).equals("B")
					&& tokens.get(i+2).getAttributeValue(indexPattern).equals("ALL_UPPER")
					&& tokens.get(i+2).getAttributeValue(indexOrth).length() == 1
					&& tokens.get(i+3).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+4).getAttributeValue(indexPersonLastNam).equals("B")
					){
				chunking.addChunk(new Annotation(i+1, "PERSON_FIRST_NAM", sentence));
				chunking.addChunk(new Annotation(i+4, "PERSON_LAST_NAM", sentence));										
			}
		}
		
		return chunking;
	}
	
	private AnnotationSet ruleCityPrefix(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexBase = ai.getIndex("base");
		int indexCityNam = ai.getIndex("city_nam");
		int indexCase = ai.getIndex("case");
	
		for (int i = 0; i + 1 < tokens.size(); i++ ){
			if ( ( tokens.get(i).getAttributeValue(indexBase).equals("gmina")
					|| tokens.get(i).getAttributeValue(indexBase).equals("gmin") 
					|| tokens.get(i).getAttributeValue(indexBase).equals("miasto") )
					&& tokens.get(i+1).getAttributeValue(indexCityNam).equals("B")
					&& tokens.get(i+1).getAttributeValue(indexCase).equals("nom")
					&& !tokens.get(i+1).getAttributeValue(indexBase).equals("miasto")) {
				int n = i + 1;
				while (n + 1 < tokens.size() && tokens.get(n+1).getAttributeValue(indexCityNam).equals("I")) n++;
				chunking.addChunk(new Annotation(i+1, n, "CITY_NAM", sentence));
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
	private AnnotationSet ruleCityPostal(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexOrth = ai.getIndex("orth");
		int indexCityNam = ai.getIndex("city_nam");
		int indexPattern = ai.getIndex("pattern");
	
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
				chunking.addChunk(new Annotation(i+3, n, "CITY_NAM", sentence));
				i = n;
			}
		}
		
		return chunking;
	}
	
	private AnnotationSet ruleRoadPrefixNumber(Sentence sentence){
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		int indexOrth = ai.getIndex("orth");
		int indexPattern = ai.getIndex("pattern");
		
		for ( int i=0; i + 3 < tokens.size(); i++ ){
			if ( tokens.get(i).getAttributeValue(indexOrth).toLowerCase().equals("ul")
					&& tokens.get(i+1).getAttributeValue(indexOrth).equals(".")
					&& tokens.get(i+2).getAttributeValue(indexPattern).equals("UPPER_INIT")
					&& tokens.get(i+3).getAttributeValue(indexPattern).equals("DIGITS")){
				chunking.addChunk(new Annotation(i+2, "ROAD_NAM", sentence));
				
				if ( i + 6 < tokens.size() 
						&& tokens.get(i+4).getAttributeValue(indexOrth).equals("/")
						&& tokens.get(i+5).getAttributeValue(indexPattern).equals("UPPER_INIT")
						&& tokens.get(i+6).getAttributeValue(indexPattern).equals("DIGITS")){
					chunking.addChunk(new Annotation(i+5, "ROAD_NAM", sentence));					
				}
			}
		}
		return chunking;
	}

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
}
