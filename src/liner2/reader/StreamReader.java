package liner2.reader;

import java.util.ArrayList;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class StreamReader {

	public abstract Paragraph readParagraph();
	public abstract void close();
	
	/**
	 * TODO
	 * @return
	 */
	public ParagraphSet readParagraphSet(){
		ParagraphSet paragraphSet = new ParagraphSet();
		Paragraph p = null;
		while (true) {
			p = readParagraph();
			if (p != null)
				paragraphSet.addParagraph(p);
			else
				break;
		}
		close();
		return paragraphSet; 
	}
}
