package liner2.reader;

import liner2.structure.Sentence;

/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class StreamReader {

	public abstract Sentence readSentence();
	
	public void close(){
		
	}
}
