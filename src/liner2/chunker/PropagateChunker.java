package liner2.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.Document;
import liner2.structure.Sentence;

/**
 * @author Michał Marcińczuk
 * 
 */

public class PropagateChunker extends Chunker {
	private Chunker baseChunker = null;
	private boolean one = false;
	private HashMap<String, String> dictionary = null;
	private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
	

	public PropagateChunker(Chunker baseChunker) {
		this.dictionary = new HashMap<String, String>();
		this.baseChunker = baseChunker;
	
		this.patterns.add(Pattern.compile("^\\p{Lu}{3}\\p{Lu}*( \\p{Lu}+)*$"));
		this.patterns.add(Pattern.compile("^\\p{Lu}{3}\\p{Lu}*(-u)$"));
		this.patterns.add(Pattern.compile("^\\p{Lu}\\p{Ll}*( \\p{Lu}\\p{Ll}*)+$"));
		this.patterns.add(Pattern.compile("^\\p{Lu}+[0-9]+$"));
		this.patterns.add(Pattern.compile("^\\p{Lu}\\p{Lu}+[0-9]+$"));
	}

	private boolean isAcceptable(String value){
		for (Pattern p : this.patterns){
			Matcher m = p.matcher(value);
			if (m.find())
				return true;
		}
		return false;
	}
	
	@Override
	public void prepare(Document ps) {
		
	}
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps){
		HashMap<Sentence, AnnotationSet> chunkings = this.baseChunker.chunk(ps);

		DictionaryChunker dictionaryChunker = new DictionaryChunker(null);

		for (AnnotationSet chunking : chunkings.values())
			for (Annotation chunk : chunking.chunkSet()){
				String value = chunk.getText();
				if (this.isAcceptable(value)){
					dictionaryChunker.addEntry(chunk.getType(), value);
				}
			}
		
		HashMap<Sentence, AnnotationSet> chukingsProp = dictionaryChunker.chunk(ps);
		
		for (Sentence sentence : chukingsProp.keySet()){
			chunkings.get(sentence).union(chukingsProp.get(sentence));
		}
		
		return chunkings;
	}

}
