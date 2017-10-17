package g419.spatial.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.spatial.tools.SpatialRelationRecognizer;
import g419.toolbox.wordnet.Wordnet3;

public class ActionPipe extends Action {
	
	private String inputFilename = null;
	private String inputFormat = null;
	
	private String outputFilename = null;
	private String outputFormat = null;
	
	private String maltparserModel = null;
	private String wordnetPath = null;
	
	/**
	 * 
	 */
	public ActionPipe() {
		super("pipe");
		this.setDescription("recognize spatial expressions and add them to the document as a set of frames");
		this.options.addOption(CommonOptions.getInputFileNameOption());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(CommonOptions.getOutputFileFormatOption());
		this.options.addOption(CommonOptions.getOutputFileNameOption());
		this.options.addOption(CommonOptions.getMaltparserModelFileOption());
		this.options.addOption(CommonOptions.getWordnetOption(true));
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
        this.outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
        this.maltparserModel = line.getOptionValue(CommonOptions.OPTION_MALT);
        this.wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET);
    }

	@Override
	public void run() throws Exception {
		Wordnet3 wordnet = new Wordnet3(this.wordnetPath);
		MaltParser malt = new MaltParser(this.maltparserModel);		
		SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(malt, wordnet);

		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);
		AbstractDocumentWriter writer = null;
				
		if ( this.outputFilename == null ){
			writer = WriterFactory.get().getStreamWriter(System.out, this.outputFormat);
		}
		else{
			writer = WriterFactory.get().getStreamWriter(this.outputFilename, this.outputFormat);
		}
		
		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			recognizer.recognizeInPlace(document);
			writer.writeDocument(document);
		}
		writer.close();
			
		reader.close();
	}
		
}
