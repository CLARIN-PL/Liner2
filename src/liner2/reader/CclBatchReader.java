package liner2.reader;

import liner2.reader.parser.CclSaxParser;
import liner2.structure.Paragraph;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/6/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */


public class CclBatchReader extends StreamReader {

    private TokenAttributeIndex attributeIndex;
    private int currIndex=0;
    private List<Paragraph> allParagraphs;
    private BufferedReader ir;

    public CclBatchReader(InputStream is) throws  DataFormatException{
        this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        allParagraphs = new ArrayList<Paragraph>();
        ir = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String line;
            try {
                line = ir.readLine();
            } catch (IOException ex) {
                throw new DataFormatException("I/O error.");
            }
            if(line == null)
                return;
            String cclFile = line.trim().split(";")[0];
            if (cclFile.length() == 0)
                return;

            InputStream  fileAsStream;
            try {
                fileAsStream = new FileInputStream(cclFile);
            } catch (IOException ex) {
                throw new DataFormatException("Unable to read input file: " + cclFile);
            }

            CclSaxParser parser_out = new CclSaxParser(fileAsStream, this.attributeIndex);
            allParagraphs.addAll(parser_out.getParagraphs());
        }


    }
    @Override
    protected TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    protected Paragraph readRawParagraph() throws DataFormatException {
        if (!paragraphReady())
            return null;
        return allParagraphs.get(currIndex++);
    }

    @Override
    public void close() throws DataFormatException {
    }

    @Override
    public boolean paragraphReady() throws DataFormatException {
        if (currIndex<allParagraphs.size())
            return true;
        else
            return false;
    }
}
