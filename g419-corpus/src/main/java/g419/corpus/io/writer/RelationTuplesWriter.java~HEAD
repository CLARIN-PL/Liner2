package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationTuplesWriter extends AbstractDocumentWriter {

	private BufferedWriter writer;
	
	public RelationTuplesWriter(OutputStream os){
		this.writer = new BufferedWriter(new OutputStreamWriter(os));
	}
		
	@Override
	public void writeDocument(Document document) {
		Map<Sentence, List<Integer>> index = this.makeOffsetIndex(document);
		for (Relation relation : document.getRelations().getRelations())
			try {
				Annotation af = relation.getAnnotationFrom();
				Annotation at = relation.getAnnotationTo();
				String line = String.format("(%d,%d,%s,\"%s\",%d,%d,%s,\"%s\",%s)\n", 
						index.get(af.getSentence()).get(af.getBegin()), 
						index.get(af.getSentence()).get(af.getEnd())-1,
						af.getType(), af.getText(),
						index.get(at.getSentence()).get(at.getBegin()), 
						index.get(at.getSentence()).get(at.getEnd())-1,
						at.getType(), at.getText(),
						relation.getType());
				writer.write(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * 
	 * @param document
	 * @return
	 */
	private Map<Sentence, List<Integer>> makeOffsetIndex(Document document){
		HashMap<Sentence, List<Integer>> index = new HashMap<Sentence, List<Integer>>();
		int offset = 0;
		for (Paragraph paragraph : document.getParagraphs())
			for (Sentence sentence : paragraph.getSentences()){
				ArrayList<Integer> sentenceIndex = new ArrayList<Integer>();
				for (Token token : sentence.getTokens()){
					sentenceIndex.add(offset);
					offset += token.getOrth().length() + 1;
				}
				sentenceIndex.add(offset);
				index.put(sentence, sentenceIndex);
			}
		return index;
	}

	@Override
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
