package g419.crete.api.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.enumvalues.MentionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
	
	public static MentionType getMentionType(Annotation mention){
		if(mention.getType().startsWith("nam")) return MentionType.NAMED_ENTITY;
//		else if(mention.getType().equalsIgnoreCase("anafora_wyznacznik")){
//			if(mention.isOverlappedByAny(Arrays.asList(new Pattern[]{Pattern.compile("chunk_agp")}))){
//				return MentionType.AGP;
//			}
//			return MentionType.PRONOUN;
//		}
		else if(mention.getType().equalsIgnoreCase("anafora_wyznacznik")){
			if(mention.getTokens().size() == 1){
				TokenAttributeIndex ai = mention.getSentence().getAttributeIndex();
				String tokenBase = ai.getAttributeValue(mention.getSentence().getTokens().get(mention.getTokens().first()), "base");
				// FIXME: rozszerzyć definicję pronoun
				if(MentionType.isPronounBase(tokenBase)){
					return MentionType.PRONOUN;
				}
			}
			return MentionType.AGP;
		}
		else return MentionType.NULL_VERB;
	}
	
	
	public static List<Token> tokensBetweenAnnotations(Annotation ann1, Annotation ann2, Document document){
		List<Token> tokens = new ArrayList<Token>();
		if(ann1.getSentence().getOrd() == ann2.getSentence().getOrd()){
			for(int i = ann1.getEnd(); i < ann2.getBegin(); i++) tokens.add(ann1.getSentence().getTokens().get(i));
			return tokens;
		}
		
		int ann1SentTokens = ann1.getSentence().getTokens().size();
		for(int i = ann1.getEnd(); i < ann1SentTokens; i++) tokens.add(ann1.getSentence().getTokens().get(i));
		
		for(Paragraph paragraph : document.getParagraphs())
			for(Sentence sentence: paragraph.getSentences())
				if(sentence.getOrd() > ann1.getSentence().getOrd() && sentence.getOrd() < ann2.getSentence().getOrd())
					tokens.addAll(sentence.getTokens());
					
		for(int i = 0; i < ann2.getBegin(); i++) tokens.add(ann2.getSentence().getTokens().get(i));
		
		return tokens;
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
		
		int dist = ann1.getSentence().getTokenNumber() - ann1.getEnd();
		
		for(Paragraph paragraph : document.getParagraphs())
			for(Sentence sentence: paragraph.getSentences())
				if(sentence.getOrd() > ann1.getSentence().getOrd() && sentence.getOrd() < ann2.getSentence().getOrd())
					dist += sentence.getTokenNumber();
		
		dist += ann2.getBegin();
		
		return dist;
	}
	
	public static int annotationSentenceDistance(Annotation ann1, Annotation ann2){
		return ann1.getSentence().getOrd() - ann2.getSentence().getOrd();
	}

	public static List<Annotation> annotationsBetweenAnnotations(Annotation ann1, Annotation ann2, Document document, List<Pattern> types) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		if(ann1.getSentence().getOrd() == ann2.getSentence().getOrd()){
			for(Annotation ann : ann1.getSentence().getAnnotations(types))
				if(ann.getBegin() > ann1.getEnd() && ann.getEnd() < ann2.getBegin())
					annotations.add(ann);
			return annotations;
		}
		
		for(Annotation ann : ann1.getSentence().getAnnotations(types))
			if(ann.getBegin() > ann1.getEnd())
				annotations.add(ann);
		
		for(Paragraph paragraph : document.getParagraphs())
			for(Sentence sentence: paragraph.getSentences())
				if(sentence.getOrd() > ann1.getSentence().getOrd() && sentence.getOrd() < ann2.getSentence().getOrd())
					annotations.addAll(sentence.getAnnotations(types));
		
		for(Annotation ann : ann2.getSentence().getAnnotations(types))
			if(ann.getEnd() < ann2.getBegin())
				annotations.add(ann);
		
				
		return annotations;
	}

}
