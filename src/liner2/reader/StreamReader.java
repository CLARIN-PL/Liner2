package liner2.reader;

import liner2.structure.AttributeIndex;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

/**
 * Abstrakcyjna klasa do strumieniowego wczytywania danych.
 * @author czuk
 *
 */
public abstract class StreamReader {

	protected abstract Paragraph readRawParagraph();
		
	public abstract void close();
		
	public Paragraph readParagraph(){
		Paragraph p = this.readRawParagraph();
		if (p == null)
			return null;
		try {
			FeatureGenerator.generateFeatures(p);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return p;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public ParagraphSet readParagraphSet(){
		ParagraphSet paragraphSet = new ParagraphSet();
					
		// initialize attributes index
		AttributeIndex attributeIndex = new AttributeIndex();
		attributeIndex.addAttribute("orth");
		attributeIndex.addAttribute("base");
		attributeIndex.addAttribute("ctag");
		paragraphSet.setAttributeIndex(attributeIndex);
		
		Paragraph p = null;
		while (true) {
			p = readParagraph();
			if (p != null)
				paragraphSet.addParagraph(p);
			else
				break;
		}
		close();
						
		return paragraphSet; 
	}
}
