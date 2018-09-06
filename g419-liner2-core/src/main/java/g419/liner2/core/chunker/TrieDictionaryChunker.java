package g419.liner2.core.chunker;

import g419.corpus.structure.*;
import g419.liner2.core.tools.TrieDictNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * Chunker rozpoznaje anotacje znajdujące się w słowniku typu <code>TrieDictNode</code>. 
 *
 * @author Michał Marcińczuk
 */
public class TrieDictionaryChunker extends Chunker {
	
	/** The dict roads. */
	private TrieDictNode dictRoads = null;
	
	/** The annotation name. */
	private String annotationName = null;
	
	/** The word source. */
	private String wordSource = null;

	
	/**
	 * Instantiates a new trie dictionary chunker.
	 *
	 * @param node the node
	 * @param annotationName the annotation name
	 * @param wordSource the word source
	 */
	public TrieDictionaryChunker(TrieDictNode node, String annotationName, String wordSource) {
		this.dictRoads = node;
		this.annotationName = annotationName;
		this.wordSource = wordSource;
	}
	
	/**
	 * Chunk sentence.
	 *
	 * @param sentence the sentence
	 * @return the annotation set
	 */
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		int i=0;
		List<Annotation> foundAnnotations = new LinkedList<Annotation>();

		while ( i < sentence.getTokenNumber() ){
			Annotation an = this.matchDictAny(sentence, i);
			if ( an != null ){
				foundAnnotations.add(an);
				i = an.getTokens().last();
			}
			i++;
		}

		for ( Annotation an : foundAnnotations ){
			chunking.addChunk(an);
		}


		return chunking;
	}	
	
	/**
	 * Dopasowuje nazwę ulicy (dictRoads) poprzedzoną prefiksem lub zaczynającą się od prefiksu.
	 *
	 * @param sentence the sentence
	 * @param index the index
	 * @return the annotation
	 */

	/**
	 * Dopasowuje nazwę ulicy bez względu, czy jest poprzedzona prefiksem.
	 * @param sentence
	 * @param index
	 * @return
	 */
	private Annotation matchDictAny(Sentence sentence, int index){
		Annotation an = null;
		int matched = this.match(this.dictRoads, sentence.getTokens(), index);
		if ( matched > 0 ) {
			an = new Annotation(index, index+matched-1, this.annotationName, sentence);
		}
		return an;
	}
	
	/**
	 * Funkcja sprawdza, czy sekwencja tokenów zaczynająca się od indeksu index zandjuje się w słowniku.
	 *
	 * @param dict the dict
	 * @param tokens the tokens
	 * @param index the index
	 * @return the int
	 */
	private int match(TrieDictNode dict, List<Token> tokens, int index){
		TrieDictNode currentNode = dict;
		int longestMatch = 0;
		int offset = 0;
		while ( currentNode != null && index+offset < tokens.size() ){
			String word = tokens.get(index+(offset++)).getElement(this.wordSource);
			TrieDictNode nextNode = currentNode.getChild(word);
			
			if ( nextNode != null && nextNode.isTerminal() ){
				longestMatch = offset;
			}
			currentNode = nextNode;
		}
		return longestMatch;
	}
	
	
	/* (non-Javadoc)
	 * @see g419.liner2.core.chunker.Chunker#chunk(g419.corpus.structure.Document)
	 */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() ) {
			for (Sentence sentence : paragraph.getSentences()) {
				chunkings.put(sentence, this.chunkSentence(sentence));
			}
		}
		return chunkings;
	}
}
