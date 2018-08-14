package g419.corpus.io.reader;

import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.Closeable;
import java.util.Iterator;


/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * <p>
 * ToDo: Add hasNext method
 * ToDo: AbstractDocumentReader as an inherited class from Iterator
 *
 * @author czuk
 */
public abstract class AbstractDocumentReader implements Closeable, Iterator<Document>, Iterable<Document> {

    protected abstract TokenAttributeIndex getAttributeIndex();

    public abstract Document nextDocument() throws Exception;

    public abstract boolean hasNext();

    @Override
    public Document next(){
        final Document doc;
        try {
            doc = nextDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }

    @Override
    public Iterator<Document> iterator(){
        return this;
    }
}
