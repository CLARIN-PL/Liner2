package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


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
	private final String TAG_HEAD 			= "head";
	
	private final String TAG_RELATIONS 			= "relations";
	private final String TAG_RELATION 			= "rel";
	private final String TAG_FROM 			= "from";
	private final String TAG_TO 			= "to";
	private final String ATTR_SENT 			= "sent";
	private final String ATTR_CHAN 			= "chan";
	private final String ATTR_NAME 			= "name";
	private final String ATTR_SET 			= "set";

	private final String TAG_PROP			= "prop";
	private final String ATTR_KEY			= "key";

	private XMLStreamWriter xmlw;
	private XMLStreamWriter xmlRelw;
	private OutputStream os;
	private OutputStream osRel;
	private HashMap<Annotation, HashMap<String, Integer>> annotationSentChannelIdx;
	private XMLOutputFactory xmlof = null;
	private boolean indent = true;
    private final String[] requiredAttributes = new String[]{"orth", "base", "ctag"};
	
    public CclStreamWriter(OutputStream os) {
		this.os = os;
		this.xmlof = XMLOutputFactory.newFactory();
		annotationSentChannelIdx = new HashMap<Annotation, HashMap<String, Integer>>();
	}
	
    public CclStreamWriter(OutputStream os, OutputStream rel){
    	this.os = os;
    	this.osRel = rel;
    	this.xmlof = XMLOutputFactory.newFactory();
    	annotationSentChannelIdx = new HashMap<Annotation, HashMap<String, Integer>>();
    }
    
	private void openRelXml(){
		try {
			this.xmlRelw = this.xmlof.createXMLStreamWriter(osRel);
			xmlRelw.writeStartDocument("UTF-8", "1.0");
			xmlRelw.writeCharacters("\n");
			xmlRelw.writeStartElement(TAG_RELATIONS);
			xmlRelw.writeCharacters("\n");
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	private void closeRelXml(){
		try {
			xmlRelw.writeEndDocument();
			xmlRelw.close();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void openXml() {
		try {
			this.xmlw = this.xmlof.createXMLStreamWriter(os);
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
			if ( this.xmlw != null && !this.os.equals(System.out))
				this.xmlw.flush();
			if ( this.xmlRelw != null && !this.os.equals(System.out))
				this.xmlRelw.flush();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
            if(!(os instanceof PrintStream))
                os.close();
            if(osRel != null && !(osRel instanceof PrintStream))
                osRel.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void writeRelations(Document document) throws XMLStreamException{
		for(Relation relation : document.getRelations().getRelations())
			writeRelation(relation);
	}
	
	public void writeRelation(Relation relation) throws XMLStreamException{
		String fromSentenceId = relation.getAnnotationFrom().getSentence().getId();
		String fromChannel = relation.getAnnotationFrom().getType().toLowerCase();
				
		String toSentenceId = relation.getAnnotationTo().getSentence().getId();
		String toChannel = relation.getAnnotationTo().getType().toLowerCase();
		
		int fromAnnIdx = 0; 
		int toAnnIdx = 0;
		try{
			fromAnnIdx = annotationSentChannelIdx.get(relation.getAnnotationFrom()).get(fromChannel);
			toAnnIdx = annotationSentChannelIdx.get(relation.getAnnotationTo()).get(toChannel);
		}
		catch(Exception ex){
			System.out.println(relation);
			System.out.println(relation.getDocument().getName());
		}
		
		indentRel(2);
		xmlRelw.writeStartElement(TAG_RELATION);
		xmlRelw.writeAttribute(ATTR_NAME, relation.getType());
		xmlRelw.writeAttribute(ATTR_SET, relation.getSet());
		xmlRelw.writeCharacters("\n");
			indentRel(4);
			xmlRelw.writeStartElement(TAG_FROM);
				xmlRelw.writeAttribute(ATTR_SENT, fromSentenceId);
				xmlRelw.writeAttribute(ATTR_CHAN, relation.getAnnotationFrom().getType());
				xmlRelw.writeCharacters(fromAnnIdx+"");
			xmlRelw.writeEndElement();
			xmlRelw.writeCharacters("\n");
			indentRel(4);
			xmlRelw.writeStartElement(TAG_TO);
				xmlRelw.writeAttribute(ATTR_SENT, toSentenceId);
				xmlRelw.writeAttribute(ATTR_CHAN, relation.getAnnotationTo().getType());
				xmlRelw.writeCharacters(toAnnIdx+"");
			xmlRelw.writeEndElement();
			xmlRelw.writeCharacters("\n");
		indentRel(2);
		xmlRelw.writeEndElement();
		xmlRelw.writeCharacters("\n");
	}
	
	@Override
	public void writeDocument(Document document){
        if(!hasRequiredAttributes(document.getAttributeIndex())){
            try {
                throw new DataFormatException("Document attribute index does not contain features required by ccl format: "+Arrays.toString(requiredAttributes));
            } catch (DataFormatException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
		this.openXml();
		for (Paragraph paragraph : document.getParagraphs())
			this.writeParagraph(paragraph);
		this.closeXml();
		
		
		
		if(this.osRel != null){
			this.openRelXml();
			if(document.getRelations().getRelations().size() > 0){
				try {
					this.writeRelations(document);
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
			this.closeRelXml();
		}
	}

    private boolean hasRequiredAttributes(TokenAttributeIndex attrs){
        return attrs.allAtributes().containsAll(Arrays.asList(requiredAttributes));
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
		LinkedHashSet<Annotation> chunks = sentence.getChunks();
		ArrayList<String> channels = new ArrayList<String>();
		for (Annotation chunk : chunks) {
			if (!channels.contains(chunk.getType()))
				channels.add(chunk.getType());
		}
//		Collections.sort(channels);
		
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
		writeText(token.getOrth());
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
		boolean written = false;
		for (Tag tag : token.getTags()){
			if(tag.getDisamb()){
				writeTag(tag);
				written = true;
				break;
			}
		}
		if(!written) writeTag(token.getTags().get(0));
		
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
                int annIdx = 1;
                for(Annotation a: chunks)
                    if (a.getType().equals(ann.getType())){
                        if(a == ann){
                            break;
                        }
                        annIdx++;
                    }
                xmlw.writeAttribute(TAG_CHAN, ann.getType().toLowerCase());
				if (ann.hasHead() && ann.getHead() == idx)
					xmlw.writeAttribute(TAG_HEAD, "1");
				xmlw.writeCharacters("" + annIdx);
				HashMap<String, Integer> sentChannelMap;
				if(annotationSentChannelIdx.get(ann) != null){
					sentChannelMap = annotationSentChannelIdx.get(ann);
				}else{
					sentChannelMap = new HashMap<String, Integer>();
				}
				sentChannelMap.put(ann.getType().toLowerCase(), annIdx);
				annotationSentChannelIdx.put(ann, sentChannelMap);
				
			}
			else{
				xmlw.writeAttribute(TAG_CHAN, channels.get(chanIdx).toLowerCase());
				xmlw.writeCharacters("0");
			}
			xmlw.writeEndElement();
			xmlw.writeCharacters("\n");
		}

		for (Annotation a: chunks){
			if (a.getSentence().getTokens().get(a.getBegin()) == token){
				for (String key: a.getMetadata().keySet()) {
					this.indent(4);
					xmlw.writeStartElement(TAG_PROP);
					xmlw.writeAttribute(ATTR_KEY, a.getType() + ":" + key);
					xmlw.writeCharacters(a.getMetadata().get(key));
					xmlw.writeEndElement();
					xmlw.writeCharacters("\n");
				}
			}
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
	
	private void indentRel(int repeat) throws XMLStreamException{
		if (this.indent)
			for (int i=0; i<repeat; i++)
				xmlRelw.writeCharacters(" ");
	}
	
	private void indent(int repeat) throws XMLStreamException{
		if (this.indent)
			for (int i=0; i<repeat; i++)
				xmlw.writeCharacters(" ");
	}
}
