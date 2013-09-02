package liner2.writer;
import liner2.structure.*;
import liner2.tools.Pair;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class TEIStreamWriter extends StreamWriter{

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //ToDo: podzielić readParagraph na podmetody!!!!!

    private final String TAG_CORPUS      = "teiCorpus";
    private final String TAG_TEI         = "TEI";
    private final String TAG_TEXT        = "text";
    private final String TAG_FRONT       = "front";
    private final String TAG_TITLE       = "docTitle";
    private final String TAG_TITLEPART   = "titlePart";
    private final String TAG_BODY        = "body";
    private final String TAG_DIV         = "div";
    private final String TAG_PARAGRAPH   = "p";
    private final String TAG_SENTENCE    = "s";
    private final String TAG_SEGMENT    = "seg";
    private final String TAG_FEATURESET		= "fs";
    private final String TAG_FEATURE		= "f";
    private final String TAG_STRING	    	= "string";
    private final String TAG_SYMBOL	    	= "symbol";
    private final String TAG_VALT	    	= "vAlt";
    private final String TAG_POINTER	    	= "ptr";

    private XMLStreamWriter textWriter;
    private XMLStreamWriter annSegmentationWriter;
    private XMLStreamWriter annMorphosyntaxWriter;
    private XMLStreamWriter annNamedWriter;
    private OutputStream text;
    private OutputStream annSegmentation;
    private OutputStream annMorphosyntax;
    private OutputStream annNamed;
    private boolean open = false;
    private boolean indent = true;
    private String documentName;
    private int currentParagraphIdx;
    public TEIStreamWriter(OutputStream text, OutputStream annSegmentation, OutputStream annMorphosyntax, OutputStream annNamed, String documentName) {
        this.documentName = documentName;
        this.text = text;
        this.annSegmentation = annSegmentation;
        this.annMorphosyntax = annMorphosyntax;
        this.annNamed = annNamed;
        XMLOutputFactory xmlof = XMLOutputFactory.newFactory();
        try {
            this.textWriter = xmlof.createXMLStreamWriter(text);
            this.annSegmentationWriter = xmlof.createXMLStreamWriter(annSegmentation);
            this.annMorphosyntaxWriter = xmlof.createXMLStreamWriter(annMorphosyntax);
            this.annNamedWriter = xmlof.createXMLStreamWriter(annNamed);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }

    }

    public void open() {
        if (open)
            return;
        try {
            writeCommonOpening(textWriter);
            textWriter.writeCharacters("\n");
            this.indent(3, textWriter);
            textWriter.writeStartElement(TAG_FRONT);
            textWriter.writeCharacters("\n");
            this.indent(4, textWriter);
            textWriter.writeStartElement(TAG_TITLE);
            textWriter.writeCharacters("\n");
            this.indent(5, textWriter);
            textWriter.writeStartElement(TAG_TITLEPART);
            textWriter.writeAttribute("type", "title");
            textWriter.writeAttribute("xml:id", "titlePart-1");
            textWriter.writeCharacters(documentName);
            textWriter.writeEndElement();
            textWriter.writeCharacters("\n");
            writeEndElementLine(4, textWriter);
            writeEndElementLine(3, textWriter);
            this.indent(3, textWriter);
            textWriter.writeStartElement(TAG_BODY);
            textWriter.writeCharacters("\n");
            this.indent(4, textWriter);
            textWriter.writeStartElement(TAG_DIV);
            textWriter.writeAttribute("type", "article");
            textWriter.writeAttribute("xml:id", "div-1");
            textWriter.writeCharacters("\n");


            writeCommonOpening(annSegmentationWriter);
            annSegmentationWriter.writeAttribute("xml:lang", "pl");
            annSegmentationWriter.writeAttribute("xml:id", "segm_text");
            annSegmentationWriter.writeCharacters("\n");
            this.indent(3, annSegmentationWriter);
            annSegmentationWriter.writeStartElement(TAG_BODY);
            annSegmentationWriter.writeAttribute("xml:id", "segm_body");
            annSegmentationWriter.writeCharacters("\n");

            writeCommonOpening(annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeCharacters("\n");
            this.indent(3, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_BODY);
            annMorphosyntaxWriter.writeCharacters("\n");

            writeCommonOpening(annNamedWriter);
            annNamedWriter.writeAttribute("xml:lang", "pl");
            annNamedWriter.writeCharacters("\n");
            this.indent(3, annNamedWriter);
            annNamedWriter.writeStartElement(TAG_BODY);
            annNamedWriter.writeCharacters("\n");


        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        open = true;
    }

    public void writeCommonOpening(XMLStreamWriter xmlw) throws XMLStreamException{
        xmlw.writeStartDocument("UTF-8", "1.0");
        xmlw.writeCharacters("\n");
        xmlw.writeStartElement(TAG_CORPUS);
        xmlw.writeCharacters("\n");
        this.indent(1, xmlw);
        xmlw.writeStartElement(TAG_TEI);
        xmlw.writeCharacters("\n");
        this.indent(2, xmlw);
        xmlw.writeStartElement(TAG_TEXT);
    }

    private void writeParagraphStart(String paragraphId) throws XMLStreamException {
        this.indent(5, textWriter);
        textWriter.writeStartElement(TAG_PARAGRAPH);
        textWriter.writeAttribute("xml:id", paragraphId);
        textWriter.writeCharacters("\n");
        this.indent(6, textWriter);

        this.indent(4, annSegmentationWriter);
        annSegmentationWriter.writeStartElement(TAG_PARAGRAPH);
        annSegmentationWriter.writeAttribute("corresp", "text_structure.xml#" + paragraphId);
        annSegmentationWriter.writeAttribute("xml:id", "segm_" + paragraphId);
        annSegmentationWriter.writeCharacters("\n");

        this.indent(4, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_PARAGRAPH);
        annMorphosyntaxWriter.writeAttribute("xml:id", paragraphId);
        annMorphosyntaxWriter.writeCharacters("\n");

        this.indent(4, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_PARAGRAPH);
        annNamedWriter.writeAttribute("xml:id", paragraphId);
        annNamedWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + paragraphId);
        annNamedWriter.writeCharacters("\n");
    }

    @Override
    public void writeParagraph(Paragraph paragraph) {
        try{
            HashMap<String, String> currentIds = new HashMap<String, String>();
            if (!open){
                open();
            }
            currentParagraphIdx++;
            currentIds.put("paragraphId", "p-"+currentParagraphIdx);
            writeParagraphStart(currentIds.get("paragraphId"));

            int sentenceNr = 1;
            int tokenNr = 1;
            StringBuilder wholeParagraph = new StringBuilder();
            for (Sentence sent: paragraph.getSentences()){
                currentIds.put("sentenceId", currentIds.get("paragraphId")+ "." + (sentenceNr++) + "-s");
                writeSentence(sent, currentIds, tokenNr, wholeParagraph);
                tokenNr += sent.getTokenNumber();
            }
            writeEndElementLine(4, annSegmentationWriter);
            writeEndElementLine(4, annMorphosyntaxWriter);
            writeEndElementLine(4, annNamedWriter);

            textWriter.writeCharacters(wholeParagraph.toString().trim());
            textWriter.writeCharacters("\n");
            writeEndElementLine(5, textWriter);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSentence(Sentence sent, HashMap<String, String> currentIds, int currentTokenNr, StringBuilder wholeParagraph) throws XMLStreamException {
        this.indent(5, annSegmentationWriter);
        annSegmentationWriter.writeStartElement(TAG_SENTENCE);
        annSegmentationWriter.writeAttribute("xml:id", "segm_" + currentIds.get("sentenceId"));
        annSegmentationWriter.writeCharacters("\n");

        this.indent(5, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_SENTENCE);
        annMorphosyntaxWriter.writeAttribute("corresp", "ann_segmentation.xml#segm_" + currentIds.get("sentenceId"));
        annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
        annMorphosyntaxWriter.writeCharacters("\n");

        this.indent(5, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_SENTENCE);
        annNamedWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
        annNamedWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId"));
        annNamedWriter.writeCharacters("\n");

        ArrayList<Token> sentenceTokens = sent.getTokens();
        HashMap<Integer, String> tokenTEIIds = new HashMap<Integer, String>();

        for(int i=0; i < sent.getTokenNumber(); i++){
            currentIds.put("tokenId", currentIds.get("paragraphId") + "." + (currentTokenNr++) + "-seg");
            Token currentToken = sentenceTokens.get(i);
            tokenTEIIds.put(i, "segm_" + currentIds.get("tokenId"));
            writeToken(currentToken, currentIds, wholeParagraph, currentToken.getNoSpaceAfter());
        }

        int annotationNr = 1;
        for(Annotation ann: sent.getChunks()){
            writeAnnotation(ann, currentIds.get("sentenceId") + "_n" + (annotationNr++), tokenTEIIds);
        }

        writeEndElementLine(6, annSegmentationWriter);
        writeEndElementLine(5, annMorphosyntaxWriter);
        writeEndElementLine(5, annNamedWriter);
    }

    public void writeAnnotation(Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
        int annNamedIndent = 6;
        this.indent(annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_SEGMENT);
        annNamedWriter.writeAttribute("xml:id", annotationId);
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURESET);
        annNamedWriter.writeAttribute("type", "named");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURE);
        annNamedWriter.writeAttribute("name", "type");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeEmptyElement(TAG_SYMBOL);
        annNamedWriter.writeAttribute("value", ann.getType());
        annNamedWriter.writeCharacters("\n");
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        this.indent(annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURE);
        annNamedWriter.writeAttribute("name", "orth");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_STRING);
        annNamedWriter.writeCharacters(ann.getText());
        annNamedWriter.writeEndElement();
        annNamedWriter.writeCharacters("\n");
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        for(int tokenIdx: ann.getTokens()){
            this.indent(annNamedIndent, annNamedWriter);
            annNamedWriter.writeEmptyElement(TAG_POINTER);
            annNamedWriter.writeAttribute("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx));
            annNamedWriter.writeCharacters("\n");
        }
        writeEndElementLine(--annNamedIndent, annNamedWriter);
    }

    public void writeToken(Token tok, HashMap<String, String> currentIds, StringBuilder wholeParagraph, boolean noSpace) throws XMLStreamException {
        int tokenStart = wholeParagraph.length();
        String orth = tok.getFirstValue();

        this.indent(6, annSegmentationWriter);
        annSegmentationWriter.writeComment(" "+orth+" ");
        annSegmentationWriter.writeCharacters("\n");
        this.indent(6, annSegmentationWriter);
        annSegmentationWriter.writeEmptyElement(TAG_SEGMENT);
        annSegmentationWriter.writeAttribute("corresp", String.format("text_structure.xml#string-range(%s,%d,%d)", currentIds.get("paragraphId"), tokenStart, orth.length()));
        annSegmentationWriter.writeAttribute("xml:id","segm_" + currentIds.get("tokenId"));
        annSegmentationWriter.writeCharacters("\n");

        String morphId = "morph_" + currentIds.get("tokenId");
        currentIds.put("morphId",morphId );
        this.indent(6, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_SEGMENT);
        annMorphosyntaxWriter.writeAttribute("corresp", "ann_segmentation.xml#segm_" + currentIds.get("tokenId"));
        annMorphosyntaxWriter.writeAttribute("xml:id", morphId);
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(7, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        annMorphosyntaxWriter.writeAttribute("type", "morph");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "orth");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(9, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(orth);
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(8, annMorphosyntaxWriter);
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeComment(String.format("%s [%s,%s]",orth,tokenStart,orth.length()));

        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name","interps");
        annMorphosyntaxWriter.writeCharacters("\n");

        Interps interps = new Interps(tok.getTags());
        int annMorphoSyntaxIndent = 9;

        if (interps.lexemes.size() > 1){
            this.indent(annMorphoSyntaxIndent++, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_VALT);
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        int lexId = 0;
        for(TEILex lex: interps.lexemes){
            currentIds.put("lexId", morphId + "_" + (lexId++) + "-lex");
            writeLexeme(lex, currentIds, annMorphoSyntaxIndent);
        }

        if (interps.lexemes.size() > 1){
            writeEndElementLine(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
        }

        writeEndElementLine(8, annMorphosyntaxWriter);

        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "disamb");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(9, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        annMorphosyntaxWriter.writeAttribute("feats", "ToDo");
        annMorphosyntaxWriter.writeAttribute("type", "tool_report");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(10, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeEmptyElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("fVal", morphId + "_" + interps.disambIdx + "-msd");
        annMorphosyntaxWriter.writeAttribute("name", "choice");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(10, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "interpretation");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(11, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(interps.disamb);
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        for(int i=10; i > 5; i--){
            writeEndElementLine(i, annMorphosyntaxWriter);
        }

        wholeParagraph.append(orth);
        if (!noSpace){
            wholeParagraph.append(" ");
        }
    }

    public void writeLexeme(TEILex lex, HashMap<String, String> currentIds, int currentIndent) throws XMLStreamException {
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        annMorphosyntaxWriter.writeAttribute("type", "lex");
        annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("lexId"));
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "base");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(lex.base);
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "ctag");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
        annMorphosyntaxWriter.writeAttribute("value", lex.ctag);
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "msd");
        annMorphosyntaxWriter.writeCharacters("\n");
        if (lex.msdSize() > 1){
            this.indent(++currentIndent, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_VALT);
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        currentIndent++;
        for(Pair<String, Integer> entry: lex.msdList){
            String msd = entry.getFirst();
            int msdIdx = entry.getSecond();
            this.indent(currentIndent, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
            annMorphosyntaxWriter.writeAttribute("value", msd);
            annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("morphId") + "_" + msdIdx + "-msd");
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        if (lex.msdSize() > 1){
            writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        }

        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
    }

    private void writeEndElementLine(int indent, XMLStreamWriter xmlw) throws XMLStreamException {
        this.indent(indent, xmlw);
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");
    }


    @Override
    public void close() {
        try {
            writeClosing(textWriter, 4);
            writeClosing(annSegmentationWriter, 3);
            writeClosing(annMorphosyntaxWriter, 3);
            writeClosing(annNamedWriter, 3);

            textWriter.close();
            annSegmentationWriter.close();
            annMorphosyntaxWriter.close();
            annNamedWriter.close();

            text.close();
            annMorphosyntax.close();
            annNamed.close();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeClosing(XMLStreamWriter xmlw, int indent){
        try {
            while(indent > 0){
                this.indent(indent--, xmlw);
                xmlw.writeEndElement();
                xmlw.writeCharacters("\n");

            }
            xmlw.writeEndDocument();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void indent(int repeat, XMLStreamWriter xmlw) throws XMLStreamException{
        if (this.indent)
            for (int i=0; i<repeat; i++)
                xmlw.writeCharacters(" ");
    }



    private class Interps{

        ArrayList<TEILex> lexemes;
        String disamb;
        int disambIdx;

        public Interps(ArrayList<Tag> tags){
            tagsToTEI(tags);
        }

        public void tagsToTEI(ArrayList<Tag> tags){
            lexemes = new ArrayList<TEILex>();
            int msdIdx = 0;
            for (Tag tag: tags){
                String base = tag.getBase();
                String[] ctag = tag.getCtag().split(":");
                String TEIctag = ctag[0];
                String msd;
                if (ctag.length > 1){
                    msd =  tag.getCtag().substring(TEIctag.length()+1);
                }
                else{
                    msd = "";
                }

                if (disamb == null && tag.getDisamb()){
                    disambIdx = msdIdx;
                    disamb = base+":"+TEIctag+":"+msd;
                }

                boolean foundMatch = false;
                for(TEILex lex: lexemes){
                    if (lex.match(base, TEIctag)){
                        foundMatch = true;
                        lex.addMsd(msd, msdIdx++);
                        break;
                    }
                }
                if (!foundMatch){
                    TEILex newLex = new TEILex(base, TEIctag);
                    newLex.addMsd(msd, msdIdx++);
                    lexemes.add(newLex);
                }
            }

            if (disamb == null){
                TEILex firstLex = lexemes.get(0);
                disamb = firstLex.base+":"+firstLex.ctag+":"+firstLex.msdList.get(0).getFirst();
            }
        }
    }

    private class TEILex{

        String base;
        String ctag;
        ArrayList<Pair<String, Integer>> msdList;
        boolean disamb;
        int disambMsdIdx;

        public TEILex(String base, String ctag){
            this.base = base;
            this.ctag = ctag;
            msdList = new ArrayList<Pair<String, Integer>>();
            disamb = false;
        }

        public boolean isDisamb(){
            return disamb;
        }

        public void setDisambTrue(int msdIdx){
            if(!disamb){
                disamb = true;
                disambMsdIdx = msdIdx;
            }
        }


        public void addMsd(String msd, int idx){
            msdList.add(new Pair<String, Integer>(msd, idx));
        }

        public boolean match(String base, String ctag){
            return this.base.equals(base) && this.ctag.equals(ctag);
        }

        public int msdSize() {
            return msdList.size();
        }
    }
}
