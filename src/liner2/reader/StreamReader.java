package liner2.reader;

import java.util.ArrayList;

import liner2.structure.Sentence;

/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class StreamReader {

	public abstract Sentence readSentence();
	
	/**
	 * TODO
	 * @return
	 */
	public ArrayList<Sentence> readAllSentences(){
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		
		return sentences; 
	}
	
	public void close(){
		
	}
}
