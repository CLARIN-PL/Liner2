package g419.liner2.api.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.util.HashMap;


public abstract class Chunker {

    String description;
	abstract public HashMap<Sentence, AnnotationSet> chunk(Document ps);

	public void chunkInPlace(Document ps){
		HashMap<Sentence, AnnotationSet> chunking = this.chunk(ps);
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				if ( chunking.containsKey(sentence) )
					sentence.addAnnotations(chunking.get(sentence));
	}


    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
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
	public void prepare(Document ps) {
	}

}
