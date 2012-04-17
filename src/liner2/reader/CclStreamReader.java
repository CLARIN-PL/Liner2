package liner2.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException; 

import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException; 
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

import liner2.tools.DataFormatException;

public class CclStreamReader extends StreamReader {
	
	private final String TAG_ANN			= "ann";
	private final String TAG_BASE 			= "base";
	private final String TAG_CHAN			= "chan";
	private final String TAG_CTAG			= "ctag";
	private final String TAG_DISAMB			= "disamb";
	private final String TAG_ID 			= "id";
	private final String TAG_ORTH			= "orth";
	private final String TAG_NS				= "ns";
	private final String TAG_PARAGRAPH 		= "chunk";
	private final String TAG_PARAGRAPH_SET	= "chunkSet";
	private final String TAG_SENTENCE		= "sentence";
	private final String TAG_TAG			= "lex";
	private final String TAG_TOKEN 			= "tok";
	
	private XMLStreamReader xmlr;
	private BufferedInputStream is;
	private AttributeIndex attributeIndex;
	private String nextParagraphId = null;
	private boolean nextParagraph = false;
	
	public CclStreamReader(InputStream is) throws DataFormatException {
		this.is = new BufferedInputStream(is);
		XMLInputFactory xmlif = XMLInputFactory.newFactory();
		try {
			this.xmlr = xmlif.createXMLStreamReader(is);
		} catch (XMLStreamException ex) {
			throw new DataFormatException("Failed to create XML stream reader.");
		} 
		this.attributeIndex = new AttributeIndex();
		this.attributeIndex.addAttribute("orth");
		this.attributeIndex.addAttribute("base");
		this.attributeIndex.addAttribute("ctag");
	}
	
	@Override
	public AttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	@Override
	public void close() throws DataFormatException {
		try {
			xmlr.close();
			is.close();
		} catch (XMLStreamException ex) {
			throw new DataFormatException("Failed to close XML stream reader.");
		} catch (IOException ex) {
			throw new DataFormatException("Failed to close input stream.");
		}
	}	
		
	@Override
	public boolean paragraphReady() throws DataFormatException {
		if (this.nextParagraph)
			return true;
		try {
			int eventType = xmlr.getEventType();
			while (xmlr.hasNext()) {
				eventType = xmlr.next();
				if (eventType != XMLStreamConstants.START_ELEMENT)
					continue;
				if (!xmlr.getName().getLocalPart().equals(TAG_PARAGRAPH)) 
					continue;
				this.nextParagraph = true;
				this.nextParagraphId = xmlr.getAttributeValue(null, TAG_ID);
				return true;
			}
			return false;
		} catch (XMLStreamException ex) {
			throw new DataFormatException("Could not read paragraph header.");
		}
	}
	
	@Override
	protected Paragraph readRawParagraph() throws DataFormatException {
		
		/*
		 * Przy wczytywaniu z CCL-a do indeksu nazw trafią 3 pola:
		 * orth, base i ctag dla analizy oznaczonej jako disamb.
		 * Jeżeli brak, to bierzemy pierwszy z brzegu.
		 * Poza tym, wszystkie analizy morfologiczne mają być dodane
		 * do atrybutu tags.
		 */
		
		int eventType = xmlr.getEventType();
		boolean outsideParagraph = true;
		String paragraphText = "";
		String paragraphId = null;
		
		// wczytaj kod xml następnego akapitu
		try {
			if (!paragraphReady())
				return null;

			paragraphText = "<" + TAG_PARAGRAPH + ">";
			paragraphId = this.nextParagraphId;
			this.nextParagraph = false;
			this.nextParagraphId = null;
			
			while (xmlr.hasNext()) {
				eventType = xmlr.next();
				if (eventType != XMLStreamConstants.END_ELEMENT) {
					if (eventType == XMLStreamConstants.START_ELEMENT) {
						paragraphText += "<" + xmlr.getName();
						for (int i = 0; i < xmlr.getAttributeCount(); i++) {
							paragraphText += " " + xmlr.getAttributeName(i);
							paragraphText += "=\"" + xmlr.getAttributeValue(i) + "\"";
						}
						paragraphText += ">";
					}
					else if ((eventType == XMLStreamConstants.CHARACTERS) || 
						(eventType == XMLStreamConstants.SPACE)) {
						paragraphText += escapeXml(xmlr.getText());
						//paragraphText += xmlr.getText();
					}
					continue;
				}
				if (!xmlr.getName().getLocalPart().equals(TAG_PARAGRAPH)) {
					paragraphText += "</" + xmlr.getName() + ">";
					continue;
				}
				paragraphText += "</" + xmlr.getName() + ">";
				break;
			}
		} catch (XMLStreamException ex) {
			throw new DataFormatException("Could not read paragraph.");
		}
		
		// jeśli nie wczytano żadnego akapitu
		if (paragraphText.equals(""))
			return null;
		
		
		// przekonwertuj kod xml akapitu w obiekt DOM
		//paragraphText = paragraphText.replace("&", "&amp;");
		Document paragraphDoc = paragraphToDOM(paragraphText);
		Paragraph paragraph = new Paragraph(paragraphId);
		
		// przetwórz obiekt DOM na struktury programu
		NodeList sentences = paragraphDoc.getElementsByTagName(TAG_SENTENCE);
		for (int i = 0; i < sentences.getLength(); i++) {
			paragraph.addSentence(getSentenceFromNode(sentences.item(i)));
		}
		
		paragraph.setAttributeIndex(this.attributeIndex);
		return paragraph;
	}
	
