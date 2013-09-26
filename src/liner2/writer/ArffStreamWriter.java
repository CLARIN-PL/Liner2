package liner2.writer;

import java.util.ArrayList;
import java.util.Hashtable;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

import liner2.structure.*;

import liner2.tools.Template;
import liner2.tools.TemplateFactory;

public class ArffStreamWriter extends StreamWriter{

	private BufferedWriter ow;
	private boolean init = false;
    private Template template;

	public ArffStreamWriter(OutputStream os, Template template) {
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
        this.template = template;
	}

    public void writeParagraphSet(ParagraphSet paragraphSet) {
        for (Paragraph p : paragraphSet.getParagraphs())
            writeParagraph(p);
        close();
    }


	protected void init(TokenAttributeIndex attributeIndex) {
		if (this.init)
			return;
		try {
//			String line = "-DOCSTART CONFIG FEATURES orth base ctag";
			TokenAttributeIndex newAttributeIndex = template.expandAttributeIndex(attributeIndex);
			String line = "@relation rel";
			ow.write(line, 0, line.length());
			ow.newLine();
			ow.newLine();
			for (int i = 0; i < newAttributeIndex.getLength(); i++) {
				String featureName = newAttributeIndex.getName(i);
				line = "@attribute " + newAttributeIndex.getName(i) + " string";
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
	public void close() {
		try {
			ow.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!init)
				init(paragraph.getAttributeIndex());
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(template.expandAttributes(sentence));
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
//		String line = token.getFirstValue();
//		ArrayList<Tag> tags = token.getTags();
//		Tag firstTag = tags.getGlobal(0);
//		line += " " + firstTag.getBase() + " " + firstTag.getCtag();
		String line = "";
//		for (int i = 0; i < token.getNumAttributes(); i++)
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++) {
//			System.out.println("" + i + ": " + sentence.getAttributeIndex().getName(i));
			String attrval = token.getAttributeValue(i);
			if (attrval == null)
				attrval = "?";
			else
				attrval = "\'" + attrval.replace("\'", "\\\'") + "\'";
			line += (line.length() > 0 ? ",\t" : "") + attrval;
		}
		
		Annotation chunk = sentence.getChunkAt(idx);
		if (chunk == null)
			line += ",\tO";
		else {
			if (idx == chunk.getBegin())
				line += ",\tB-" + chunk.getType();
			else
				line += ",\tI-" + chunk.getType();
		}
		ow.write(line, 0, line.length());
		ow.newLine();
	}
		
	

}
