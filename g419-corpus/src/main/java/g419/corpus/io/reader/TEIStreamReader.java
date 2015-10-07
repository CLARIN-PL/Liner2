package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.tei.AnnGroupsSAXParser;
import g419.corpus.io.reader.parser.tei.AnnMentionsSAXParser;
import g419.corpus.io.reader.parser.tei.AnnMorphosyntaxSAXParser;
import g419.corpus.io.reader.parser.tei.AnnNamedSAXParser;
import g419.corpus.io.reader.parser.tei.AnnRelationsSAXParser;
import g419.corpus.io.reader.parser.tei.AnnSegmentationSAXParser;
import g419.corpus.io.reader.parser.tei.AnnWordsSAXParser;
import g419.corpus.structure.Document;
import g419.corpus.structure.RelationSet;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.IOException;
import java.io.InputStream;


/**
 * SAX reader for a document in TEI (NKJP) format.
 */
public class TEIStreamReader extends  AbstractDocumentReader{

    private TokenAttributeIndex attributeIndex;
    private Document document;
    
    public TEIStreamReader(
    		InputStream annMorphosyntax, 
    		InputStream annSegmentation, 
    		InputStream annNamed, 
    		InputStream annMentions, 
    		InputStream annCoreference,
    		InputStream annWords,
    		InputStream annGroups,
    		InputStream annRelations,
    		String docName) throws DataFormatException {
        
    	this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        // TODO dodanie tego atrybutu "psuje" kolejność atrybutów
        //this.attributeIndex.addAttribute("tagTool");

        RelationSet relationSet = new RelationSet();
        
        // ToDo: Sprawdzenie, czy poszczególne inputstream nie są nullem
        AnnMorphosyntaxSAXParser morphoParser = new AnnMorphosyntaxSAXParser(docName, annMorphosyntax, this.attributeIndex);
        AnnSegmentationSAXParser segmentationParser = new AnnSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs());
        AnnWordsSAXParser wordsParser = null;
        AnnMentionsSAXParser mentionParser = null;

        /* Read words from the ann_words.xml file */
        if ( annWords != null ){
        	wordsParser = new AnnWordsSAXParser(docName, 
        		annWords,
        		segmentationParser.getParagraphs(), 
    			morphoParser.getTokenIdsMap());
        }

        /* Read names from the ann_names.xml file */
        if ( annNamed != null ){
        	new AnnNamedSAXParser(
        			annNamed, 
        			segmentationParser.getParagraphs(), 
        			morphoParser.getTokenIdsMap());
        }
        
        /* Read groups from the ann_groups.xml file */
        if ( annGroups != null && wordsParser != null ){
        	new AnnGroupsSAXParser(
        			annGroups, 
        			segmentationParser.getParagraphs(), 
        			morphoParser.getTokenIdsMap(), 
        			wordsParser.getWordsIdsMap());
        }
        
        if ( annMentions != null ){
        	mentionParser = new AnnMentionsSAXParser(
        			annMentions, 
        			segmentationParser.getParagraphs(), 
        			morphoParser.getTokenIdsMap());
        }
        
        if ( annCoreference != null ){
        	//AnnCoreferenceSAXParser coreferenceParser = new AnnCoreferenceSAXParser(annCoreference, mentionsParser.getParagraphs(), mentionsParser.getMentions());
        }

        if ( annRelations != null ){
        	AnnRelationsSAXParser relationParser = new AnnRelationsSAXParser(annRelations, mentionParser.getMentions());
        	relationSet.getRelations().addAll(relationParser.getRelations());
        }

        this.document = new Document(docName, segmentationParser.getParagraphs(), this.attributeIndex, relationSet);       
    }

    @Override
    public TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    public void close() throws DataFormatException {

    }

	@Override
	public Document nextDocument() throws DataFormatException, IOException {
		Document doc = this.document;
		this.document = null;
		return doc;
	}

}
