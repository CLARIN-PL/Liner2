package g419.liner2.api.chunker.ensemble;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.normalizer.Normalizer;
import g419.liner2.api.normalizer.NormalizingChunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO
 *  
 * @author Michał Marcińczuk
 *
 */
public class CascadeChunker extends Chunker implements Normalizer {

	private ArrayList<Chunker> chunkers;

	public CascadeChunker(ArrayList<Chunker> chunkers){
		this.chunkers = chunkers;
	}
	
    public void onNewDocument(Document document){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewDocument(document);

    }
    public void onDocumentEnd(Document document){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onDocumentEnd(document);
    }

    public void onNewSentence(Sentence sentence){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewSentence(sentence);
    }
    public void onSentenceEnd(Sentence sentence){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onSentenceEnd(sentence);
    }

    public void onNewAnnotation(Annotation annotation){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onNewAnnotation(annotation);
    }
    public void onAnnotationEnd(Annotation annotation){
        for (Chunker c: chunkers)
            if (c instanceof NormalizingChunker)
                ((NormalizingChunker) c).onAnnotationEnd(annotation);
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
				if ( !sentence.getChunks().contains(an) ){
                    an.setSentence(sentence);
                    set.addChunk(an);
                }
			chunkings.put(sentence, set);
		}
		
		// Dodaj relacje - dla np. ChunkRel'a
		ps.setRelations(document.getRelations());
		
		return chunkings;
	}	

    @Override
    public List<Pattern> getNormalizedChunkTypes() {
        List<Pattern> out = new ArrayList<>();
        for (Chunker c: chunkers)
            if (c instanceof Normalizer)
                out.addAll(((Normalizer) c).getNormalizedChunkTypes());
        return out;
    }

    @Override
    public void normalize(Annotation annotation) {
        for (Chunker c: chunkers)
            if (c instanceof Normalizer)
                ((Normalizer) c).normalize(annotation);
    }	
}
