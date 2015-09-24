package g419.liner2.cli.action;

import g419.corpus.Logger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.normalizer.Normalizer;
import g419.liner2.api.tools.*;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ActionNormalizerEval extends Action {

    private String input_file = null;
    private String input_format = null;
    private TokenFeatureGenerator gen = null;
    protected PrintWriter missPrinter = null;
    protected Set<String> metaDataKeys = null;

    @SuppressWarnings("static-access")
    public ActionNormalizerEval() {
        super("normalizer-eval");
        this.setDescription("evaluates normalizer against a specific set of documents (-i batch:FORMAT, -i FORMAT)");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(CommonOptions.getVerboseDeatilsOption());
        this.options.addOption(OptionBuilder.withArgName("filename").hasArg().
                withLongOpt("misses").
                withDescription("Path to file in which misses will be saved (for model tweaking)").
                create("misses")
        );
        this.options.addOption(OptionBuilder.withArgName("keys").hasArg().
                withLongOpt("metaKeys").
                withDescription("Set of metadata keys (joined with ;), used as view while comparing normalization results.").
                create("metaKeys")
        );

    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        if(line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
            Logger.verboseDetails = true;
        }
        this.missPrinter = line.hasOption("misses") ?
                new PrintWriter(new FileOutputStream(line.getOptionValue("misses"), false)) :
                null;

        if (!line.hasOption("metaKeys"))
            throw new IllegalStateException("Provide 'metaKeys' argument!"); //todo: specialize
        metaDataKeys = new HashSet<>(Arrays.asList(line.getOptionValue("metaKeys").split("[;]")));
    }

    /**
     *
     */
    public void run() throws Exception {

        if (!LinerOptions.isOption(LinerOptions.OPTION_USED_CHUNKER)) {
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
        }

        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

        ProcessingTimer timer = new ProcessingTimer();
        timer.startTimer("Model loading");
        Normalizer normalizer = getNormalizer();
        timer.stopTimer();

        System.out.print("Annotations to evaluate:");
            for (Pattern pattern : normalizer.getNormalizedChunkTypes())
                System.out.print(" " + pattern);
        System.out.println();

        evaluate(ReaderFactory.get().getStreamReader(this.input_file, this.input_format),
                gen, normalizer, timer);
    }

    protected Normalizer getNormalizer() throws Exception {
        ChunkerManager manager = new ChunkerManager(LinerOptions.getGlobal());
        manager.loadChunkers();
        manager.loadTestData(ReaderFactory.get().getStreamReader(this.input_file, this.input_format), gen);
        Chunker chunker = manager.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
        if (!(chunker instanceof Normalizer)) {
            ClassCastException up = new ClassCastException("Specified Chunker is not Normalizer, but "+chunker.getClass().getSimpleName());
            throw up;
        }
        Normalizer normalizer = (Normalizer) chunker;
        if (normalizer.getNormalizedChunkTypes().isEmpty()) {
            RuntimeException up = new RuntimeException("This should be checked in factory item!");
            throw up;
        }
        manager.resetChunkers();
        Logger.log("Using normalizer: "+normalizer);
        return normalizer;

    }

    private void evaluate(AbstractDocumentReader dataReader, TokenFeatureGenerator gen, Normalizer normalizer,
                          ProcessingTimer timer) throws Exception {
        NormalizerEvaluator evaluator = new NormalizerEvaluator(this.missPrinter, this.metaDataKeys);
        evaluator.reset();
        timer.startTimer("Data reading");
        Document ps = dataReader.nextDocument();
        timer.stopTimer();

        while ( ps != null ){

    		/* Generate features */
            timer.startTimer("Feature generation");
            if ( gen != null )
                gen.generateFeatures(ps);
            timer.stopTimer();

            timer.startTimer("Evaluation", false);
            timer.addTokens(ps);
            evaluator.evaluateDocument(ps, normalizer, timer);
            timer.stopTimer();

            timer.startTimer("Data reading");
            ps = dataReader.nextDocument();
            timer.stopTimer();
        }
        missPrinter.close();
        evaluator.printResults();
        timer.printStats();
    }

}
