package liner2.chunker;

import java.util.HashMap;

import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

public abstract class Chunker {

	abstract public HashMap<Sentence, Chunking> chunk(ParagraphSet ps);

	public void chunkInPlace(ParagraphSet ps){
		HashMap<Sentence, Chunking> chunking = this.chunk(ps);
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				if ( chunking.containsKey(sentence) )
					sentence.addChunking(chunking.get(sentence));
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
