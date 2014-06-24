package g419.liner2.api.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 * Klasyfikator dwuprzebiegowy aktualizujący cechy słownikowe.
 */

public class AduChunker extends Chunker {
	private Chunker baseChunker = null;
	private boolean one = false;
	private HashMap<String, String> dictionary = null;

	public AduChunker() {
		this.dictionary = new HashMap<String, String>();
	}

	public void setSettings(Chunker baseChunker, boolean one) {
		this.baseChunker = baseChunker;
		this.one = one;
	}

	private AnnotationSet chunkSentence(Sentence sentence) {
		ArrayList<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		int sentenceLength = sentence.getTokenNumber();

		ArrayList<HashMap<Integer, String>> nGrams = 
			new ArrayList<HashMap<Integer, String>>();
		
		// wygeneruj unigramy
		nGrams.add(new HashMap<Integer, String>());		
		for (int i = 0; i < sentenceLength; i++)
			nGrams.get(0).put(new Integer(i), tokens.get(i).getOrth());
		// wygeneruj n-gramy
		for (int n = 1; n < sentenceLength; n++) {
			nGrams.add(new HashMap<Integer, String>());
			for (int j = 0; j < sentenceLength - n; j++)
				nGrams.get(n).put(new Integer(j), 
					nGrams.get(n-1).get(j) + " " + tokens.get(j+n).getOrth());
		}
		
		// aktualizuj cechy słownikowe (poczynając od najdłuższych n-gramów)
		for (int n = sentenceLength - 1; n >= 0; n--) {
			for (int i = 0; i < sentenceLength - n; i++) {
				int idx = new Integer(i);
				
				// jeśli danego n-gramu nie ma w tablicy, to kontynuuj
				if (nGrams.get(n).get(idx) == null)
					continue;
				
				// jeśli znaleziono w słowniku
				if (this.dictionary.containsKey(nGrams.get(n).get(idx))) {
					String featureName = this.dictionary.get(nGrams.get(n).get(idx))
						.toLowerCase();
					int featureIdx = ai.getIndex(featureName);
					boolean updateFeature = true;
					for (int j = i; j < i+n; j++) {
						if (!tokens.get(j).getAttributeValue(featureIdx).equals("O")) {
							updateFeature = false;
							break;
						}
					}
					if (updateFeature) {
						tokens.get(i).setAttributeValue(featureIdx, "B");
						for (int j = i+1; j < i+n; j++)
							tokens.get(j).setAttributeValue(featureIdx, "I");
					}
				}
			}
		}
		return null;

		//return this.baseChunker.chunkSentence(sentence);
	}
	
//	@Override
//	public void prepare(ParagraphSet ps) {
//		for (Paragraph p : ps.getParagraphs()) {
//			for (Sentence s : p.getSentences()) {
//				Chunking chunking = this.baseChunker.chunkSentence(s);
//				ArrayList<Token> tokens = s.getTokens();
//
//				for (Chunk chunk : chunking.chunkSet()) {
//					String seq = tokens.getGlobal(chunk.getBegin()).getOrth();
//				 	for (int i = chunk.getBegin()+1; i < chunk.getEnd(); i++)
//						seq += " " + tokens.getGlobal(i).getOrth();
//					if (!this.dictionary.containsKey(seq))
//						this.dictionary.put(seq, chunk.getType());
//				} 

//				for (Chunk chunk : chunking.chunkSet()) {
//					AttributeIndex ai = s.getAttributeIndex();
//					int featureIdx = ai.getIndex(chunk.getType().toLowerCase());
//					boolean updateFeature = true;
//					for (int i = chunk.getBegin(); i < chunk.getEnd(); i++) {
//						if (!tokens.getGlobal(i).getAttributeValue(featureIdx).equals("O"))
//							updateFeature = false;
//					}
//					if (updateFeature) {
//						tokens.getGlobal(chunk.getBegin()).setAttributeValue(featureIdx, "B");
//						for (int i = chunk.getBegin()+1; i < chunk.getEnd(); i++)
//							tokens.getGlobal(i).setAttributeValue(featureIdx, "I");
//					}
//				}
//			}
//		}
//	}

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
}
