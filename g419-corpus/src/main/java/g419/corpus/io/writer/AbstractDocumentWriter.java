package g419.corpus.io.writer;

import g419.corpus.structure.Document;

import java.io.Closeable;

public abstract class AbstractDocumentWriter implements Closeable {

	public abstract void writeDocument(Document document);
	public abstract void flush();

}
