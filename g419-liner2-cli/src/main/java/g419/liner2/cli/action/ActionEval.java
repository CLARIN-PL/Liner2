package g419.liner2.cli.action;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ChunkerEvaluator;
import g419.liner2.api.tools.ChunkerEvaluatorMuc;
import g419.liner2.api.tools.ProcessingTimer;


/**
 * Evaluate chunker on a specified corpus.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionEval extends Action{

    private String inputFile = null;
    private String inputFormat = null;
    private boolean errorsOnly = false;

    private boolean checkLemma = false;
    
    private static final String PARAM_ERRORS_ONLY = "e";
    private static final String PARAM_ERRORS_ONLY_LONG = "errors-only";
    private static final String PARAM_CHECK_LEMMA = "l";
    private static final String PARAM_CHECK_LEMMA_LONG = "lemma";

    //@SuppressWarnings("static-access")
	public ActionEval() {
		super("eval");
        this.setDescription("evaluates chunkers against a specific set of documents (-i batch:FORMAT, -i FORMAT) #or perform cross validation (-i cv:{format})");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(CommonOptions.getVerboseDeatilsOption());
        this.options.addOption(Option.builder(PARAM_ERRORS_ONLY).longOpt(PARAM_ERRORS_ONLY_LONG).desc("print only sentence with errors").build());
        this.options.addOption(Option.builder(PARAM_CHECK_LEMMA).longOpt(PARAM_CHECK_LEMMA_LONG).desc("evaluate with annotation lemma").build());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        this.errorsOnly = line.hasOption(PARAM_ERRORS_ONLY_LONG);
        this.checkLemma = line.hasOption(PARAM_CHECK_LEMMA_LONG);
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        if(line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)){
            ConsolePrinter.verboseDetails = true;
        }
	}
	
	/**
	 * 
	 */		
	public void run() throws Exception {

		if ( !LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
		}
		
    	ProcessingTimer timer = new ProcessingTimer();
    	TokenFeatureGenerator gen = null;
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
    	
    	System.out.print("Annotations to evaluate:");
        if(LinerOptions.getGlobal().types.isEmpty()){
            System.out.print(" all");
        } else {
            for (Pattern pattern : LinerOptions.getGlobal().types){
                System.out.print(" " + pattern);
            }
        }
    	System.out.println();

        if (this.inputFormat.startsWith("cv:")){
            ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions.getGlobal().types, true);
            globalEval.setCheckLemma(this.checkLemma);
            ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

            this.inputFormat = this.inputFormat.substring(3);
            LinerOptions.getGlobal().setCVDataFormat(this.inputFormat);
            ArrayList<List<String>> folds = loadFolds();
            for(int i=0; i < folds.size(); i++){
                timer.startTimer("fold "+ (i + 1));
                System.out.println("***************************************** FOLD " + (i + 1) + " *****************************************");
                String trainSet = getTrainingSet(i, folds);
                String testSet = getTestingSet(i, folds);
                ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
                cm.loadTrainData(new BatchReader(IOUtils.toInputStream(trainSet), "", this.inputFormat), gen);
                cm.loadTestData(new BatchReader(IOUtils.toInputStream(testSet), "", this.inputFormat), gen);
                AbstractDocumentReader reader = new BatchReader(IOUtils.toInputStream(testSet), "", this.inputFormat);
                evaluate(reader, gen, cm, globalEval, globalEvalMuc, errorsOnly);
                timer.stopTimer();
            }

            System.out.println("***************************************** SUMMARY *****************************************");
            globalEval.printResults();
            globalEvalMuc.printResults();
            System.out.println("");
            timer.printStats();
        } else {
            ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
            cm.loadTestData(ReaderFactory.get().getStreamReader(this.inputFile, this.inputFormat), gen);
            evaluate(ReaderFactory.get().getStreamReader(this.inputFile, this.inputFormat),
                    gen, cm, null, null, errorsOnly);
        }


	}

    private void evaluate(AbstractDocumentReader dataReader, TokenFeatureGenerator gen, ChunkerManager cm,
                          ChunkerEvaluator globalEval, ChunkerEvaluatorMuc globalEvalMuc, boolean errorsOnly) throws Exception {
        ProcessingTimer timer = new ProcessingTimer();
        timer.startTimer("Model loading");
        cm.loadChunkers();
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
        timer.stopTimer();


    	/* Create all defined chunkers. */
        ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().types, false, errorsOnly);
        eval.setCheckLemma(this.checkLemma);
        ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

        timer.startTimer("Data reading");
        Document ps = dataReader.nextDocument();
        timer.stopTimer();

        HashMap<Sentence, AnnotationSet> chunkings = null;
        while ( ps != null ){

    		/* Get reference set of annotations */
            HashMap<Sentence, AnnotationSet> referenceChunks = ps.getChunkings();

    		/* Remove annotations from data */
            //ps.removeAnnotations();
            ps.removeAnnotationsByTypePatterns(LinerOptions.getGlobal().types);

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
                globalEval.evaluate(ps, chunkings, referenceChunks);
                globalEvalMuc.evaluate(ps, chunkings, referenceChunks);
            }
            eval.evaluate(ps, chunkings, referenceChunks);
            evalMuc.evaluate(ps, chunkings, referenceChunks);
            timer.stopTimer();

            timer.startTimer("Data reading");
            ps = dataReader.nextDocument();
            timer.stopTimer();
        }

        eval.printResults();
        evalMuc.printResults();
        timer.printStats();
    }

    private ArrayList<List<String>> loadFolds() throws IOException, DataFormatException {
        ArrayList<List<String>> folds = new ArrayList<List<String>>();
        /** Wczytaj listy plików */
        File sourceFile = new File(this.inputFile);
        String root = sourceFile.getParentFile().getAbsolutePath();
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

        String line = bf.readLine();
        while ( line != null ){
            String[] fileData = line.split("\t");
            if(fileData.length != 2){
                throw new DataFormatException("Incorrect line in folds file: "+this.inputFile+"\\"+line+"\nProper line format: {file_name}\\t{fold_nr}");
            }
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
