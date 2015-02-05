package g419.liner2.cli.action;

import g419.corpus.Logger;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.*;
import g419.corpus.structure.CrfTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;

/**
 * Perform bottom-up feature selection using chunker on a specified corpus.
 * 
 * @author Jan Kocoń
 * 
 */
public class ActionFeatureSelection extends Action {

	public static final String OPTION_TEMPLATES = "T";
	public static final String OPTION_TEMPLATES_LONG = "templates";

    public static final String OPTION_CHUNKER = "C";
    public static final String OPTION_CHUNKER_LONG = "chunker";

	private String input_file = null;
	private String input_format = null;
	private HashMap<String, CrfTemplate> templates = new HashMap<String, CrfTemplate>();
    private TokenFeatureGenerator gen;
    private String chunker = null;

	public ActionFeatureSelection() {
		super("selection");
		this.setDescription("todo");

		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(CommonOptions.getInputFileNameOption());
		this.options.addOption(CommonOptions.getModelFileOption());
		this.options.addOption(CommonOptions.getVerboseDeatilsOption());
		
		OptionBuilder.withArgName("templates");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("file with paths to template files");
		OptionBuilder.withLongOpt(OPTION_TEMPLATES_LONG);
		OptionBuilder.isRequired();
		this.options.addOption(OptionBuilder.create(OPTION_TEMPLATES));
		
		OptionBuilder.withArgName("chunker");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("chunker used for testing features");
		OptionBuilder.withLongOpt(OPTION_CHUNKER_LONG);
		OptionBuilder.isRequired();		
        this.options.addOption(OptionBuilder.create(OPTION_CHUNKER));
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
		CommandLine line = new GnuParser().parse(this.options, args);
		parseDefault(line);
		loadTemplates(line.getOptionValue(OPTION_TEMPLATES));
        chunker = line.getOptionValue(OPTION_CHUNKER);
		this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
		this.input_format = line.getOptionValue(
				CommonOptions.OPTION_INPUT_FORMAT, "ccl");
		LinerOptions.getGlobal().parseModelIni(
				line.getOptionValue(CommonOptions.OPTION_MODEL));
		if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
			Logger.verboseDetails = true;
		}
	}

	private void loadTemplates(String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer sb = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			if (line.isEmpty() || !line.startsWith("#")) {
				line = line.trim();
				templates.put(line, TemplateFactory.parseTemplate(line));
				line = br.readLine();
			}
		}
	}

	/**
	 * 
	 */
	public void run() throws Exception {

		if (!LinerOptions.isOption(LinerOptions.OPTION_USED_CHUNKER)) {
			throw new ParameterException(
					"Parameter 'chunker' in 'main' section of model not set");
		}
		// not nice solution - but initializes all objects with full feature set
		Iterator<Entry<String, CrfTemplate>> it = templates.entrySet()
				.iterator();
		System.out.println("#FS: begin");
		while (it.hasNext()) {
			Entry<String, CrfTemplate> pairs = it.next();
			System.out.println(pairs.getKey());
			CrfTemplate ct = pairs.getValue();
			System.out.println("#FS: current template: " + pairs.getKey());
			selectFeaturesBottomUp(ct);
			// selectFeaturesTopDown(ct);
		}
		System.out.println("#FS: end");
	}

	private void selectFeaturesTopDown(CrfTemplate ct) throws Exception {
		ArrayList<String> featureNames = new ArrayList<String>();
		Hashtable<String, String[]> features = new Hashtable<String, String[]>();
		Iterator<String> it = ct.getFeatureNames().iterator();
		System.out.println("#FS: initial features list");
		while (it.hasNext()) {
			String featureName = it.next();
			System.out.println("#FS: >> " + featureName);
			featureNames.add(featureName);
			features.put(featureName, ct.getFeatures().get(featureName));
		}
		// ct.getFeatureNames().clear();
		// ct.getFeatures().clear();
		float globalBestFMeasure = 0.0f;
		float localBestFMeasure = 0.0f;
		String localBestFeatureName = null;
		ChunkerEvaluator localEvaluator = null;
		System.out.println("#FS: initial features list");
		int iterationNumber = 0;
		while (!featureNames.isEmpty()) {
			iterationNumber++;
			System.out.println("#FS: iteration: " + iterationNumber);
			Iterator<String> localIt = featureNames.iterator();
			localBestFeatureName = null;
			localBestFMeasure = globalBestFMeasure;
			while (localIt.hasNext()) {
				String currentFeatureName = localIt.next();
				ct.getFeatureNames().remove(currentFeatureName);
				ct.getFeatures().remove(currentFeatureName);
				System.out.println("#FS: checking feature: "
						+ currentFeatureName);
                ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
                cm.setChunkerTemplate(chunker, ct);
                localEvaluator = eval(cm);
				float currentFMeasure = localEvaluator.getFMeasure();
				if (currentFMeasure > localBestFMeasure) {
					System.out.println("#FS: current local best to remove: "
							+ currentFeatureName);
					System.out.println("#FS: previous local FMeasure: "
							+ localBestFMeasure);
					System.out.println("#FS: current local FMeasure: "
							+ currentFMeasure);
					System.out.println("#FS: local gain: "
							+ (currentFMeasure - localBestFMeasure));
					localBestFMeasure = currentFMeasure;
					localBestFeatureName = currentFeatureName;
				}
				ct.getFeatures().put(currentFeatureName,
						features.get(currentFeatureName));
				ct.getFeatureNames().add(currentFeatureName);
			}
			if (localBestFMeasure > globalBestFMeasure) {
				System.out.println("#FS: local best to remove: "
						+ localBestFeatureName);
				System.out.println("#FS: previous FMeasure: "
						+ globalBestFMeasure);
				System.out.println("#FS: current FMeasure: "
						+ localBestFMeasure);
				System.out.println("#FS: gain: "
						+ (localBestFMeasure - globalBestFMeasure));
				globalBestFMeasure = localBestFMeasure;
				ct.getFeatureNames().remove(localBestFeatureName);
				ct.getFeatures().remove(localBestFeatureName);
				featureNames.remove(localBestFeatureName);
				features.remove(localBestFeatureName);
			} else {
				System.out.println("#FS: no gain, finishing");
				break;
			}
		}
		System.out.println("#FS: summary");
		System.out.println("#FS: remaining features:");
		Iterator<String> finalIt = ct.getFeatureNames().iterator();
		while (finalIt.hasNext())
			System.out.println("#FS: >> " + finalIt.next());
	}

	private void selectFeaturesBottomUp(CrfTemplate ct) throws Exception {
		ArrayList<String> featureNames = new ArrayList<String>();
		Hashtable<String, String[]> features = new Hashtable<String, String[]>();
		Iterator<String> it = ct.getFeatureNames().iterator();
		System.out.println("#FS: initial features list");
		while (it.hasNext()) {
			String featureName = it.next();
			System.out.println("#FS: >> " + featureName);
			featureNames.add(featureName);
			features.put(featureName, ct.getFeatures().get(featureName));
		}
		ct.getFeatureNames().clear();
		ct.getFeatures().clear();
		float globalBestFMeasure = 0.0f;
		float localBestFMeasure = 0.0f;
		String localBestFeatureName = null;
		ChunkerEvaluator localEvaluator = null;
		System.out.println("#FS: initial features list");
		int iterationNumber = 0;
		while (!featureNames.isEmpty()) {
			iterationNumber++;
			System.out.println("#FS: iteration: " + iterationNumber);
			Iterator<String> localIt = featureNames.iterator();
			localBestFeatureName = null;
			localBestFMeasure = globalBestFMeasure;
			while (localIt.hasNext()) {
				String currentFeatureName = localIt.next();
				ct.getFeatureNames().add(currentFeatureName);
				ct.getFeatures().put(currentFeatureName,
						features.get(currentFeatureName));
				System.out.println("#FS: checking feature: "
						+ currentFeatureName);
                ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
                cm.setChunkerTemplate(chunker, ct);
				localEvaluator = eval(cm);

				float currentFMeasure = localEvaluator.getFMeasure();
				if (currentFMeasure > localBestFMeasure) {
					System.out.println("#FS: current local best: "
							+ currentFeatureName);
					System.out.println("#FS: previous local FMeasure: "
							+ localBestFMeasure);
					System.out.println("#FS: current local FMeasure: "
							+ currentFMeasure);
					System.out.println("#FS: local gain: "
							+ (currentFMeasure - localBestFMeasure));
					localBestFMeasure = currentFMeasure;
					localBestFeatureName = currentFeatureName;
				}
				ct.getFeatureNames().remove(currentFeatureName);
				ct.getFeatures().remove(currentFeatureName);
			}
			if (localBestFMeasure > globalBestFMeasure) {
				System.out.println("#FS: local best: " + localBestFeatureName);
				System.out.println("#FS: previous FMeasure: "
						+ globalBestFMeasure);
				System.out.println("#FS: current FMeasure: "
						+ localBestFMeasure);
				System.out.println("#FS: gain: "
						+ (localBestFMeasure - globalBestFMeasure));
				globalBestFMeasure = localBestFMeasure;
				ct.getFeatureNames().add(localBestFeatureName);
				ct.getFeatures().put(localBestFeatureName,
						features.get(localBestFeatureName));
				featureNames.remove(localBestFeatureName);
				features.remove(localBestFeatureName);
			} else {
				System.out.println("#FS: no gain, finishing");
				break;
			}
		}
		System.out.println("#FS: summary");
		System.out.println("#FS: selected features:");
		Iterator<String> finalIt = ct.getFeatureNames().iterator();
		while (finalIt.hasNext())
			System.out.println("#FS: >> " + finalIt.next());
	}

	private ChunkerEvaluator eval(ChunkerManager cm) throws Exception {

		ChunkerEvaluator result = null;
		ProcessingTimer timer = new ProcessingTimer();
        if (!LinerOptions.getGlobal().features.isEmpty()){
            this.gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		System.out.print("Annotations to evaluate:");
		if (LinerOptions.getGlobal().getTypes().isEmpty()) {
			System.out.print(" all");
		} else {
			for (Pattern pattern : LinerOptions.getGlobal().getTypes())
				System.out.print(" " + pattern);
			System.out.println("\nDIRTY list-to-set CONVERSION");
			HashMap<String, Pattern> s = new HashMap<String, Pattern>();
			for (Pattern pattern : LinerOptions.getGlobal().getTypes())
				s.put(pattern.pattern(), pattern);
			LinerOptions.getGlobal().getTypes().clear();
			for (Entry<String, Pattern> e : s.entrySet())
				LinerOptions.getGlobal().getTypes().add(e.getValue());
			for (Pattern pattern : LinerOptions.getGlobal().getTypes())
				System.out.print(" " + pattern);
		}
		System.out.println();

        if (this.input_format.startsWith("cv:")){
            ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions.getGlobal().types, true);
            ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().types);

            String input_format = this.input_format.substring(3);
            LinerOptions.getGlobal().setCVDataFormat(this.input_format);
            ArrayList<List<String>> folds = loadFolds();
            for(int i=0; i < folds.size(); i++){
                timer.startTimer("fold "+ (i + 1));
                System.out.println("***************************************** FOLD " + (i + 1) + " *****************************************");
                String trainSet = getTrainingSet(i, folds);
                String testSet = getTestingSet(i, folds);
                cm.loadTrainData(new BatchReader(IOUtils.toInputStream(trainSet), "", input_format), this.gen);
                cm.loadTestData(new BatchReader(IOUtils.toInputStream(testSet), "", input_format), this.gen);
                AbstractDocumentReader reader = new BatchReader(IOUtils.toInputStream(testSet), "", input_format);
                evaluate(reader, gen, cm, globalEval, globalEvalMuc);
                timer.stopTimer();


            }

            System.out.println("***************************************** SUMMARY *****************************************");
            globalEval.printResults();
            globalEvalMuc.printResults();
            System.out.println("");
            timer.printStats();
            result = globalEval;
        }
        else{
            System.out.println("INPUT: " + this.input_file + " | " + this.input_format);
            cm.loadTestData(ReaderFactory.get().getStreamReader(this.input_file, this.input_format), gen);
            result = evaluate(ReaderFactory.get().getStreamReader(this.input_file, this.input_format),
                    gen, cm, null, null);
        }
		return result;
	}

    private ChunkerEvaluator evaluate(AbstractDocumentReader dataReader, TokenFeatureGenerator gen, ChunkerManager cm,
                                      ChunkerEvaluator globalEval, ChunkerEvaluatorMuc globalEvalMuc) throws Exception {
        ProcessingTimer timer = new ProcessingTimer();
        timer.startTimer("Model loading");
        cm.resetChunkers();
        cm.loadChunkers();
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
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
        return eval;
    }

    private ArrayList<List<String>> loadFolds() throws IOException, DataFormatException {
        ArrayList<List<String>> folds = new ArrayList<List<String>>();
        /** Wczytaj listy plików */
        File sourceFile = new File(this.input_file);
        String root = sourceFile.getParentFile().getAbsolutePath();
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

        String line = bf.readLine();
        while ( line != null ){
            String[] fileData = line.split("\t");
            if(fileData.length != 2){
                throw new DataFormatException("Incorrect line in folds file: "+this.input_file+"\\"+line+"\nProper line format: {file_name}\\t{fold_nr}");
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
