package liner2.chunker;

import java.util.HashMap;
import java.util.HashSet;

import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

public abstract class Chunker {

	/**
	 * TODO
	 * Przetwarza podane zdanie, rozpoznaje chunki i zwraca w postaci tablicy chunków.
	 * @param sentence
	 * @return
	 */
	public abstract Chunking chunkSentence(Sentence sentence);

	private void chunkInPlace(Sentence sentence){
		Chunking chunking = this.chunkSentence(sentence);
		sentence.addChunking(chunking);
	}

	public HashMap<Sentence, Chunking> chunk(ParagraphSet ps){
		HashMap<Sentence, Chunking> chunkings = new HashMap<Sentence, Chunking>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences()){
				chunkings.put(sentence, this.chunkSentence(sentence));
			}
		return chunkings;
	}

	public void chunkInPlace(ParagraphSet ps){
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				this.chunkInPlace(sentence);
	}

	/**
	 * Zwolnienie zasobów wykorzystywanych przez chunker, 
	 * np. zamknięcie zewnętrznych procesów i połączeń.
	 * Jeżeli jest to wymagane, to klasa dziedzicząca powinna przeciążyć
	 * tą metodę. 
	 */
	public void close(){
		
	}
	
	/**
	 * Przygotowanie do klasyfikacji danego tekstu. Tą metodę przeciążają klasyfikatory,
	 * które wymagają podania całego tekstu przed rozpoczęciem pracy, np. dwuprzebiegowe.
	 */
	public void prepare(ParagraphSet ps) {
	}

}
