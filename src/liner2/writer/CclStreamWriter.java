package liner2.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import liner2.structure.Annotation;
import liner2.structure.Document;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class CclStreamWriter extends AbstractDocumentWriter {

	private final String TAG_ANN			= "ann";
	private final String TAG_BASE 			= "base";
	private final String TAG_CHAN			= "chan";
	private final String TAG_CTAG			= "ctag";
	private final String TAG_DISAMB		= "disamb";
	private final String TAG_ID			= "id";
	private final String TAG_NS			= "ns";
	private final String TAG_ORTH			= "orth";
	private final String TAG_PARAGRAPH 	= "chunk";
	private final String TAG_PARAGRAPH_SET	= "chunkList";
	private final String TAG_SENTENCE		= "sentence";
	private final String TAG_TAG			= "lex";
	private final String TAG_TOKEN 		= "tok";
	private final String TAG_HEAD 		= "head";

	private XMLStreamWriter xmlw;
	private OutputStream os;
	private XMLOutputFactory xmlof = null;
	private boolean indent = true;
	
	public CclStreamWriter(OutputStream os) {
		this.os = os;
		this.xmlof = XMLOutputFactory.newFactory();
	}
	
	private void openXml() {
		try {
			this.xmlw = xmlof.createXMLStreamWriter(os);
			xmlw.writeStartDocument("UTF-8", "1.0");
			xmlw.writeCharacters("\n");
			xmlw.writeDTD("<!DOCTYPE chunkList SYSTEM \"ccl.dtd\">");
			xmlw.writeCharacters("\n");
			xmlw.writeStartElement(TAG_PARAGRAPH_SET);
			xmlw.writeCharacters("\n");
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	private void closeXml() {
		try {
			xmlw.writeEndDocument();
			xmlw.close();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void flush() {
		try {
			this.os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
            if(!(os instanceof PrintStream))
                os.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeDocument(Document document){
		this.openXml();
		for (Paragraph paragraph : document.getParagraphs())
			this.writeParagraph(paragraph);
		this.closeXml();
	}	

	private void writeParagraph(Paragraph paragraph) {
		try {
			this.indent(1);
			xmlw.writeStartElement(TAG_PARAGRAPH);
			
			Set<String> chunkMetaDataKeys = paragraph.getKeysChunkMetaData();
			for(String key : chunkMetaDataKeys){
				xmlw.writeAttribute(key, paragraph.getChunkMetaData(key));
			}
			
			if (paragraph.getId() != null)
				xmlw.writeAttribute(TAG_ID, paragraph.getId());
			xmlw.writeCharacters("\n");
			for (Sentence sentence : paragraph.getSentences())
				writeSentence(sentence);
			this.indent(1);
			xmlw.writeEndElement();
			xmlw.writeCharacters("\n");
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeSentence(Sentence sentence) throws XMLStreamException {
		this.indent(2);
		xmlw.writeStartElement(TAG_SENTENCE);
		if (sentence.getId() != null)
			xmlw.writeAttribute(TAG_ID, sentence.getId());
		xmlw.writeCharacters("\n");
		
		// prepare annotation channels
		HashSet<Annotation> chunks = sentence.getChunks();
		ArrayList<String> channels = new ArrayList<String>();
		for (Annotation chunk : chunks) {
			if (chunk.getChannelIdx() == 0){
				int lastIdx = 0;
				for(Annotation a: chunks)
					if (a.getType().equals(chunk.getType()) && a.getChannelIdx() > lastIdx)
						lastIdx = a.getChannelIdx();
				chunk.setChannelIdx(lastIdx+1);
			}
			if (!channels.contains(chunk.getType()))
				channels.add(chunk.getType());
		}
		Collections.sort(channels);
		
		ArrayList<Token> tokens = sentence.getTokens();
		for (int i = 0; i < tokens.size(); i++)
			writeToken(i, tokens.get(i), chunks, channels);
		this.indent(2);
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}
	
	private void writeToken(int idx, Token token, HashSet<Annotation> chunks, ArrayList<String> channels)
		throws XMLStreamException {
		this.indent(3);
		xmlw.writeStartElement(TAG_TOKEN);
		if (token.getId() != null)
			xmlw.writeAttribute(TAG_ID, token.getId());
		xmlw.writeCharacters("\n");
		this.indent(4);
		xmlw.writeStartElement(TAG_ORTH);
		writeText(token.getFirstValue());
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
		for (Tag tag : token.getTags())
			writeTag(tag);
		
		Annotation[] tokenchannels = new Annotation[channels.size()];
		for (Annotation chunk : chunks) {
			if (chunk.getTokens().contains(idx))
				tokenchannels[channels.indexOf(chunk.getType())] = chunk;
		}

//		Collections.sort(sortedChannels);
		
		for (int chanIdx = 0; chanIdx < channels.size(); chanIdx++) {
			this.indent(4);
			xmlw.writeStartElement(TAG_ANN);
			Annotation ann = tokenchannels[chanIdx];
			if (ann != null){
				xmlw.writeAttribute(TAG_CHAN, ann.getType().toLowerCase());
				if (ann.hasHead() && ann.getHead() == idx)
					xmlw.writeAttribute(TAG_HEAD, "1");
				xmlw.writeCharacters("" + ann.getChannelIdx());
			}
			else{
				xmlw.writeAttribute(TAG_CHAN, channels.get(chanIdx).toLowerCase());
				xmlw.writeCharacters("0");
			}
			xmlw.writeEndElement();
			xmlw.writeCharacters("\n");
		}
		
		this.indent(3);
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");

		if (token.getNoSpaceAfter()) {
			this.indent(3);
			xmlw.writeEmptyElement(TAG_NS);
			xmlw.writeCharacters("\n");
		}
	}
	
	private void writeTag(Tag tag) throws XMLStreamException {
		this.indent(4);
		xmlw.writeStartElement(TAG_TAG);
		if (tag.getDisamb())
			xmlw.writeAttribute(TAG_DISAMB, "1");
		xmlw.writeStartElement(TAG_BASE);
		writeText(tag.getBase());
		xmlw.writeEndElement();
		xmlw.writeStartElement(TAG_CTAG);
		writeText(tag.getCtag());
		xmlw.writeEndElement();
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}

	private void writeText(String text) throws XMLStreamException {
		if (text.equals("\""))
			xmlw.writeEntityRef("quot");
		else if (text.equals("\'"))
			xmlw.writeEntityRef("apos");
		else if (text.equals("<"))
			xmlw.writeEntityRef("lt");
		else if (text.equals(">"))
			xmlw.writeEntityRef("gt");
		else 
			xmlw.writeCharacters(text);
	}
	
	private void indent(int repeat) throws XMLStreamException{
		if (this.indent)
			for (int i=0; i<repeat; i++)
				xmlw.writeCharacters(" ");
	}
}
