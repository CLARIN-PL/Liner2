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
	public abstract boolean paragraphReady();
		
	public Paragraph readParagraph(){
//		Paragraph p = this.readRawParagraph();
//		if (p == null)
//			return null;
//		if (FeatureGenerator.isInitialized()) {
//			try {
//				FeatureGenerator.generateFeatures(p, true);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		return p;
		if (paragraphReady()) {
			Paragraph p = this.readRawParagraph();
			if (FeatureGenerator.isInitialized()) {
				try {
					FeatureGenerator.generateFeatures(p, true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return p;
		} 
		else
			return null;
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

		while (paragraphReady())
			paragraphSet.addParagraph(readRawParagraph());
		
//		Paragraph p = null;
//		while (true) {
//			p = readRawParagraph();
//			if (p != null){
//				p.setAttributeIndex(attributeIndex);
//				paragraphSet.addParagraph(p);
//			}else
//				break;
//		}
		if (FeatureGenerator.isInitialized()) {
			try {
				FeatureGenerator.generateFeatures(paragraphSet);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		close();
						
		return paragraphSet; 
	}
}
