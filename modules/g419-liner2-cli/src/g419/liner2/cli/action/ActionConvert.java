package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.Logger;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.converter.Converter;
import g419.liner2.api.converter.factory.ConverterFactory;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ActionConvert extends Action {

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
    public static final String OPTION_CONVERSION = "c";
    public static final String OPTION_CONVERSION_LONG = "conversion";

    public ArrayList<String> convertersDesciptions = new ArrayList<String>();
    LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();

    @SuppressWarnings("static-access")
	public ActionConvert() {
		super("convert");
        this.setDescription("converts documents from one format to another and applies defined converters");
        this.multipleValueOptions.add(OPTION_CONVERSION);
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(OptionBuilder
                .withArgName("file").hasArg()
                .withDescription("converter description")
                .withLongOpt(OPTION_CONVERSION_LONG)
                .create(OPTION_CONVERSION));
	}

	@Override
	public void parseOptions(String[] args) throws ParseException, IOException {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if(line.hasOption(OPTION_CONVERSION)) {
            String[] converters = line.getOptionValues(OPTION_CONVERSION);
            for(String conv: converters){
                this.convertersDesciptions.add(conv);
            }

        }
        String featuresFile = line.getOptionValue(CommonOptions.OPTION_FEATURES);
        if(featuresFile != null){
        	
            this.features = LinerOptions.getGlobal().parseFeatures(featuresFile);            
        }

	}
	
	@Override
	public void run() throws Exception {

        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);

        TokenFeatureGenerator gen = null;
        if (!this.features.isEmpty()) {
            gen = new TokenFeatureGenerator(this.features);
        }
        Converter converter = null;
        if (!this.convertersDesciptions.isEmpty()) {
            converter = ConverterFactory.createPipe(this.convertersDesciptions);
        }

        AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(this.output_file, this.output_format);
        Document ps = reader.nextDocument();
        while(ps != null) {
            if(gen != null) {
            	if ( gen != null ){
            		Logger.log("Generating features ...");
            	}
                gen.generateFeatures(ps);
            }

            if (converter != null) {
        		Logger.log("Applying converter ...");
                converter.apply(ps);
            }

    		Logger.log("Writing ...");
            writer.writeDocument(ps);
            ps = reader.nextDocument();
        }

        reader.close();
        writer.close();
	}

    @Override
    public void printOptions(){
        super.printOptions();
        System.out.println();
        ConverterFactory.printAvailableConverters();

    }

}
