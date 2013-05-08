package liner2.reader;

import liner2.features.NerdFeatureGenerator;
import liner2.structure.TokenAttributeIndex;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

import liner2.tools.DataFormatException;

import liner2.Main;
import liner2.LinerOptions;
/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class StreamReader {

	protected abstract TokenAttributeIndex getAttributeIndex();
	protected abstract Paragraph readRawParagraph() throws DataFormatException;
	public abstract void close() throws DataFormatException;
	public abstract boolean paragraphReady() throws DataFormatException;
		
	public Paragraph readParagraph() throws DataFormatException {
		if (paragraphReady())
			return this.readRawParagraph();
		else
			return null;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public ParagraphSet readParagraphSet() throws DataFormatException {
		ParagraphSet paragraphSet = new ParagraphSet();
					
		while (paragraphReady())
			paragraphSet.addParagraph(readRawParagraph());
		paragraphSet.setAttributeIndex(this.getAttributeIndex());
		close();
						
		return paragraphSet; 
	}
}
