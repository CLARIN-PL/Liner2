package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ChunkerEvaluator;
import g419.liner2.api.tools.ChunkerEvaluatorMuc;
import g419.liner2.api.tools.ParameterException;
import g419.liner2.api.tools.ProcessingTimer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import g419.liner2.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;


/**
 * Evaluate chunker on a specified corpus.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionEval extends Action{

    public static final String OPTION_VERBOSE_DETAILS = "d";
    public static final String OPTION_VERBOSE_DETAILS_LONG = "details";

    private String input_file = null;
    private String input_format = null;

    @SuppressWarnings("static-access")
	public ActionEval() {
		super("eval");
        this.setDescription("data evaluation using given model, crossvalidaion mode included");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(OptionBuilder
                .withArgName("verbose_details").hasArg()
                .withDescription("verbose processed sentences data")
                .withLongOpt(OPTION_VERBOSE_DETAILS_LONG)
                .create(OPTION_VERBOSE_DETAILS));


	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        if(line.hasOption(OPTION_VERBOSE_DETAILS)){
            LinerOptions.getGlobal().verboseDetails = true;
        }
	}
	
	/**
	 * 
	 */		
	public void run() throws Exception {

		if ( !LinerOptions.isOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
		}
		
    	ProcessingTimer timer = new ProcessingTimer();
    	TokenFeatureGenerator gen = null;
    	
    	System.out.print("Annotations to evaluate:");
        if(LinerOptions.getGlobal().types.isEmpty()){
            System.out.print(" all");
        }
        else{
            for (Pattern pattern : LinerOptions.getGlobal().types)
                System.out.print(" " + pattern);
        }
    	System.out.println();

        if (this.input_format.startsWith("cv:")){
            ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions.getGlobal().types, true);
            ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

            this.input_format = this.input_format.substring(3);
            LinerOptions.getGlobal().setCVDataFormat(this.input_format);
            ArrayList<List<String>> folds = loadFolds();
            for(int i=0; i < folds.size(); i++){
                timer.startTimer("fold "+ (i + 1));
                System.out.println("***************************************** FOLD " + (i + 1) + " *****************************************");
                String trainSet = getTrainingSet(i, folds);
                String testSet = getTestingSet(i, folds);
                LinerOptions.getGlobal().setCVTrainData(trainSet);
                AbstractDocumentReader reader = new BatchReader(IOUtils.toInputStream(testSet), "", this.input_format);
                evaluate(reader, gen, globalEval, globalEvalMuc);
                timer.stopTimer();


            }

            System.out.println("***************************************** SUMMARY *****************************************");
            globalEval.printResults();
            globalEvalMuc.printResults();
            System.out.println("");
            timer.printStats();
        }
        else{
            evaluate(ReaderFactory.get().getStreamReader(this.input_file, this.input_format),
                    gen, null, null);
        }


	}

    private void evaluate(AbstractDocumentReader dataReader, TokenFeatureGenerator gen,
                          ChunkerEvaluator globalEval, ChunkerEvaluatorMuc globalEvalMuc) throws Exception {
        ProcessingTimer timer = new ProcessingTimer();
        timer.startTimer("Model loading");
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
        timer.stopTimer();


    	/* Create all defined chunkers. */
        ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().types);
        ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

        timer.startTimer("Data reading");
        Document ps = dataReader.nextDocument();
        timer.stopTimer();

        HashMap<Sentence, AnnotationSet> chunkings = null;
        while ( ps != null ){

    		/* Get reference set of annotations */
            HashMap<Sentence, AnnotationSet> referenceChunks = ps.getChunkings();

    		/* Remove annotations from data */
            ps.removeAnnotations();

    		/* Generate features */
            timer.startTimer("Feature generation");
            if ( gen != null )
                gen.generateFeatures(ps);
            timer.stopTimer();

            timer.startTimer("Chunking");
            chunker.prepare(ps);
            try{
                chunkings = chunker.chunk(ps);
            }
            catch(Exception ex){
                System.err.println("Failed to chunk a sentence in document " + ps.getName());
                ex.printStackTrace(System.err);
                chunkings = new HashMap<Sentence, AnnotationSet>();
            }
            timer.stopTimer();

            timer.startTimer("Evaluation", false);
            timer.addTokens(ps);
            if(globalEval != null){
                globalEval.evaluate(ps.getSentences(), chunkings, referenceChunks);
                globalEvalMuc.evaluate(chunkings, referenceChunks);
            }
            eval.evaluate(ps.getSentences(), chunkings, referenceChunks);
            evalMuc.evaluate(chunkings, referenceChunks);
            timer.stopTimer();

            timer.startTimer("Data reading");
            ps = dataReader.nextDocument();
            timer.stopTimer();
        }

        eval.printResults();
        evalMuc.printResults();
        timer.printStats();
    }

    private ArrayList<List<String>> loadFolds() throws IOException {
        ArrayList<List<String>> folds = new ArrayList<List<String>>();
        /** Wczytaj listy plików */
        File sourceFile = new File(this.input_file);
        String root = sourceFile.getParentFile().getAbsolutePath();
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

        String line = bf.readLine();
        while ( line != null ){
            String[] fileData = line.split("\t");
            String file = fileData[0];
            int fold = Integer.parseInt(fileData[1]);
            if (!file.startsWith("/")) {
                file = root + "/" + file;
            }
            while(folds.size() < (fold)) {
                folds.add(new ArrayList<String>());
            }
                folds.get(fold - 1).add(file);
            line = bf.readLine();
        }
        bf.close();

        return folds;

    }

    private String getTrainingSet(int fold, ArrayList<List<String>> folds){
        StringBuilder sbtrain = new StringBuilder();

        for ( int i = 0; i< folds.size(); i++){
            if ( i != fold )
                for ( String line : folds.get(i))
                    sbtrain.append(line + "\n");
        }
        return sbtrain.toString().trim();
    }

    private String getTestingSet(int fold, ArrayList<List<String>> folds){
        StringBuilder sbtrain = new StringBuilder();
        for ( String line : folds.get(fold))
            sbtrain.append(line + "\n");
        return sbtrain.toString().trim();
    }
}
