package liner2.writer;

import javafx.util.Pair;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
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
    private final String TAG_VALT	    	= "ptr";

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
            this.indent(4, textWriter);
            textWriter.writeEndElement();
            this.indent(3, textWriter);
            textWriter.writeEndElement();
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

    @Override
    public void writeParagraph(Paragraph paragraph) {
        try{
            if (!open){
                open();
            }
            currentParagraphIdx++;
            String paragraphId = "p-"+currentParagraphIdx;
            String segmentId = "segm_" + paragraphId;

            this.indent(5, textWriter);
            textWriter.writeStartElement(TAG_PARAGRAPH);
            textWriter.writeAttribute("xml:id", paragraphId);
            textWriter.writeCharacters("\n");
            this.indent(6, textWriter);

            this.indent(4, annSegmentationWriter);
            annSegmentationWriter.writeStartElement(TAG_PARAGRAPH);
            annSegmentationWriter.writeAttribute("corresp", "text_structure.xml#" + paragraphId);
            annSegmentationWriter.writeAttribute("xml:id", segmentId);
            annSegmentationWriter.writeCharacters("\n");

            this.indent(4, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_PARAGRAPH);
            annMorphosyntaxWriter.writeAttribute("xml:id", paragraphId);

            boolean noSpace = false;
            int sentenceNr = 1;
            int tokenNr = 1;
            ArrayList<String> tokenXmlIds = new ArrayList<String>();
            StringBuilder wholeParagraph = new StringBuilder();
            for (Sentence sent: paragraph.getSentences()){
                this.indent(5, annSegmentationWriter);
                annSegmentationWriter.writeStartElement(TAG_SENTENCE);
                annSegmentationWriter.writeAttribute("xml:id", segmentId+"."+sentenceNr+"-s");
                annSegmentationWriter.writeCharacters("\n");

                this.indent(5, annMorphosyntaxWriter);
                annMorphosyntaxWriter.writeStartElement(TAG_SENTENCE);
                annMorphosyntaxWriter.writeAttribute("corresp", "ann_segmentation.xml#" + segmentId + "." + sentenceNr + "-s");
                annMorphosyntaxWriter.writeAttribute("xml:id",paragraphId+"."+sentenceNr+"-s");
                annMorphosyntaxWriter.writeCharacters("\n");

                for(Token tok: sent.getTokens()){
                    int currentTokenStart = wholeParagraph.length();
                    String orth = tok.getFirstValue();

                    this.indent(6, annSegmentationWriter);
                    annSegmentationWriter.writeComment(orth);
                    annSegmentationWriter.writeCharacters("\n");
                    this.indent(6, annSegmentationWriter);
                    annSegmentationWriter.writeEmptyElement(TAG_SEGMENT);
                    annSegmentationWriter.writeAttribute("corresp",String.format("text_structure.xml#string-range(%s,%s,%s)",paragraphId,currentTokenStart,orth.length()));
                    annSegmentationWriter.writeAttribute("xml:id",segmentId+"."+tokenNr+"-seg");
                    annSegmentationWriter.writeCharacters("\n");

                    String morphSegId = "morph_" + paragraphId + "." + tokenNr + "-seg";
                    int annMorphoSyntaxIndent = 6;
                    this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_SEGMENT);
                    annMorphosyntaxWriter.writeAttribute("corresp", segmentId + "." + tokenNr + "-seg");
                    annMorphosyntaxWriter.writeAttribute("xml:id", morphSegId);
                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
                    annMorphosyntaxWriter.writeAttribute("type", "morph");
                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
                    annMorphosyntaxWriter.writeAttribute("name", "orth");
                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_STRING);
                    annMorphosyntaxWriter.writeCharacters(orth);
                    annMorphosyntaxWriter.writeEndElement();
                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeEndElement();
                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeComment(String.format("%s [%s,%s]",orth,currentTokenStart,orth.length()));

                    annMorphosyntaxWriter.writeCharacters("\n");
                    this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
                    annMorphosyntaxWriter.writeAttribute("name","interps");
                    annMorphosyntaxWriter.writeCharacters("\n");

                    Interps interps = new Interps(tok.getTags());
                    if (interps.lexemes.size() > 1){
                        this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeStartElement(TAG_VALT);
                        annMorphosyntaxWriter.writeCharacters("\n");
                    }
                    annMorphoSyntaxIndent++;
                    int lexId = 0;

                    for(TEILex lex: interps.lexemes){
                        this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEmptyElement(TAG_FEATURESET);
                        annMorphosyntaxWriter.writeAttribute("type", "lex");
                        annMorphosyntaxWriter.writeAttribute("xml:id", morphSegId+"_"+(lexId++)+"-lex");
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEmptyElement(TAG_FEATURE);
                        annMorphosyntaxWriter.writeAttribute("name", "base");
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
                        annMorphosyntaxWriter.writeCharacters(lex.base);
                        annMorphosyntaxWriter.writeEndElement();
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEndElement();
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEmptyElement(TAG_FEATURE);
                        annMorphosyntaxWriter.writeAttribute("name", "ctag");
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
                        annMorphosyntaxWriter.writeAttribute("value", lex.ctag);
                        annMorphosyntaxWriter.writeCharacters("\n");
                        this.indent(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        if (lex.msdSize() > 1){
                            this.indent(++annMorphoSyntaxIndent, annMorphosyntaxWriter);
                            annMorphosyntaxWriter.writeStartElement(TAG_VALT);
                            annMorphosyntaxWriter.writeCharacters("\n");
                        }
                        annMorphoSyntaxIndent++;
                        for(Pair<String, Integer> entry: lex.msdList){
                            String msd = entry.getKey();
                            int msdIdx = entry.getValue();
                            this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                            annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
                            annMorphosyntaxWriter.writeAttribute("value", msd);
                            annMorphosyntaxWriter.writeAttribute("xml:id", morphSegId+"_"+msdIdx+"-msd");
                            annMorphosyntaxWriter.writeCharacters("\n");
                        }
                        annMorphoSyntaxIndent--;


                        if (lex.msdSize() > 1){
                            this.indent(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
                            annMorphosyntaxWriter.writeEndElement();
                            annMorphosyntaxWriter.writeCharacters("\n");
                        }
                    }
                    annMorphoSyntaxIndent--;

                    if (interps.lexemes.size() > 1){
                        this.indent(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
                        annMorphosyntaxWriter.writeEndElement();
                        annMorphosyntaxWriter.writeCharacters("\n");
                    }

                    this.indent(annMorphoSyntaxIndent, annMorphosyntaxWriter);
                    annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
                    annMorphosyntaxWriter.writeAttribute("name","disamb");
                    annMorphosyntaxWriter.writeCharacters("\n");
                    //ToDo: finish it




                    wholeParagraph.append(orth);
                    if (!noSpace){
                        wholeParagraph.append(" ");
                    }
                    tokenNr++;
                }
                this.indent(5, annSegmentationWriter);
                annSegmentationWriter.writeEndElement();
                annSegmentationWriter.writeCharacters("\n");
                sentenceNr++;
            }
            this.indent(4, annSegmentationWriter);
            annSegmentationWriter.writeEndElement();
            annSegmentationWriter.writeCharacters("\n");
            textWriter.writeCharacters(wholeParagraph.toString().trim());
            textWriter.writeCharacters("\n");
            this.indent(5, textWriter);
            textWriter.writeEndElement();
            textWriter.writeCharacters("\n");


        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }


    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
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
                disamb = firstLex.base+":"+firstLex.ctag+":"+firstLex.msdList.get(0).getKey();
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
