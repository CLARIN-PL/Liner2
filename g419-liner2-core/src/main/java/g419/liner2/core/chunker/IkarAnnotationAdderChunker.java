package g419.liner2.core.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class IkarAnnotationAdderChunker extends Chunker {
	private List<Pattern> convertTypes;
	private List<Pattern> mentionTypes;
	// TODO: sprawdzić czy to jest ppron3, czy trzeba sprawdzić jeszcze osobę
	private static final String PPRON3 = "ppron3";
	private static final String SUBST = "subst";
	
	private final boolean annotateAgP;
	private final boolean annotatePron;
	private final boolean annotateVerbs;

	public IkarAnnotationAdderChunker(boolean agps, boolean prons, boolean verbs) {
		this.convertTypes = new ArrayList<Pattern>();
		this.convertTypes.add(Pattern.compile("chunk_agp"));
		this.mentionTypes = new ArrayList<Pattern>();
		this.mentionTypes.add(Pattern.compile("anafora_wyznacznik"));
		this.mentionTypes.add(Pattern.compile(".*nam"));
		this.annotateAgP = agps;
		this.annotatePron = prons;
		this.annotateVerbs = verbs;
	}

	private boolean mentionDoesNotExist(Integer singleTokenId, Sentence sentence){
		boolean mentionExists = false;
		for (Annotation mention : sentence.getAnnotations(mentionTypes)) {
			if (mention.getTokens().size() == 1 && mention.getTokens().contains(singleTokenId)) {
				mentionExists = true;
				break;
			}
		}
		return !mentionExists;
	}
	
	private boolean mentionDoesNotExist(Annotation annotation){
		boolean mentionExists = false;
		Sentence sentence = annotation.getSentence();
		for (Annotation mention : sentence.getAnnotations(mentionTypes)) {
			if (annotation.getTokens().equals(mention.getTokens())) {
				mentionExists = true;
				break;
			}
		}
		return !mentionExists;
	}
	
	private boolean containsNoun(Annotation annotation, TokenAttributeIndex ai){
		boolean containsNoun = false;
		for(int index : annotation.getTokens()){
			Token token = annotation.getSentence().getTokens().get(index);
			if(SUBST.equals(ai.getAttributeValue(token, "class"))){
				containsNoun = true;
				break;
			}
		}
		return containsNoun;
	}
	
	public boolean crossNamOrMention(Annotation annotation){
		boolean cross = false;
		List<Token> sentenceTokens = annotation.getSentence().getTokens();
		for(int tokenId : annotation.getTokens()){
			if(annotation.getSentence().getChunksAt(tokenId, mentionTypes).size() > 0){
				cross = true;
				break;
			}
		}
		return cross;
	}
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunking = new HashMap<Sentence, AnnotationSet>();
		for (Sentence sentence : ps.getSentences()) {
			TokenAttributeIndex ai = sentence.getAttributeIndex();
			AnnotationSet annotationSet = new AnnotationSet(sentence);
			
			if(this.annotateAgP){
				// Dodaj wyznaczniki dla chunków AgP
				for (Annotation annotation : sentence.getAnnotations(convertTypes)) {
					if(mentionDoesNotExist(annotation) && containsNoun(annotation, ai) && !crossNamOrMention(annotation)){
						Annotation mention = new Annotation(annotation.getTokens(), "anafora_wyznacznik", sentence);
						sentence.addChunk(mention);
						annotationSet.addChunk(mention);
					}
				}
			}
			
			if(this.annotatePron){
				// Dodaj wyznaczniki dla zaimków
				for (Token token : sentence.getTokens()) {
					String tokenPos = ai.getAttributeValue(token, "class");
					int tokenPosition = sentence.getTokens().indexOf(token);
					if (PPRON3.equals(tokenPos)) {
						// Sprawdź czy wyznacznik już istnieje
						if (mentionDoesNotExist(tokenPosition, sentence)) {
							Annotation mention = new Annotation(tokenPosition, tokenPosition, "anafora_wyznacznik", sentence);
							sentence.addChunk(mention);
							annotationSet.addChunk(mention);
						}
					}
				}
			}
			
			if(this.annotateVerbs){
				// Dodaj wyznaczniki dla czasowników
				for (Token token : sentence.getTokens()) {
					String tokenPos = ai.getAttributeValue(token, "class");
					int tokenPosition = sentence.getTokens().indexOf(token);
					if (MinosChunker.MinosVerb.PartsOfSpeech.contains(tokenPos)) {
						// Sprawdź czy wyznacznik już istnieje
						if (mentionDoesNotExist(tokenPosition, sentence)) {
							Annotation mention = new Annotation(tokenPosition, tokenPosition, "anafora_wyznacznik", sentence);
							sentence.addChunk(mention);
							annotationSet.addChunk(mention);
						}
					}
					
				}
			}
			
			chunking.put(sentence, annotationSet);
		}
		return chunking;
	}
}
