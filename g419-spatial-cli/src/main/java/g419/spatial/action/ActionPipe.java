package g419.spatial.action;

import com.google.common.base.Optional;
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
	
	private Optional<String> inputFilename;
	private Optional<String> inputFormat;
	private Optional<String> outputFilename;
	private Optional<String> outputFormat;
	private Optional<String> maltparserModel;
	private Optional<String> wordnetPath;
	
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
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        final CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFilename = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_INPUT_FILE));
        this.inputFormat = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT));
        this.outputFilename = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE));
        this.outputFormat = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT));
        this.maltparserModel = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_MALT));
        this.wordnetPath = Optional.fromNullable(line.getOptionValue(CommonOptions.OPTION_WORDNET));
    }

	@Override
	public void run() throws Exception {
		final Wordnet3 wordnet = new Wordnet3(wordnetPath.get());
		final MaltParser malt = new MaltParser(maltparserModel.get());
		final SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer(malt, wordnet);

		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename.get(), inputFormat.get());
		AbstractDocumentWriter writer;

		if ( outputFilename.isPresent() ){
			writer = WriterFactory.get().getStreamWriter(outputFilename.get(), outputFormat.get());
		} else {
			writer = WriterFactory.get().getStreamWriter(System.out, outputFormat.get());
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
