package liner2.chunker;

import java.util.ArrayList;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 * Klasyfikator dwuprzebiegowy aktualizujący cechy słownikowe.
 */

public class AduChunker extends Chunker {
	private Chunker baseChunker = null;
	private boolean one = false;

	public void setSettings(Chunker baseChunker, boolean one) {
		this.baseChunker = baseChunker;
		this.one = one;
	}

	@Override
	public Chunking chunkSentence(Sentence sentence) {
		return this.baseChunker.chunkSentence(sentence);
	}
	
	@Override
	public void prepare(ParagraphSet ps) {
		for (Paragraph p : ps.getParagraphs()) {
			for (Sentence s : p.getSentences()) {
				Chunking chunking = this.baseChunker.chunkSentence(s);
				ArrayList<Token> tokens = s.getTokens();

				for (Chunk chunk : chunking.chunkSet()) {
					AttributeIndex ai = s.getAttributeIndex();
					int featureIdx = ai.getIndex(chunk.getType().toLowerCase());
					boolean updateFeature = true;
					for (int i = chunk.getBegin(); i < chunk.getEnd(); i++) {
						if (!tokens.get(i).getAttributeValue(featureIdx).equals("O"))
							updateFeature = false;
					}
					if (updateFeature) {
						tokens.get(chunk.getBegin()).setAttributeValue(featureIdx, "B");
						for (int i = chunk.getBegin()+1; i < chunk.getEnd(); i++)
							tokens.get(i).setAttributeValue(featureIdx, "I");
					}
				}
			}
		}
	}
}
