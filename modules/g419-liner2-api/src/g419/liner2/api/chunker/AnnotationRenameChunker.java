package g419.liner2.api.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashMap;
import java.util.Map;


public class AnnotationRenameChunker extends Chunker {
	
	private Chunker chunker = null;
	private Map<String, String> rename = new HashMap<String, String>();

    public AnnotationRenameChunker(Chunker baseChunker) {
		this.chunker = baseChunker;
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		/* Get base chunking for every sentence. */
		HashMap<Sentence, AnnotationSet> chunkings = this.chunker.chunk(ps);

		for (Sentence sentence : chunkings.keySet()){
			
		}
		
		return chunkings;
	}
	
}
