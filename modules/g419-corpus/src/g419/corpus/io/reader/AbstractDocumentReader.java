package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;


/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class AbstractDocumentReader {

	protected abstract TokenAttributeIndex getAttributeIndex();
	
	public abstract void close() throws DataFormatException;
		
	public abstract Document nextDocument() throws Exception;
}
