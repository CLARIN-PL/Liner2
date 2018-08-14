package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.converter.Converter;
import g419.liner2.core.converter.factory.ConverterFactory;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ActionConvert extends Action {

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;
    public static final String OPTION_CONVERSION = "c";
    public static final String OPTION_CONVERSION_LONG = "conversion";

    public ArrayList<String> convertersDesciptions = new ArrayList<>();
    LinkedHashMap<String, String> features = new LinkedHashMap<>();

    public ActionConvert() {
        super("convert");
        setDescription("converts documents from one format to another and applies defined converters");
        options.addOption(CommonOptions.getInputFileFormatOption());
        options.addOption(CommonOptions.getInputFileNameOption());
        options.addOption(CommonOptions.getOutputFileFormatOption());
        options.addOption(CommonOptions.getOutputFileNameOption());
        options.addOption(CommonOptions.getFeaturesOption());

        options.addOption(Option.builder(OPTION_CONVERSION).longOpt(OPTION_CONVERSION_LONG).hasArgs().argName("file")
                .desc("converter description").build());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if (line.hasOption(OPTION_CONVERSION)) {
            final String[] converters = line.getOptionValues(OPTION_CONVERSION);
            for (final String conv : converters) {
                convertersDesciptions.add(conv);
            }

        }
        final String featuresFile = line.getOptionValue(CommonOptions.OPTION_FEATURES);
        if (featuresFile != null) {

            features = LinerOptions.getGlobal().parseFeatures(featuresFile);
        }

    }

    @Override
    public void run() throws Exception {

        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format);

        TokenFeatureGenerator gen = null;
        if (!features.isEmpty()) {
            gen = new TokenFeatureGenerator(features);
        }
        Converter converter = null;
        if (!convertersDesciptions.isEmpty()) {
            converter = ConverterFactory.createPipe(convertersDesciptions);
        }

        final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        Document ps = reader.nextDocument();
        while (ps != null) {
            Logger.getLogger(getClass()).info("Processing " + ps.getName() + " ...");
            if (gen != null) {
                if (gen != null) {
                    Logger.getLogger(getClass()).info(" - generating features ...");
                }
                gen.generateFeatures(ps);
            }

            if (converter != null) {
                Logger.getLogger(getClass()).info(" - applying converter ...");
                converter.apply(ps);
            }

            Logger.getLogger(getClass()).info(" - writing ...");
            writer.writeDocument(ps);
            ps = reader.nextDocument();
        }

        reader.close();
        writer.close();
    }

    @Override
    public void printOptions() {
        super.printOptions();
        System.out.println();
        ConverterFactory.printAvailableConverters();

    }

}
