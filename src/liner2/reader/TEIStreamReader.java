package liner2.reader;

import liner2.reader.parser.CclSaxParser;
import liner2.reader.parser.tei.AnnMorphosyntaxSAXParser;
import liner2.reader.parser.tei.AnnNamedSAXParser;
import liner2.reader.parser.tei.AnnSegmentationSAXParser;
import liner2.structure.*;
import liner2.tools.DataFormatException;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 9:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TEIStreamReader extends  StreamReader{

    private TokenAttributeIndex attributeIndex;
    private int currIndex=0;
    private ArrayList<Paragraph> paragraphs;

    public TEIStreamReader(InputStream annMorphosyntax, InputStream annSegmentation, InputStream annNamed) throws DataFormatException {
        this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        this.attributeIndex.addAttribute("tagTool");
        AnnMorphosyntaxSAXParser morphoParser = new AnnMorphosyntaxSAXParser(annMorphosyntax, this.attributeIndex);
        AnnSegmentationSAXParser segmentationParser = new AnnSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs());
        AnnNamedSAXParser namedParser = new AnnNamedSAXParser(annNamed, segmentationParser.getParagraphs(), morphoParser.getTokenIdsMap());
        this.paragraphs = namedParser.getParagraphs();
    }

    @Override
    public TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    public void close() throws DataFormatException {

    }

    @Override
    public boolean paragraphReady() throws DataFormatException {
        if (currIndex<paragraphs.size())
            return true;
        else
            return false;
    }

    @Override
    protected Paragraph readRawParagraph() throws DataFormatException {
        if (!paragraphReady())
            return null;
        return paragraphs.get(currIndex++);
    }
}
