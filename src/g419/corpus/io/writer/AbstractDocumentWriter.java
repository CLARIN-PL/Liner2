package g419.corpus.io.writer;

import g419.corpus.structure.Document;

public abstract class AbstractDocumentWriter {

	public abstract void writeDocument(Document document);
	public abstract void flush();
	public abstract void close();
	
}
