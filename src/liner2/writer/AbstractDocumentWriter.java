package liner2.writer;

import liner2.structure.Document;

public abstract class AbstractDocumentWriter {

	public abstract void writeDocument(Document document);
	public abstract void flush();
	public abstract void close();
	
}
