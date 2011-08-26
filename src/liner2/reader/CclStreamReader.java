package liner2.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
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

public class CclStreamReader extends StreamReader {
	
	private final String TAG_ANN		= "ann";
	private final String TAG_BASE 		= "base";
	private final String TAG_CHAN		= "chan";
	private final String TAG_CTAG		= "ctag";
	private final String TAG_ID 		= "id";
	private final String TAG_ORTH		= "orth";
	private final String TAG_NS			= "ns";
	private final String TAG_PARAGRAPH 	= "chunk";
	private final String TAG_SENTENCE	= "sentence";
	private final String TAG_TAG		= "lex";
	private final String TAG_TOKEN 		= "tok";
	
	private XMLStreamReader xmlr;
	
	public CclStreamReader(String filename){
		InputStream is = System.in;
		if ((filename != null) && (!filename.isEmpty())) {
			try {
				is = new FileInputStream(filename);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		XMLInputFactory xmlif = XMLInputFactory.newFactory();
		try {
			this.xmlr = xmlif.createXMLStreamReader(is);
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
			xmlr.close();
		}
		catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}	
		
	@Override
	public Paragraph readParagraph() {
		
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
			while (xmlr.hasNext()) {
				eventType = xmlr.next();
				if (outsideParagraph) {
					if (eventType != XMLStreamConstants.START_ELEMENT)
						continue;
					if (!xmlr.getName().getLocalPart().equals(TAG_PARAGRAPH)) 
						continue;
					paragraphId = xmlr.getAttributeValue(null, TAG_ID);
					outsideParagraph = false;
					paragraphText = "<" + xmlr.getName() + ">";
				}
				else {
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
							paragraphText += xmlr.getText();
						}
						continue;
					}
					if (!xmlr.getName().getLocalPart().equals(TAG_PARAGRAPH)) {
						paragraphText += "</" + xmlr.getName() + ">";
						continue;
					}
					paragraphText += "</" + xmlr.getName() + ">";
					outsideParagraph = true;
					break;
				}
			}
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
			return null;
		}
		
		// jeśli nie wczytano żadnego akapitu
		if (paragraphText.equals("")) {
			return null;
		}
		
		// przekonwertuj kod xml akapitu w obiekt DOM
		paragraphText = paragraphText.replace("&", "&amp;");
		Document paragraphDoc = paragraphToDOM(paragraphText);
		Paragraph paragraph = new Paragraph(paragraphId);
		
		// przetwórz obiekt DOM na struktury programu
		NodeList sentences = paragraphDoc.getElementsByTagName(TAG_SENTENCE);
		for (int i = 0; i < sentences.getLength(); i++) {
			paragraph.addSentence(getSentenceFromNode(sentences.item(i)));
		}
		
		return paragraph;
	}
	
	private Document paragraphToDOM(String paragraphText) {
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
      		// Error generated by the parser
			System.out.println("\n** Parsing error"
				+ ", line " + spe.getLineNumber()
				+ ", uri " + spe.getSystemId());
			System.out.println("  " + spe.getMessage() );
	  
			// Use the contained exception, if any
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		} catch (SAXException sxe) {
			// Error generated by this application
			// (or a parser-initialization error)
			Exception x = sxe;
			if (sxe.getException() != null)
			x = sxe.getException();
			x.printStackTrace();
		} catch (ParserConfigurationException pce) {
			// Parser with specified options cannot be built
			pce.printStackTrace();
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}
		
		return document;
	}
	
	private Sentence getSentenceFromNode(Node sentenceNode) {
		Sentence sentence = new Sentence();
		NodeList sentenceChildNodes = sentenceNode.getChildNodes();
		int idx = 0;
		Hashtable<String, Chunk> annotations = new Hashtable<String, Chunk>();
		for (int i = 0; i < sentenceChildNodes.getLength(); i++) {
			Node n = sentenceChildNodes.item(i);
			if ((n.getNodeType() == Node.ELEMENT_NODE) &&
				(n.getNodeName().equals(TAG_TOKEN))) {
				Token token = getTokenFromNode(idx, n, annotations, sentence);
				sentence.addToken(token);
				idx++;
			}
		}
		
		// process annotations
		for (Chunk chunk : annotations.values())
			sentence.addChunk(chunk);
			
		// initialize attributes index
		AttributeIndex attributeIndex = new AttributeIndex();
		attributeIndex.addAttribute("orth");
		attributeIndex.addAttribute("base");
		attributeIndex.addAttribute("ctag");
		sentence.setAttributeIndex(attributeIndex);
		
		return sentence;
	}
	
	private Token getTokenFromNode(int idx, Node tokenNode, Hashtable<String, Chunk> annotations, Sentence sentence) {
		Token token = new Token();
		NodeList tokenChildNodes = tokenNode.getChildNodes();
		for (int i = 0; i < tokenChildNodes.getLength(); i++) {
			Node n = tokenChildNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals(TAG_ORTH)) {
					token.setAttributeValue(0, getTextFromNode(n));
				}
				else if (n.getNodeName().equals(TAG_TAG))
					token.addTag(getTagFromNode(n));
				else if (n.getNodeName().equals(TAG_NS)) {
					token.setNoSpaceAfter(true);
				}
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
		return token;
	}
	
	private Tag getTagFromNode(Node tagNode) {
		String base = null;
		String ctag = null;
		boolean disamb = false;
		
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
				text += n.getNodeValue();
		}
		return text;
	}

}

