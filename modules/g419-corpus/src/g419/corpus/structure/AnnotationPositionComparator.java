package g419.corpus.structure;

import java.util.Comparator;


/**
 * Porównywarka anotacji służąca do sortowania wg. pozycji w tekście
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class AnnotationPositionComparator implements Comparator<Annotation>{

	
	private int parseSentenceId(String sentenceId){
		if(sentenceId.startsWith("sentence")){
			return Integer.parseInt(sentenceId.substring(8));
		}
		
		if(sentenceId.startsWith("sent")){
			return Integer.parseInt(sentenceId.substring(4));
		}
		
		if(sentenceId.startsWith("s")){
			return Integer.parseInt(sentenceId.substring(1));
		}
		try{
			return Integer.parseInt(sentenceId);
		}
		catch(NumberFormatException ex){
			String sentenceNumId = sentenceId.split("-")[1].split("\\.")[0];
			return Integer.parseInt(sentenceNumId);
		}
	}
	
	
	/**
	 * Anotacje są porównywane wg. pozycji w tekście, a następnie wg. długości
	 */
	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		// Różnica zdań
		int sentDiff = parseSentenceId(ann1.getSentence().getId()) - parseSentenceId(ann2.getSentence().getId());
		if(sentDiff != 0) return sentDiff;
		// Różnica pozycji wewnątrz zdania
		int posDiff = ann1.getBegin() - ann2.getBegin();
		if(posDiff != 0) return posDiff;
		
		int sizeDiff = ann1.getTokens().size() - ann2.getTokens().size();
		if(sizeDiff != 0) return sizeDiff;
		
		return ann1.getType().compareTo(ann2.getType());
	}
	
}
