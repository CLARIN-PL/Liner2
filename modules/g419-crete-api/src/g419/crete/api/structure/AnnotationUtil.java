package g419.crete.api.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

public class AnnotationUtil {
	
	public static String getAnnotationHeadAttribute(Annotation annotation, String attribute){
		annotation.assignHead();
		Token headToken = annotation.getSentence().getTokens().get(annotation.getHead());
		TokenAttributeIndex ai = annotation.getSentence().getAttributeIndex();
		
		return ai.getAttributeValue(headToken, attribute);
	}
	
	public static Annotation getClosestPreceeding(Annotation mention, AnnotationCluster cluster){
		AnnotationPositionComparator comparator = new AnnotationPositionComparator();
		
		Annotation closestPreceeding = null;
		for(Annotation annotation : cluster.getAnnotations()){
			if(comparator.compare(annotation, mention) < 0){
				closestPreceeding = annotation;
			}
			else{
				break;
			}
		}
		
		return closestPreceeding;
	}
	
	public static Annotation getClosestFollowing(Annotation mention, AnnotationCluster cluster){
		AnnotationPositionComparator comparator = new AnnotationPositionComparator();
		
		Annotation closestFollowing = null;
		for(Annotation annotation : cluster.getAnnotations())
			if(comparator.compare(annotation, mention) > 0)
				closestFollowing = annotation;
		
		return closestFollowing;
	}
	
	
	/**
	 *  (...) ann1 (...) ann2 (...)
	 * @param ann1 - Poprzedzająca (bliższa) anotacja
	 * @param ann2 - Poprzedzana (dalsza) anotacja
	 * @param document - Dokument
	 * @return dystans w tokenach pomiędzy anotacjami
	 */
	public static int annotationTokenDistance(Annotation ann1, Annotation ann2, Document document){
		if(ann1.getSentence().getOrd() == ann2.getSentence().getOrd()) return ann2.getBegin() - ann1.getEnd();
		
		int dist = ann2.getSentence().getTokenNumber() - ann1.getEnd();
		
		for(Paragraph paragraph : document.getParagraphs())
			for(Sentence sentence: paragraph.getSentences())
				if(sentence.getOrd() > ann1.getSentence().getOrd() && sentence.getOrd() < ann2.getSentence().getOrd())
					dist += sentence.getTokenNumber();
		
		dist += ann1.getBegin();
		
		return dist;
	}
	
	public static int annotationSentenceDistance(Annotation ann1, Annotation ann2){
		return ann1.getSentence().getOrd() - ann2.getSentence().getOrd();
	}
}
