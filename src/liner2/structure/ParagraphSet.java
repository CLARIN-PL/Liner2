package liner2.structure;

import java.util.ArrayList;

public class ParagraphSet {

	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return paragraphs;
	}
}
