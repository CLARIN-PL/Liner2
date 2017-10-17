package g419.liner2.core.chunker;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

/**
 * Chunker, który usuwa zagnieżdżone anotacje
 * @author Michał Marcińczuk
 */

public class RemoveNestedChunker extends Chunker {

	List<Pattern> anTypes = null; 
	
	public RemoveNestedChunker(List<Pattern> types) {
		this.anTypes = types;
	}
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Sentence sentence : ps.getSentences() ){
			chunkings.put(sentence, this.chunkSentence(sentence));
		}
		return chunkings;
	}


	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet chunkSentence(Sentence sentence) {
		this.removeNestedAnnotations(sentence);
		return new AnnotationSet(sentence);
	}
	
	/**
	 * Usuwa anotacje zagnieżdżone wewnątrz innych anotacji.
	 * @param sentence
	 */
	private void removeNestedAnnotations(Sentence sentence){
		Map<Integer, Set<Annotation>> tokenToAn = new HashMap<Integer, Set<Annotation>>();
		
		// Zaindeksuj anotacje
		for ( Annotation an : sentence.getAnnotations(this.anTypes) ){
			for ( int i=an.getBegin(); i<=an.getEnd(); i++){
				Set<Annotation> ans = tokenToAn.get(i);
				if ( ans == null ){
					ans = new HashSet<Annotation>();
					tokenToAn.put(i, ans);
				}
				ans.add(an);
			}
		}
		
		// Wykryj zagnieżdżenia
		for ( Set<Annotation> ans : tokenToAn.values() ){
			if ( ans.size() > 1 ){
				for ( Annotation anShorter : ans ){
					for ( Annotation anLonger : ans ){
						if ( this.isInside(anLonger, anShorter) ){
							sentence.getChunks().remove(anShorter);
						}
					}
				}
			}
		}		
	}
		
	/**
	 * Sprawdza, czy anotacja inside zawiera się w anotacji outside. Anotacje się zawierają w sobie, 
	 * jeżeli jedna zawiera podzbiór tokenów drugiej.
	 * @param outsie
	 * @param inside
	 * @return
	 */
	public boolean isInside(Annotation outsie, Annotation inside){
		return outsie.getTokens().size() != inside.getTokens().size() && outsie.getTokens().containsAll(inside.getTokens());
	}
}	
