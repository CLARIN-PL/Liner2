package g419.corpus.io.reader;

import g419.corpus.HasLogger;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.Closeable;
import java.util.Iterator;


/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 */
public abstract class AbstractDocumentReader implements Closeable, Iterator<Document>, Iterable<Document>, HasLogger {

    protected abstract TokenAttributeIndex getAttributeIndex();

    public abstract Document nextDocument() throws Exception;

    @Override
    public abstract boolean hasNext();

    @Override
    public Document next() {
        try {
            return nextDocument();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Iterator<Document> iterator() {
        return this;
    }
}
