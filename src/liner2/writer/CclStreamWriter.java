package liner2.writer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import liner2.structure.Annotation;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

public class CclStreamWriter extends StreamWriter {

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
	private boolean open = false;
	private boolean indent = true;
	
	public CclStreamWriter(OutputStream os) {
		this.os = os;
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
			xmlw.writeStartDocument("UTF-8", "1.0");
			xmlw.writeCharacters("\n");
			xmlw.writeDTD("<!DOCTYPE chunkList SYSTEM \"ccl.dtd\">");
			xmlw.writeCharacters("\n");
			xmlw.writeStartElement(TAG_PARAGRAPH_SET);
			xmlw.writeCharacters("\n");
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
		open = true;
	}
	
	@Override
	public void close() {
		try {
			xmlw.writeEndDocument();
			xmlw.close();
            if(!(os instanceof PrintStream))
                os.close();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writeParagraph(Paragraph paragraph) {
		try {
			if (!open)
				open();
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
			xmlw.flush();
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
		//xmlw.writeCharacters(token.getFirstValue().replace("&", "&amp;"));
		//xmlw.writeCharacters(escapeXml(token.getFirstValue()));
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
		//xmlw.writeCharacters(tag.getBase().replace("&", "&amp;"));
		//xmlw.writeCharacters(escapeXml(tag.getBase()));
		writeText(tag.getBase());
		xmlw.writeEndElement();
		xmlw.writeStartElement(TAG_CTAG);
		//xmlw.writeCharacters(tag.getCtag());
		writeText(tag.getCtag());
		xmlw.writeEndElement();
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
	}

	private String escapeXml(String text) {
		//text = text.replace("&", "&amp;");
		text = text.replace("\"", "&quot;");
		text = text.replace("\'", "&apos;");
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
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