	private Document paragraphToDOM(String paragraphText) throws DataFormatException {
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			// disable validation
			factory.setValidating(false);
			factory.setExpandEntityReferences(false);
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new ByteArrayInputStream(	
				paragraphText.getBytes()));
		} catch (SAXParseException spe) {
			throw new DataFormatException("Parse error (SAXParseException).");
		} catch (SAXException sxe) {
			throw new DataFormatException("Parse error (SAXException).");
		} catch (ParserConfigurationException pce) {
			throw new DataFormatException("Parse error (ParserConfigurationException).");
		} catch (IOException ioe) {
			throw new DataFormatException("I/O error by parsing.");
		}
		
		return document;
	}
	
	private Sentence getSentenceFromNode(Node sentenceNode) {
		Sentence sentence = new Sentence();
		for (int j=0; j<sentenceNode.getAttributes().getLength(); j++){
			Node n = sentenceNode.getAttributes().item(j);
			if ( n.getNodeName().equals("id"))
				sentence.setId(n.getNodeValue());
		}
			
		NodeList sentenceChildNodes = sentenceNode.getChildNodes();
		int idx = 0;
		Hashtable<String, Chunk> annotations = new Hashtable<String, Chunk>();
		Token currentToken = null;

		for (int i = 0; i < sentenceChildNodes.getLength(); i++) {
			Node n = sentenceChildNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals(TAG_TOKEN)) {
					currentToken = getTokenFromNode(idx, n, annotations, sentence);
					sentence.addToken(currentToken);
					idx++;
				}
				else if (n.getNodeName().equals(TAG_NS))
					if (currentToken != null)
						currentToken.setNoSpaceAfter(true);
			}
		}
		
		// process annotations
		for (Chunk chunk : annotations.values())
			sentence.addChunk(chunk);
		
		return sentence;
	}
	
	private Token getTokenFromNode(int idx, Node tokenNode, Hashtable<String, Chunk> annotations, Sentence sentence) {
		Token token = new Token();
		for (int j=0; j<tokenNode.getAttributes().getLength(); j++){
			Node n = tokenNode.getAttributes().item(j);
			if ( n.getNodeName().equals("id"))
				token.setId(n.getNodeValue());
		}
		
		NodeList tokenChildNodes = tokenNode.getChildNodes();
		for (int i = 0; i < tokenChildNodes.getLength(); i++) {
			Node n = tokenChildNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals(TAG_ORTH)) {
					token.setAttributeValue(0, getTextFromNode(n));
				}
				else if (n.getNodeName().equals(TAG_TAG))
					token.addTag(getTagFromNode(n));
				else if (n.getNodeName().equals(TAG_ANN)) {
					String ann = getAnnotationFromNode(n);
					if (ann != null) {
						if (annotations.containsKey(ann))
							annotations.get(ann).setEnd(idx);
						else {
							String type = ann.substring(0, ann.lastIndexOf('#')).toUpperCase();
							annotations.put(ann, new Chunk(idx, idx, type, sentence));
						}
					}
				}
			}
		}
		ArrayList<Tag> tags = token.getTags();
		for (Tag tag : tags) {
			if (tag.getDisamb()) {
				token.setAttributeValue(1, tag.getBase());
				token.setAttributeValue(2, tag.getCtag());
				return token;
			}
		}
		// if disamb not found
		token.setAttributeValue(1, tags.get(0).getBase());
		token.setAttributeValue(2, tags.get(0).getCtag());
		return token;
	}
	
	private Tag getTagFromNode(Node tagNode) {
		String base = null;
		String ctag = null;
		boolean disamb = false;
		
		// retrieve disamb
		NamedNodeMap attributes = tagNode.getAttributes();
		if (attributes != null) {
			Node disambNode = attributes.getNamedItem(TAG_DISAMB);
			if ((disambNode != null) && (disambNode.getNodeValue().equals("1")))
				disamb = true;
		}
		
		NodeList tagChildNodes = tagNode.getChildNodes();
		for (int i = 0; i < tagChildNodes.getLength(); i++) {
			Node n = tagChildNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals(TAG_BASE))
					base = getTextFromNode(n);
				else if (n.getNodeName().equals(TAG_CTAG))
					ctag = getTextFromNode(n);
			}
		}
		return new Tag(base, ctag, disamb);
	}
	
	private String getAnnotationFromNode(Node annNode) {
		NamedNodeMap attributes = annNode.getAttributes();
		String chanName = attributes.getNamedItem(TAG_CHAN).getNodeValue();
		String chanNumber = getTextFromNode(annNode).trim();
		if (chanNumber.equals("0"))
			return null;
		else
			return chanName + "#" + chanNumber;
	}
	
	private String getTextFromNode(Node textNode) {
		String text = "";
		NodeList textChildNodes = textNode.getChildNodes();
		for (int i = 0; i < textChildNodes.getLength(); i++) {
			Node n = textChildNodes.item(i);
			if (n.getNodeType() == Node.TEXT_NODE)
				text += unescapeXml(n.getNodeValue());
		}
		return text;
	}
	
	private String escapeXml(String text) {
		text = text.replace("&", "&amp;");
		text = text.replace("\"", "&quot;");
		text = text.replace("\'", "&apos;");
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	}

	private String unescapeXml(String text) {
		text = text.replace("&quot;", "\"");
		text = text.replace("&apos;", "\'");
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&amp;", "&");
		return text;
	}
}

