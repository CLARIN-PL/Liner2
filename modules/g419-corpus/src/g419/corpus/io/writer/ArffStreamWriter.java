package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class ArffStreamWriter extends AbstractDocumentWriter{

	private BufferedWriter ow;
	private boolean init = false;

	public ArffStreamWriter(OutputStream os) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
    public void writeDocument(Document paragraphSet) {
        for (Paragraph p : paragraphSet.getParagraphs())
            writeParagraph(p);
    }


	protected void init(TokenAttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
			String line = "@relation rel";
			ow.write(line, 0, line.length());
			ow.newLine();
			ow.newLine();
			for (int i = 0; i < attributeIndex.getLength(); i++) {
				line = "@attribute " + attributeIndex.getName(i) + " string";
				ow.write(line, 0, line.length());
				ow.newLine();
			}
			line = "@attribute iobtag string";
			ow.write(line, 0, line.length());
			ow.newLine();
			ow.newLine();
			line = "@data";
			ow.write(line, 0, line.length());
			ow.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
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
	
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!init)
				init(paragraph.getAttributeIndex());
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(sentence);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws IOException {
		ArrayList<Token> tokens = sentence.getTokens();
		for (int i = 0; i < tokens.size(); i++) {
			writeToken(i, tokens.get(i), sentence);
		}
	}
	
	private void writeToken(int idx, Token token, Sentence sentence) 
		throws IOException {
		String line = "";
		for (int i = 0; i < token.getNumAttributes(); i++){
			String attrval = token.getAttributeValue(i);
			if (attrval == null)
				attrval = "?";
			else
				attrval = "\'" + attrval.replace("\'", "\\\'") + "\'";
			line += (line.length() > 0 ? ",\t" : "") + attrval;
		}

        line += " " + sentence.getTokenClassLabel(idx, null);
		ow.write(line, 0, line.length());
		ow.newLine();
	}
		
	

}
