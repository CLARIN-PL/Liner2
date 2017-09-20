package g419.liner2.api.chunker;

import java.util.Map;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class AnnotationTop8ClassifierChunker extends Chunker {
		
	private Chunker inputChunker = null;
	
	/**
	 * 
	 * @param inputChunker
	 */
    public AnnotationTop8ClassifierChunker(Chunker inputChunker) {
    	this.inputChunker = inputChunker;
    }


	@Override
	public Map<Sentence, AnnotationSet> chunk(Document ps) {
		Map<Sentence, AnnotationSet> inputChunks = this.inputChunker.chunk(ps);
		return inputChunks;
	}
	
}
