package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Relation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnRelationsSAXParser extends DefaultHandler {
		
	    private final String TAG_SEGMENT = "seg";
	    private final String TAG_POINTER = "ptr";

	    String relationType;
	    String sourceRef = null;
	    String targetRef = null;
	    Map<String, Annotation> annotationsMap;

	    List<Relation> relations = Lists.newArrayList();

	    final private Logger logger = LoggerFactory.getLogger(getClass());

	    public AnnRelationsSAXParser(InputStream is, Map<String, Annotation> annotationsMap) throws DataFormatException {
	        this.annotationsMap = annotationsMap;
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try {
				SAXParser parser = factory.newSAXParser();
				parser.parse(is,this);
			} catch (ParserConfigurationException e) {
				throw new DataFormatException("Parse error (ParserConfigurationException)");
			} catch (SAXException e) {
				throw new DataFormatException("Parse error (SAXException)");
			} catch (IOException e) {
				throw new DataFormatException("Parse error (IOException)");
			}
	    }

	    @Override
	    public InputSource resolveEntity (String publicId, String systemId){
	        return new InputSource(new StringReader(""));
	    }

	    @Override
	    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
	    	switch (elementName.toLowerCase()){
				case TAG_SEGMENT:
					this.relationType = attributes.getValue("type");
					break;

				case TAG_POINTER:
					switch(attributes.getValue("type")){
						case "source":
							sourceRef = attributes.getValue("target");
							break;
						case "target":
							targetRef = attributes.getValue("target");
							break;
					}
					break;
			}
	    }

	    @Override
	    public void endElement(String s, String s1, String element) throws SAXException {
	        if (element.equals(TAG_SEGMENT)) {
	        	Annotation sourceAnn = annotationsMap.get(sourceRef);
	        	Annotation targetAnn = annotationsMap.get(targetRef);
	        	if ( sourceAnn == null ){
	        		logger.error("Relation was skipped because source annotation was not found for the id {}", sourceRef);
				} else if ( targetAnn == null ){
					logger.error("Relation was skipped because target annotation was not found for the id {}", targetRef);
				} else {
		        	Relation r = new Relation(sourceAnn, targetAnn, relationType);
		        	relations.add(r);
	        	}
	        	relationType = null;
	        	sourceRef = null;
	        	targetRef = null;
	        }

	    }

	    public List<Relation> getRelations(){
	    	return this.relations;
	    }
}
