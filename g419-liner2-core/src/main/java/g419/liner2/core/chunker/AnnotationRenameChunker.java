package g419.liner2.core.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashMap;
import java.util.Map;


public class AnnotationRenameChunker extends Chunker {
	
	private Chunker chunker = null;
	private Map<String, String> rename = new HashMap<String, String>();

    public AnnotationRenameChunker(Chunker baseChunker, Map<String, String> rename) {
		this.chunker = baseChunker;
		this.rename = rename;
    }
    
    /**
     * 
     */
	@Override
	public Map<Sentence, AnnotationSet> chunk(Document ps) {
		/* Get base chunking for every sentence. */
		Map<Sentence, AnnotationSet> chunkings = this.chunker.chunk(ps);

		for (Sentence sentence : chunkings.keySet()){
			for (Annotation ann : chunkings.get(sentence).chunkSet()){
				if ( this.rename.containsKey(ann.getType()) ){
					ann.setType(this.rename.get(ann.getType()));
				}
			}
		}
		
		return chunkings;
	}

}
