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
	
	private ArrayList<String> rules = null;
	
	public HeuristicChunker() {}
	
	public HeuristicChunker(String[] rules) throws ParameterException {
		this.rules = new ArrayList<String>();
		for (String r : rules) {
			if ((r.equals("general-ign-dict")) ||
				(r.equals("general-camel-base")) ||
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
}
