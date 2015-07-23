package g419.tools;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.Wordnet2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.io.*;

/**
 * Created by michal on 7/23/15.
 */
public class WordnetTool extends Tool {

    public static final String OPTION_WORDNET = "w";
    public static final String OPTION_WORDNET_LONG = "wordnet";

    String output_file = null;
    Wordnet2 wordnet_loader = null;

    public WordnetTool(){
        super("wordnet");
        this.setDescription("ToDo");
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        OptionBuilder.withDescription("use base forms of named entities");
        OptionBuilder.isRequired();
        OptionBuilder.withArgName("wordnet").hasArg();
        OptionBuilder.withLongOpt(OPTION_WORDNET_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_WORDNET));
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        System.out.println(line.getOptionValue(OPTION_WORDNET));
        this.wordnet_loader = new Wordnet2(line.getOptionValue(OPTION_WORDNET));

    }

    @Override
    public void run() throws Exception {
        TokenAttributeIndex attridx = null;
        Document outputDoc = new Document("multiwordPhrases", attridx);
        Paragraph paragraph = new Paragraph("ch1");
        paragraph.setAttributeIndex(attridx);
        outputDoc.addParagraph(paragraph);
        int sentId = 1;
        for(String phrase:  wordnet_loader.getMuliwordPhrases()){
            System.out.println(phrase);
            Sentence sent = macaAnalyze(phrase).getSentences().get(0);
            sent.setId("s"+sentId);
            if(attridx == null){
                attridx = sent.getAttributeIndex().clone();
                outputDoc.setAttributeIndex(attridx);
            }
            sent.setAttributeIndex(attridx);
            paragraph.addSentence(sent);
            sentId++;
        }
        AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(this.output_file, "ccl");
        writer.writeDocument(outputDoc);

    }

    private Document macaAnalyze(String cSeq) throws Exception {
        File tager_input = File.createTempFile("wcrft_input", ".txt");
        BufferedWriter tager_writer = new BufferedWriter(
                new FileWriter(tager_input));

        tager_writer.write(cSeq, 0, cSeq.length());
        tager_writer.close();
        String cmd = "wcrft-app nkjp_e2.ini -i text -o ccl -A " + tager_input.getAbsolutePath();
        Process tager = Runtime.getRuntime().exec(cmd);
        InputStream tager_in = tager.getInputStream();
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("/tmp/phre.xml", tager_in, "ccl");
        return reader.nextDocument();
    }
}
