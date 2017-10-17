package g419.liner2.core.chunker;

import java.util.Map;

import org.ini4j.Ini;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;


public abstract class Chunker {

    Ini.Section description;
    
    /**
     * Recognize annotations in the document. 
     * The recognized annotations should not be added to the sentences but returned in the map.
     * @param ps
     * @return
     */
	abstract public Map<Sentence, AnnotationSet> chunk(Document ps);

	public void chunkInPlace(Document ps){
		Map<Sentence, AnnotationSet> chunking = this.chunk(ps);
		for ( Paragraph paragraph : ps.getParagraphs() ){
			for (Sentence sentence : paragraph.getSentences()){
				if ( chunking.containsKey(sentence) ){
					sentence.addAnnotations(chunking.get(sentence));
				}
			}
		}
	}


    public void setDescription(Ini.Section description){
        this.description = description;
    }

    public org.ini4j.Profile.Section getDescription(){
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
