package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes a set of annotations in a BSNLP2017 format.
 * The output consists of the following fields:
 * Number of annotations
 * <ul>
 *  <li>text form,</li>
 *  <li>lemma,</li>
 *  <li>type,</li>
 * 	<li>lemma ID.</li>
 * </ul>
 * 
 * @author Jan Kocoń
 */
public class BSNLPStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow = null;
	/** Current token index inside document */
	private int sentenceIndexOffset = 0;
	private Set<String> linesWritten = null;

	public BSNLPStreamWriter(OutputStream os) {
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
		this.linesWritten = new HashSet();
		// look for document ID
		String documentID = "0";
		Matcher matcher = Pattern.compile("\\d+").matcher(document.getName());
		if (matcher.find())
			documentID = matcher.group();
		try {
			this.ow.write(documentID + "\n");
		} catch (IOException ex) {
			Logger.getLogger(this.getClass()).error("There was an error while writing document ID", ex);
		}
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
	 * @throws IOException
	 */
	private void writeChunk(Annotation an) throws IOException {
		StringJoiner joiner = new StringJoiner("\t");
		String type = an.getType().toUpperCase();
		joiner.add(an.getText());
		joiner.add(an.getLemma());
		joiner.add(type);
		joiner.add("#" + an.getLemma() + "#" + type + "#");
		String newLine = joiner.toString() + "\n";

		if (!this.linesWritten.contains(newLine.toLowerCase())){
			this.linesWritten.add(newLine.toLowerCase());
			this.ow.write(newLine);
		}

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
