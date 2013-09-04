package liner2.reader;

import liner2.LinerOptions;
import liner2.features.TokenFeatureGenerator;
import liner2.structure.TokenAttributeIndex;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

import liner2.tools.DataFormatException;
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
		
	public Paragraph readParagraph() throws Exception {
		if (paragraphReady()){
			Paragraph p = this.readRawParagraph();
			p.setAttributeIndex(this.getAttributeIndex());
			return p;
		}
		else
			return null;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public ParagraphSet readParagraphSet() throws Exception {
		ParagraphSet paragraphSet = new ParagraphSet();
					
		while (paragraphReady())
			paragraphSet.addParagraph(readRawParagraph());
		paragraphSet.setAttributeIndex(this.getAttributeIndex());
		close();
						
		return paragraphSet; 
	}
}
