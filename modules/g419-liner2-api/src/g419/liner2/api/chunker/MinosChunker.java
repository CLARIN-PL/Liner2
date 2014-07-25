package g419.liner2.api.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.HashMap;



/**
 * Null verb mention detector.
 * 
 * @author Adam Kaczmarek
 */

public class MinosChunker extends Chunker {
	
	public final static String Annotation = "wyznacznik_null_verb";
	
	public MinosChunker() {}
		
	/**
	 * @param sentence
	 * @return
	 */
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		
		ArrayList<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		/* Get part of speech feature index */
		int indexPos = ai.getIndex("class");

		/* Przykład nanoszenia anotacji.
		 * Każde wystąpienie słowa o klasie "fin" zostaje oznaczone jako wyznacznik_null_verb.
		 */
		for (int i=0; i<tokens.size(); i++ ){
			Token t = tokens.get(i);
			String pos = t.getAttributeValue(indexPos);
			System.out.println(pos);
			if ( pos != null && pos.equals("fin"))
				chunking.addChunk(new Annotation(i, MinosChunker.Annotation, sentence));
		}
		
		return chunking;
	}	
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
}
