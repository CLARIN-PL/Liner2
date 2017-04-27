package g419.corpus.io.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringJoiner;

import org.apache.log4j.Logger;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

/**
 * Writes a set of annotations in a TSV format. 
 * The output consists of the following fields:
 * <ul>
 *  <li>lemma,</li>
 * 	<li>text form,</li>
 *  <li>token bases,</li>
 *  <li>ctags,</li>
 *  <li>nss,</li>
 *  <li>type,</li>
 *  <li>group.</li>
 * </ul>
 * 
 * @author Michał Marcińczuk
 */
public class AnnotationTsvStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow = null;
	
	/** Current token index inside document */
	private int sentenceIndexOffset = 0;
	
	public AnnotationTsvStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void close() {
		try {
			this.ow.flush();
			this.ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeDocument(Document document){
        this.sentenceIndexOffset = 0;
		for (Paragraph paragraph : document.getParagraphs()){
			for (Sentence sentence : paragraph.getSentences()){
				Annotation[] chunks = Annotation.sortChunks(sentence.getChunks());
				for (Annotation an : chunks) {
					try {
						writeChunk(an);
					} catch (IOException ex) {
						Logger.getLogger(this.getClass()).error("There was an error while writing an annotation", ex);
					}
				}			
				this.sentenceIndexOffset += sentence.getTokenNumber();			
			}
		}
		try {
			this.ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a single annotation.
	 * @param an
	 * @param sentence
	 * @throws IOException
	 */
	private void writeChunk(Annotation an) throws IOException {
		StringJoiner joiner = new StringJoiner("\t");
		//joiner.add("" + (this.sentenceIndexOffset + an.getBegin()));
		//joiner.add("" + (this.sentenceIndexOffset + an.getEnd()));
		joiner.add(an.getLemma());
		joiner.add(an.getText());
		joiner.add(an.getBaseText(false));
		joiner.add(an.getCtags());
		joiner.add(an.getNss());
		joiner.add(an.getType());
		joiner.add(an.getGroup());
		this.ow.write(joiner.toString() + "\n");
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
