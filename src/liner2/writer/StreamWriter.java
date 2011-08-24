package liner2.writer;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

public abstract class StreamWriter {

	public abstract void writeParagraph(Paragraph paragraph);
	public abstract void close();
	
	public void writeParagraphSet(ParagraphSet paragraphSet) {
		for (Paragraph p : paragraphSet.getParagraphs())
			writeParagraph(p);
		close();
	}
}
