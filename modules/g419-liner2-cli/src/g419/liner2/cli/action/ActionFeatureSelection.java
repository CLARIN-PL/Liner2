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
import g419.corpus.structure.CrfTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Perform bottom-up feature selection using chunker on a specified corpus.
 * 
 * @author Jan Kocoń
 * 
 */
public class ActionFeatureSelection extends Action {

	/**
	 * 
	 */
	public void run() throws Exception {
		LinerOptions.getGlobal().setDefaultDataFormats("ccl", "ccl");

		if (!LinerOptions.isOption(LinerOptions.OPTION_USE)) {
			throw new ParameterException(
					"Parameter --use <chunker_pipe_desription> not set");
		}
		
		Iterator<Entry<String, CrfTemplate>> it = LinerOptions.getGlobal().templates
				.entrySet().iterator();
		System.out.println("#FS: begin");
		while (it.hasNext()) {
			Entry<String, CrfTemplate> pairs = it.next();
			CrfTemplate ct = pairs.getValue();
			System.out.println("#FS: current template: " + pairs.getKey());
			selectFeatures(ct);		
		}
		System.out.println("#FS: end");
	}
	
	private void selectFeatures(CrfTemplate ct) throws Exception{
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
		while (localBestFMeasure >= globalBestFMeasure && !featureNames.isEmpty()){
			iterationNumber ++;
			System.out.println("#FS: iteration: " + iterationNumber);		
			Iterator<String> localIt = featureNames.iterator();
			localBestFeatureName = null;
			localBestFMeasure = globalBestFMeasure;
			while (localIt.hasNext()){
				String currentFeatureName = localIt.next();
				ct.getFeatureNames().add(currentFeatureName);
				ct.getFeatures().put(currentFeatureName, features.get(currentFeatureName));
				System.out.println("#FS: checking feature: " + currentFeatureName);
				localEvaluator = eval();
				float currentFMeasure = localEvaluator.getFMeasure();
				if (currentFMeasure > localBestFMeasure){
					System.out.println("#FS: current local best: " + currentFeatureName);
					System.out.println("#FS: previous local FMeasure: " + localBestFMeasure);
					System.out.println("#FS: current local FMeasure: " + currentFMeasure);
					System.out.println("#FS: local gain: " + (currentFMeasure - localBestFMeasure));
					localBestFMeasure = currentFMeasure;
					localBestFeatureName = currentFeatureName;
				}
				ct.getFeatureNames().remove(currentFeatureName);
				ct.getFeatures().remove(currentFeatureName);
			}
			if (localBestFMeasure > globalBestFMeasure){
				System.out.println("#FS: local best: " + localBestFeatureName); 
				System.out.println("#FS: previous FMeasure: " + globalBestFMeasure);
				System.out.println("#FS: current FMeasure: " + localBestFMeasure);
				System.out.println("#FS: gain: " + (localBestFMeasure - globalBestFMeasure));
				globalBestFMeasure = localBestFMeasure;
				ct.getFeatureNames().add(localBestFeatureName);
				ct.getFeatures().put(localBestFeatureName, features.get(localBestFeatureName));
				featureNames.remove(localBestFeatureName);
				features.remove(localBestFeatureName);
			}
			else {
				System.out.println("#FS: no gain, finishing");
			}
		}
		System.out.println("#FS: summary");
		System.out.println("#FS: selected features:");
		Iterator<String> finalIt = ct.getFeatureNames().iterator();
		while (finalIt.hasNext())
			System.out.println("#FS: >> " + finalIt.next());		
	}
	

