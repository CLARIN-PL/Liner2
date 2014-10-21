package g419.liner2.api.chunker.ensemble;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * TODO
 *  
 * @author Michał Marcińczuk
 *
 */
public class CascadeChunker extends Chunker {

	private ArrayList<Chunker> chunkers;

	public CascadeChunker(ArrayList<Chunker> chunkers){
		this.chunkers = chunkers;
	}
		
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		
		Document document = ps.clone();
				
		for ( Chunker chunker : this.chunkers){
			chunker.chunkInPlace(document);
		}
		
		for ( int i=0; i<document.getSentences().size(); i++ ){
			Sentence sentence = ps.getSentences().get(i);
			AnnotationSet set = new AnnotationSet(sentence);
			for ( Annotation an : document.getSentences().get(i).getChunks() )
				if ( !sentence.getChunks().contains(an) )
					set.addChunk(an);
			chunkings.put(sentence, set);
		}
		
		return chunkings;
	}	
	
}
