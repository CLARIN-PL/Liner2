package liner2.reader;

import java.io.IOException;

import liner2.structure.Document;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

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
