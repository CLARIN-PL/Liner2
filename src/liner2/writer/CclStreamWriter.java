package liner2.writer;

import java.util.ArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class CclStreamWriter extends StreamWriter {

	public final String TAG_BASE 			= "base";
	public final String TAG_CTAG			= "ctag";
	public final String TAG_ID 				= "id";
	public final String TAG_ORTH			= "orth";
	public final String TAG_PARAGRAPH 		= "chunk";
	public final String TAG_PARAGRAPH_SET 	= "chunkList";
	public final String TAG_SENTENCE		= "sentence";
	public final String TAG_TAG				= "lex";
	public final String TAG_TOKEN 			= "tok";

	private XMLStreamWriter xmlw;
	private boolean open = false;
	
	public CclStreamWriter(String filename) {
		OutputStream os = System.out;
		if ((filename != null) && (!filename.isEmpty())) {
			try {
				os = new FileOutputStream(filename);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		XMLOutputFactory xmlof = XMLOutputFactory.newFactory();
		try {
			this.xmlw = xmlof.createXMLStreamWriter(os);
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	public void open() {
		if (open)
			return;
		try {
			xmlw.writeStartDocument();
			xmlw.writeCharacters("\n");
			xmlw.writeStartElement(TAG_PARAGRAPH_SET);
			xmlw.writeCharacters("\n");
		}
		catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
		open = true;
	}
	
	@Override
	public void close() {
		try {
			xmlw.writeEndDocument();
			xmlw.close();
		}
		catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!open)
				open();
			xmlw.writeStartElement(TAG_PARAGRAPH);
			xmlw.writeCharacters("\n");
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(sentence);
			xmlw.writeEndElement();
			xmlw.writeCharacters("\n");
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws XMLStreamException {
		xmlw.writeStartElement(TAG_SENTENCE);
		xmlw.writeCharacters("\n");
		for (Token token : sentence.getTokens())
			writeToken(token);
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}
	
	private void writeToken(Token token) throws XMLStreamException {
		xmlw.writeStartElement(TAG_TOKEN);
		xmlw.writeCharacters("\n");
		xmlw.writeStartElement(TAG_ORTH);
		xmlw.writeCharacters(token.getFirstValue());
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
		for (Tag tag : token.getTags())
			writeTag(tag);
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}
	
	private void writeTag(Tag tag) throws XMLStreamException {
		xmlw.writeStartElement(TAG_TAG);
		xmlw.writeStartElement(TAG_BASE);
		xmlw.writeCharacters(tag.getBase());
		xmlw.writeEndElement();
		xmlw.writeStartElement(TAG_CTAG);
		xmlw.writeCharacters(tag.getCtag());
		xmlw.writeEndElement();
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}

}
