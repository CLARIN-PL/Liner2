package g419.corpus.io.writer;

import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class IobTabStreamWriter extends AbstractDocumentWriter {

	private BufferedWriter ow;
	private boolean init = false;
	private ArrayList<Integer> widths = new ArrayList<Integer>();

	public IobTabStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	protected void init(TokenAttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
			String line = "-DOCSTART CONFIG FEATURES";
			for (int i = 0; i < attributeIndex.getLength(); i++)
				line += " " + attributeIndex.getName(i);
			ow.write(line, 0, line.length());
			ow.newLine();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		this.init = true;
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void close() {
		try {
			ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeDocument(Document document){
		this.calcuateWidths(document);
		for (Paragraph paragraph : document.getParagraphs())
			this.writeParagraph(paragraph);
	}

	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!init)
				init(paragraph.getAttributeIndex());
			String paragraphId = paragraph.getId();
			if (paragraphId == null)
				paragraphId = "";
			String header = "-DOCSTART FILE "+ paragraphId;
			ow.write(header, 0, header.length());
			ow.newLine();
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(sentence);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws IOException {
		List<Token> tokens = sentence.getTokens();
		for (int i = 0; i < tokens.size(); i++) {
			writeToken(i, tokens.get(i), sentence);
		}
		ow.newLine();
	}
	
	private void writeToken(int idx, Token token, Sentence sentence) 
		throws IOException {
		String line = "";
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++){
			line += (line.length() > 0 ? " " : "");
			if ( this.widths.size() > i )
				line += String.format("%-"+this.widths.get(i)+"s", token.getAttributeValue(i));
			else
				line += token.getAttributeValue(i);
		}

		line += " " + sentence.getTokenClassLabel(idx, null);
		ow.write(line, 0, line.length());
		ow.newLine();
	}
	
	private void calcuateWidths(Document document){
		this.widths.clear();
		for ( int i=0; i<document.getAttributeIndex().getLength(); i++ )
			this.widths.add(0);
		for (Paragraph paragraph : document.getParagraphs())
			for (Sentence sentence : paragraph.getSentences())
				for (Token token : sentence.getTokens())
					for ( int i = 0; i<this.widths.size(); i++)
						this.widths.set(i, Math.max(this.widths.get(i), (""+token.getAttributeValue(i)).length()));
	}
		
}
