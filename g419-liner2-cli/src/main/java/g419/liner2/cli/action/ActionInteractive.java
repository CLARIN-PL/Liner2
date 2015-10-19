package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

/**
 * Chunking in interactive mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionInteractive extends Action{

    private String input_format = null;
    private String output_format = null;
    private boolean silent = false;

    private static HashSet<String> validInputFormats = new HashSet<String>();
    private static HashSet<String> validOutputFormats = new HashSet<String>();

    static{
        validInputFormats.add("plain");
        validInputFormats.add("plain:maca");
        validInputFormats.add("plain:wcrft");

        validOutputFormats.add("ccl");
        validOutputFormats.add("iob");
        validOutputFormats.add("tuples");
        validOutputFormats.add("tokens");
    }

	public ActionInteractive() {
		super("interactive");
        this.setDescription("processes text entered directly into the terminal");

        Option inputFormat = CommonOptions.getInputFileFormatOption();
        inputFormat.setDescription("input format " + validInputFormats);
        this.options.addOption(inputFormat);
        Option outputFormat = CommonOptions.getOutputFileFormatOption();
        outputFormat.setDescription("output format "+ validOutputFormats);
        this.options.addOption(outputFormat);
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "plain:wcrft");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}

	public void run() throws Exception {
        
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
		}

        TokenFeatureGenerator gen = null;
        if(!validInputFormats.contains(input_format)){
            throw new ParameterException("Input format " + input_format + " is not valid for interactive mode. Use one of: " + validInputFormats);
        }
        if(!validOutputFormats.contains(output_format)){
            throw new ParameterException("Output format " + output_format + " is not valid for interactive mode. Use one of: " + validOutputFormats);
        }
        AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(System.out, output_format);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cSeq = "";
		
		if (!silent){
			System.out.println("# Loading, please wait...");
		}
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
		
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		if (!silent){
			System.out.println("# Enter a sentence and press Enter.");
			System.out.println("# Input format: " + input_format);
			System.out.println("# To finish, enter 'EOF'.");
        }
		
		do {
	        if (!silent)
	        	System.out.print("\n> ");
			
			// Get line of text to process
			cSeq = in.readLine();

			// If the text is not EndOfFile then process it
			if (!cSeq.equals("EOF")) {

				// if empty line -- continue
				if (Pattern.matches("^\\s*$", cSeq))
					continue;

				// force treating everything as one sentence? -- [SENTENCE] prefix
				boolean forceSentence = false;
				Pattern pSentence = Pattern.compile("^\\s*\\[SENTENCE\\]\\s*(.*)$");
				Matcher mSentence = pSentence.matcher(cSeq);
				if (mSentence.matches()) {
					cSeq = mSentence.group(1);
					forceSentence = true;
				}

				// morphological analysis, feature generation
				AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
						"terminal input", 
						IOUtils.toInputStream(cSeq), 
						input_format);
				Document ps = reader.nextDocument();
				reader.close();

				if (forceSentence)
					ps = mergeSentences(ps);
				
				if ( gen != null )
					gen.generateFeatures(ps);
                
				chunker.chunkInPlace(ps);

				// write output
                writer.writeDocument(ps);
                writer.flush();
			}
        } while (!cSeq.equals("EOF"));
		writer.close();
	}
	

	/**
	 * Merge all tokens into a single sentence.	
	 * @param paragraph
	 * @return
	 */
	private Document mergeSentences(Document paragraph) {
		Sentence merged = new Sentence();
		merged.setAttributeIndex(paragraph.getAttributeIndex());

		for (Sentence sentence : paragraph.getSentences())
			for (Token token : sentence.getTokens())
				merged.addToken(token);

		Document document = new Document("interactive mode", paragraph.getAttributeIndex());
		Paragraph resultParagraph = new Paragraph("id1");		
		resultParagraph.addSentence(merged);
		document.addParagraph(resultParagraph);

		return document;
	}
}
