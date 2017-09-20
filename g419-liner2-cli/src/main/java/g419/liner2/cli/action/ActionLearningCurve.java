package g419.liner2.cli.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

/**
 * TODO
 * 
 * @author Jan Kocoń
 */
public class ActionLearningCurve extends Action {

	private String input_file = null;
	private String input_format = null;

	public ActionLearningCurve() {
		super("curve");
		this.options.addOption(CommonOptions.getModelFileOption());
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(CommonOptions.getInputFileNameOption());

	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
		CommandLine line = new DefaultParser().parse(this.options, args);
		parseDefault(line);
		this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
		this.input_format = line.getOptionValue(
				CommonOptions.OPTION_INPUT_FORMAT, "ccl");
		LinerOptions.getGlobal().parseModelIni(
				line.getOptionValue(CommonOptions.OPTION_MODEL));
	}

	public void run() throws Exception {
		TokenFeatureGenerator gen = null;
		ChunkerEvaluator globalEval = new ChunkerEvaluator(
				LinerOptions.getGlobal().types, true);
		ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(
				LinerOptions.getGlobal().types);

		System.out.print("Annotations to evaluate:");
		if (LinerOptions.getGlobal().types.isEmpty()) {
			System.out.print(" all");
		} else {
			for (Pattern pattern : LinerOptions.getGlobal().types)
				System.out.print(" " + pattern);
		}
		System.out.println();
		LinerOptions.getGlobal()
				.setCVDataFormat(this.input_format.substring(3));
		int repeats = 5;
		float t = 0.2f;
		int parts = 20;
		ArrayList<String> files = loadFiles();
		int filesNumber = files.size();
		int testMaxIndex = (int) (t * filesNumber);
		for (int i = 0; i < repeats; i++) {
			Collections.shuffle(files, new Random(System.nanoTime()));
			StringBuilder testSetB = new StringBuilder();
			for (int j = 0; j < testMaxIndex; j++)
				testSetB.append(files.get(j) + "\n");
			int part = (filesNumber - testMaxIndex) / parts;
			StringBuilder trainSetB = new StringBuilder();
			for (int p = 0; p < parts; p++) {
				for (int j = p * part; j < (p + 1) * part; j++)
					trainSetB.append(files.get(j + testMaxIndex) + "\n");
				LinerOptions.getGlobal().setCVTrainData(
						trainSetB.toString().trim());
				AbstractDocumentReader reader = new BatchReader(
						IOUtils.toInputStream(testSetB.toString().trim(), "UTF-8"), "",
						this.input_format.substring(3));
				evaluate(reader, gen, globalEval, globalEvalMuc);
			}
		}
	}

	private void evaluate(AbstractDocumentReader dataReader,
			TokenFeatureGenerator gen, ChunkerEvaluator globalEval,
			ChunkerEvaluatorMuc globalEvalMuc) throws Exception {
		ProcessingTimer timer = new ProcessingTimer();
		timer.startTimer("Model loading");
		ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
		Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal()
				.getOptionUse());
		if (!LinerOptions.getGlobal().features.isEmpty()) {
			gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
		}
		timer.stopTimer();

		/* Create all defined chunkers. */
		ChunkerEvaluator eval = new ChunkerEvaluator(
				LinerOptions.getGlobal().types);
		ChunkerEvaluatorMuc evalMuc = new ChunkerEvaluatorMuc(
				LinerOptions.getGlobal().types);

		timer.startTimer("Data reading");
		Document ps = dataReader.nextDocument();
		timer.stopTimer();

		Map<Sentence, AnnotationSet> chunkings = null;
		while (ps != null) {

			/* Get reference set of annotations */
			Map<Sentence, AnnotationSet> referenceChunks = ps
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

	private ArrayList<String> loadFiles() throws IOException,
			DataFormatException {
		ArrayList<String> folds = new ArrayList<String>();
		File sourceFile = new File(this.input_file);
		String root = sourceFile.getParentFile().getAbsolutePath();
		BufferedReader bf = new BufferedReader(new InputStreamReader(
				new FileInputStream(sourceFile)));
		String line = bf.readLine();
		while (line != null) {
			String file = line.trim();
			if (!file.startsWith("/"))
				file = root + "/" + file;
			folds.add(file);
			line = bf.readLine();
		}
		bf.close();
		return folds;
	}

}
