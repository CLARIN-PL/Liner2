package liner2.reader;

import java.io.IOException;
import java.io.InputStream;

import liner2.reader.parser.tei.AnnMorphosyntaxSAXParser;
import liner2.reader.parser.tei.AnnNamedSAXParser;
import liner2.reader.parser.tei.AnnSegmentationSAXParser;
import liner2.structure.Document;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 9:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TEIStreamReader extends  AbstractDocumentReader{

    private TokenAttributeIndex attributeIndex;
    private int currIndex=0;
    private Document document;

    public TEIStreamReader(InputStream annMorphosyntax, InputStream annSegmentation, InputStream annNamed) throws DataFormatException {
        
    	this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        this.attributeIndex.addAttribute("tagTool");
        
        AnnMorphosyntaxSAXParser morphoParser = new AnnMorphosyntaxSAXParser(annMorphosyntax, this.attributeIndex);
        AnnSegmentationSAXParser segmentationParser = new AnnSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs());
        AnnNamedSAXParser namedParser = new AnnNamedSAXParser(annNamed, segmentationParser.getParagraphs(), morphoParser.getTokenIdsMap());
        this.document = new Document("unknown", namedParser.getParagraphs(), this.attributeIndex);
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
		return this.document;
	}

}
