package liner2.reader;

import liner2.reader.parser.CclSaxParser;
import liner2.reader.parser.tei.AnnMorphosyntaxSAXParser;
import liner2.reader.parser.tei.AnnNamedSAXParser;
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

    public TEIStreamReader(InputStream annMorphosyntax, InputStream annNamed) throws DataFormatException {
        this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        AnnMorphosyntaxSAXParser morphoParser = new AnnMorphosyntaxSAXParser(annMorphosyntax, this.attributeIndex);
        AnnNamedSAXParser namedParser = new AnnNamedSAXParser(annNamed, morphoParser.getParagraphs(), morphoParser.getTokenIdsMap());
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
