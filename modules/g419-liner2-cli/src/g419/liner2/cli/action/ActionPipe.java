package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionPipe extends Action{

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;

	public ActionPipe(){
		super("pipe");
        this.setDescription("processes data with given model");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}
	
	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
        if ( !LinerOptions.getGlobal().isOption(LinerOptions.OPTION_USED_CHUNKER) ){
			throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
		}		
	
        AbstractDocumentReader reader = getInputReader();
		AbstractDocumentWriter writer = getOutputWriter();
        TokenFeatureGenerator gen = null;
        
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);            
        }
		
		/* Create all defined chunkers. */
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();

        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

		Document ps = reader.nextDocument();
		while ( ps != null ){
			if ( gen != null )
				gen.generateFeatures(ps);
			chunker.chunkInPlace(ps);
			writer.writeDocument(ps);
			ps = reader.nextDocument();
		}

		reader.close();
		writer.close();
	}

    /**
     * Get document writer defined with the -o and -t options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentWriter getOutputWriter() throws Exception{
        AbstractDocumentWriter writer;

        if ( output_format.startsWith("batch:") && !input_format.startsWith("batch:") ) {
            throw new Exception("Output format `batch:` (-o) is valid only for `batch:` input format (-i).");
        }
        if (output_file == null){
            writer = WriterFactory.get().getStreamWriter(System.out, output_format);
        }
        else if (output_format.equals("arff")){
//            ToDo: format w postaci arff:{PLIK Z TEMPLATEM}
            writer = null;
//            CrfTemplate arff_template = LinerOptions.getGlobal().getArffTemplate();
//            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        }
        return writer;
    }

    /**
     * Get document reader defined with the -i and -f options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }
		
}
