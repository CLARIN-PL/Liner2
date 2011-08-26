package liner2.structure;

import java.util.ArrayList;

public class ParagraphSet {

	AttributeIndex attributeIndex = null;
	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public AttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public void setAttributeIndex(AttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Paragraph p : this.paragraphs)
			p.setAttributeIndex(this.attributeIndex);
	}
}
