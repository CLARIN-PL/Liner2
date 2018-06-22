package g419.liner2.core.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Michał Marcińczuk
 * 
 */

public class PropagateChunker extends Chunker {
	private Chunker baseChunker = null;
	private boolean one = false;
	private Map<String, String> dictionary = null;
	private List<Pattern> patterns = new ArrayList<Pattern>();
	

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
	public Map<Sentence, AnnotationSet> chunk(Document ps){
		Map<Sentence, AnnotationSet> chunkings = this.baseChunker.chunk(ps);

		DictionaryChunker dictionaryChunker = new DictionaryChunker(null);
        HashSet<String> ambigious = new HashSet<String>();
		for (AnnotationSet chunking : chunkings.values())
			for (Annotation chunk : chunking.chunkSet()){
				String name = chunk.getText();
                String channel = chunk.getType();
				if (this.isAcceptable(name) && !ambigious.contains(name)){
                    if(dictionaryChunker.hasName(name) && !dictionaryChunker.getChannel(name).equals(channel)){
                        dictionaryChunker.removeEntry(name);
                        ambigious.add(name);
                    }
                    else{
                        dictionaryChunker.addEntry(name, channel);
                    }
                }
			}
		
		HashMap<Sentence, AnnotationSet> chukingsProp = dictionaryChunker.chunk(ps);
		
		for (Sentence sentence : chukingsProp.keySet()){
			chunkings.get(sentence).union(chukingsProp.get(sentence));
		}
		
		return chunkings;
	}

}