	private ChunkerEvaluator eval() throws Exception {

		ChunkerEvaluator result = null;
		ProcessingTimer timer = new ProcessingTimer();
		TokenFeatureGenerator gen = null;

		System.out.print("Annotations to evaluate:");
		if (LinerOptions.getGlobal().getTypes().isEmpty()) {
			System.out.print(" all");
		} else {
			for (Pattern pattern : LinerOptions.getGlobal().getTypes())
				System.out.print(" " + pattern);
		}
		System.out.println();

		String inputFormat = LinerOptions.getGlobal().getOption(
				LinerOptions.OPTION_INPUT_FORMAT);
		if (inputFormat.startsWith("cv:")) {
			ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions
					.getGlobal().getTypes());
			ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(
					LinerOptions.getGlobal().getTypes());

			inputFormat = inputFormat.substring(3);
			ArrayList<List<String>> folds = loadFolds();
			for (int i = 0; i < folds.size(); i++) {
				timer.startTimer("fold " + (i + 1));
				System.out
						.println("***************************************** FOLD "
								+ (i + 1)
								+ " *****************************************");
				String trainSet = getTrainingSet(i, folds);
				String testSet = getTestingSet(i, folds);
				LinerOptions.getGlobal().setCvTrain(trainSet);
				AbstractDocumentReader reader = new BatchReader(
						IOUtils.toInputStream(testSet), "", inputFormat);
				evaluate(reader, gen, globalEval);
				timer.stopTimer();

			}

			System.out
					.println("***************************************** SUMMARY *****************************************");
			globalEval.printResults();
			globalEvalMuc.printResults();
			System.out.println("");
			timer.printStats();
			result = globalEval;
		} else
			result = evaluate(
					ReaderFactory.get().getStreamReader(
							LinerOptions.getGlobal().getOption(
									LinerOptions.OPTION_INPUT_FILE),
							LinerOptions.getGlobal().getOption(
									LinerOptions.OPTION_INPUT_FORMAT)), gen,
					null);
		return result;
	}

	private ChunkerEvaluator evaluate(AbstractDocumentReader dataReader,
			TokenFeatureGenerator gen, ChunkerEvaluator globalEval)
			throws Exception {

		ProcessingTimer timer = new ProcessingTimer();
		timer.startTimer("Model loading");
		ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions
				.getGlobal());
		Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal()
				.getOptionUse());
		if (!LinerOptions.getGlobal().features.isEmpty()) {
			gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
		}
		timer.stopTimer();

		/* Create all defined chunkers. */
		ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal()
				.getTypes());
		ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(LinerOptions
				.getGlobal().getTypes());

		timer.startTimer("Data reading");
		Document ps = dataReader.nextDocument();
		timer.stopTimer();

		HashMap<Sentence, AnnotationSet> chunkings = null;
		while (ps != null) {

			/* Get reference set of annotations */
			HashMap<Sentence, AnnotationSet> referenceChunks = ps
					.getChunkings();

			/* Remove annotations from data */
			ps.removeAnnotations();

			/* Generate features */
			timer.startTimer("Feature generation");
			if (gen != null)
				gen.generateFeatures(ps);
			timer.stopTimer();

			timer.startTimer("Chunking");
			chunker.prepare(ps);
			try {
				chunkings = chunker.chunk(ps);
			} catch (Exception ex) {
				System.err.println("Failed to chunk a sentence in document "
						+ ps.getName());
				ex.printStackTrace(System.err);
				chunkings = new HashMap<Sentence, AnnotationSet>();
			}
			timer.stopTimer();

			timer.startTimer("Evaluation", false);
			timer.addTokens(ps);
			if (globalEval != null) {
				globalEval.evaluate(ps.getSentences(), chunkings,
						referenceChunks);
				// globalEvalMuc.evaluate(chunkings, referenceChunks);
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
		return eval;
	}

	private ArrayList<List<String>> loadFolds() throws IOException {
		ArrayList<List<String>> folds = new ArrayList<List<String>>();
		/** Wczytaj listy plików */
		File sourceFile = new File(LinerOptions.getGlobal().getOption(
				LinerOptions.OPTION_INPUT_FILE));
		String root = sourceFile.getParentFile().getAbsolutePath();
		BufferedReader bf = new BufferedReader(new InputStreamReader(
				new FileInputStream(sourceFile)));

		String line = bf.readLine();
		while (line != null) {
			String[] fileData = line.split("\t");
			String file = fileData[0];
			int fold = Integer.parseInt(fileData[1]);
			if (!file.startsWith("/")) {
				file = root + "/" + file;
			}
			while (folds.size() < (fold)) {
				folds.add(new ArrayList<String>());
			}
			folds.get(fold - 1).add(file);
			line = bf.readLine();
		}
		bf.close();

		return folds;

	}

	private String getTrainingSet(int fold, ArrayList<List<String>> folds) {
		StringBuilder sbtrain = new StringBuilder();

		for (int i = 0; i < folds.size(); i++) {
			if (i != fold)
				for (String line : folds.get(i))
					sbtrain.append(line + "\n");
		}
		return sbtrain.toString().trim();
	}

	private String getTestingSet(int fold, ArrayList<List<String>> folds) {
		StringBuilder sbtrain = new StringBuilder();
		for (String line : folds.get(fold))
			sbtrain.append(line + "\n");
		return sbtrain.toString().trim();
	}

}
