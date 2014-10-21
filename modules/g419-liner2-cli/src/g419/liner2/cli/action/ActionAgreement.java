package g419.liner2.cli.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import g419.corpus.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ChunkerEvaluator;
import g419.liner2.api.tools.ChunkerEvaluatorMuc;
import g419.liner2.api.tools.ProcessingTimer;
import g419.liner2.cli.CommonOptions;

public class ActionAgreement extends Action {

	boolean debug_flag = false;

	private String[] input_files = null;
	private String input_format = null;

	public ActionAgreement() {
		super("agreement");
		this.setDescription("checks agreement (of annotations) between suplied documents");
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(CommonOptions.getInputFileNameOption());
		this.options.addOption(CommonOptions.getModelFileOption());
		this.options.addOption(CommonOptions.getVerboseDeatilsOption());

	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
		CommandLine line = new GnuParser().parse(this.options, args);
		parseDefault(line);
		input_files = line.getOptionValues(CommonOptions.OPTION_INPUT_FILE);

		this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
		LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
		if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
			Logger.verboseDetails = true;
		}

	}

	@Override
	public void run() throws Exception {
		ResultHolder results = new ResultHolder();

		if (this.input_format.startsWith("batch:")) {

			this.input_format = this.input_format.substring(6);

			NumberMixer numbers = new NumberMixer(input_files.length);
			for (NumberPair pair : numbers) {
				AbstractDocumentReader originalDocument = new BatchReader(IOUtils.toInputStream(getBatch(pair.first)), "", this.input_format);
				AbstractDocumentReader referenceDocument = new BatchReader(IOUtils.toInputStream(getBatch(pair.second)), "", this.input_format);
				compare(originalDocument, referenceDocument, results);
			}
		} else {

			NumberMixer numbers = new NumberMixer(input_files.length);
			for (NumberPair pair : numbers) {
				AbstractDocumentReader originalDocument = ReaderFactory.get().getStreamReader(input_files[pair.first], this.input_format);
				AbstractDocumentReader referenceDocument = ReaderFactory.get().getStreamReader(input_files[pair.second], this.input_format);
				compare(originalDocument, referenceDocument, results);
			}
		}

		// Print final results
		results.printResults();
	}

	public void compare(AbstractDocumentReader dataReader, AbstractDocumentReader referenceDataReader, ResultHolder results) throws Exception {

		ProcessingTimer timer = new ProcessingTimer();

		/* Create all defined chunkers. */
		ChunkerEvaluator eval = new ChunkerEvaluator(LinerOptions.getGlobal().types);

		timer.startTimer("Data reading");
		Document originalDocument = dataReader.nextDocument();
		Document referenceDocument = referenceDataReader.nextDocument();
		timer.stopTimer();

		TranslatedChunkings translatedChunkings;
		while (originalDocument != null && referenceDocument != null) {
			try {
				/* Get set of annotations (original and translated), meanwhile checking if documents are the same */
				translatedChunkings = getTranslatedChunkings(originalDocument, referenceDocument);	
				
				/* Evaluate */
				eval.evaluate(originalDocument, translatedChunkings.original, translatedChunkings.translated);
			} catch (Exception e) {
				System.out.println("Documents do not match!");
				throw e;
			}

			timer.startTimer("Data reading");
			originalDocument = dataReader.nextDocument();
			referenceDocument = referenceDataReader.nextDocument();
			timer.stopTimer();
		}

		// Submit results to holder
		results.submitResult(eval);
		// eval.printResults();
		timer.printStats();
	}

	private String getBatch(int index) throws IOException {
		File sourceFile = new File(this.input_files[index]);
		String root = sourceFile.getParentFile().getAbsolutePath();
		StringBuffer outputBuffer = new StringBuffer();
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

		String line;
		while ((line = inputReader.readLine()) != null) {
			line = line.startsWith("/") ? line : root + "/" + line;
			outputBuffer.append(line).append('\n');
		}
		inputReader.close();

		return outputBuffer.toString();
	}

	private static class TranslatedChunkings {
		final HashMap<Sentence, AnnotationSet> original, translated;

		public TranslatedChunkings(HashMap<Sentence, AnnotationSet> original, HashMap<Sentence, AnnotationSet> translated) {
			this.original = original;
			this.translated = translated;
		}
	}

	private TranslatedChunkings getTranslatedChunkings(Document original, Document reference) {
		// Keep original set of annotations
		HashMap<Sentence, AnnotationSet> originalChunkings = original.getChunkings();

		// Peel all original annotations from original document (the translated annotations will be placed instead)
		original.removeAnnotations();

		Iterator<Paragraph> originalParagraphs = original.getParagraphs().iterator();
		Iterator<Paragraph> referenceParagraphs = reference.getParagraphs().iterator();

		// Translate each paragraph
		while (originalParagraphs.hasNext() && referenceParagraphs.hasNext()) {
			translateParagraphs(originalParagraphs.next(), referenceParagraphs.next());
		}

		// If anything is left in original or reference iterator than the number of paragraphs do not match
		if (originalParagraphs.hasNext() || referenceParagraphs.hasNext())
			throw new RuntimeException("Number of paragraphs do not match.");

		return new TranslatedChunkings(originalChunkings, original.getChunkings());
	}
	
	/* 
	 * To have it working that way sentences must have UIDs to distinguish them, plus the evaluation method must
	 * use those UIDs instead of sentence hashes from hash set.
	 * 
	private Document translateDocument(Document original, Document reference)
	{
		Document translated = original.clone();
		
		// Peel all original annotations from translated document (the translated annotations will be placed instead)
		translated.removeAnnotations();
		
		Iterator<Paragraph> translatedParagraphs = translated.getParagraphs().iterator();
		Iterator<Paragraph> referenceParagraphs = reference.getParagraphs().iterator();
		
		// Translate each paragraph
		while (translatedParagraphs.hasNext() && referenceParagraphs.hasNext()) {
			translateParagraphs(translatedParagraphs.next(), referenceParagraphs.next());
		}

		// If anything is left in translated or reference iterator than the number of paragraphs do not match
		if (translatedParagraphs.hasNext() || referenceParagraphs.hasNext())
			throw new RuntimeException("Number of paragraphs do not match.");
		
		return translated;
	}
	*/

	private void translateParagraphs(Paragraph translated, Paragraph reference) {
		Iterator<Sentence> originalSentences = translated.getSentences().iterator();
		Iterator<Sentence> referenceSentences = reference.getSentences().iterator();

		// Translate each sentence
		while (originalSentences.hasNext() && referenceSentences.hasNext()) {
			translateSentence(originalSentences.next(), referenceSentences.next());
		}

		// If anything is left in original or reference iterator than the number of sentences do not match
		if (originalSentences.hasNext() || referenceSentences.hasNext())
			throw new RuntimeException("Number of sentences in paragraph do not match.");
	}

	private void translateSentence(Sentence translated, Sentence reference) {
		if (!compareSentences(translated, reference))
			throw new RuntimeException("Sentences do not match.");

		// Create new AnnotationSet for proper sentence (the translated one reference -> original)
		AnnotationSet translatedAnnotationSet = new AnnotationSet(translated);

		// Get annotations to translate
		LinkedHashSet<Annotation> referenceAnnotations = reference.getChunks();

		// Translate each annotation to original sentence and put them into created AnnotationSet
		for (Annotation oldAnnotation : referenceAnnotations)
			translatedAnnotationSet.addChunk(new Annotation(oldAnnotation.getBegin(), oldAnnotation.getEnd(), oldAnnotation.getType(), translated));

		// Attach AnnotationSet to sentence
		translated.addAnnotations(translatedAnnotationSet);

	}

	private boolean compareSentences(Sentence firstSentence, Sentence secondSentence) {
		ArrayList<Token> firstSentenceTokens = firstSentence.getTokens();
		ArrayList<Token> seconsSentenceTokens = secondSentence.getTokens();

		boolean match = firstSentenceTokens.size() == seconsSentenceTokens.size();
		for (int i = 0; match && i < firstSentenceTokens.size() && i < seconsSentenceTokens.size(); i++)
			match &= compareTokens(firstSentenceTokens.get(i), seconsSentenceTokens.get(i));

		return match;
	}

	private boolean compareTokens(Token firstToken, Token secondToken) {
		int firstTokenAttributesNo = firstToken.getNumAttributes();
		int secondTokenAttributesNo = secondToken.getNumAttributes();

		boolean match = (firstTokenAttributesNo == secondTokenAttributesNo) && (firstToken.getNoSpaceAfter() == secondToken.getNoSpaceAfter());
		for (int i = 0; match && i < firstTokenAttributesNo && i < secondTokenAttributesNo; i++)
			match &= firstToken.getAttributeValue(i).equals(secondToken.getAttributeValue(i));

		return match;
	}

	/*
	 * Holds info about evaluation results from all data sets.
	 */
	private static class ResultHolder {
		int evaluationNumber;
		float precision, spanPrecision, recall, spanRecall, fMeasure, spanFMeasure;
		int truePositive, falsePositive, falseNegative;

		public ResultHolder() {
			precision = spanPrecision = recall = spanRecall = fMeasure = spanFMeasure = 0.0f;
			evaluationNumber = truePositive = falsePositive = falseNegative = 0;
		}

		public void submitResult(ChunkerEvaluator eval) {
			evaluationNumber++;

			precision += eval.getPrecision();
			spanPrecision += eval.getSpanPrecision();
			recall += eval.getRecall();
			spanRecall += eval.getSpanRecall();
			fMeasure += eval.getFMeasure();
			spanFMeasure += eval.getSpanFMeasure();

			truePositive += eval.getTruePositive();
			falsePositive += eval.getFalsePositive();
			falseNegative += eval.getFalseNegative();
		}

		public float getPrecision() {
			return precision / evaluationNumber;
		}

		public float getSpanPrecision() {
			return spanPrecision / evaluationNumber;
		}

		public float getRecall() {
			return recall / evaluationNumber;
		}

		public float getSpanRecall() {
			return spanRecall / evaluationNumber;
		}

		public float getFMeasure() {
			return fMeasure / evaluationNumber;
		}

		public float getSpanFMeasure() {
			return spanFMeasure / evaluationNumber;
		}

		public int getTruePositive() {
			return truePositive;
		}

		public int getFalsePositive() {
			return falsePositive;
		}

		public int getFalseNegative() {
			return falseNegative;
		}

		public void printResults() {
			String header = "        Annotation           &   TP &   FP &   FN & Precision & Recall  & F$_1$   \\\\";
			String line = "        %-20s & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\";

			this.printHeader("Exact match evaluation -- annotation span and types evaluation");
			System.out.println(header);
			System.out.println("\\hline");
			ArrayList<String> keys = new ArrayList<String>();
			// /
			System.out.println("\\hline");
			System.out.println(String.format(line, "*TOTAL*", this.getTruePositive(), this.getFalsePositive(), this.getFalseNegative(), this.getPrecision() * 100,
					this.getRecall() * 100, this.getFMeasure() * 100));
			System.out.println("\n");

			// this.printHeader("Annotation span evaluation (annotation types are ignored)");
			// System.out.println(header);
			// System.out.println("\\hline");
			// System.out.println(String.format(line, "*TOTAL*",
			// this.globalTruePositivesRangeOnly, this.globalFalsePositivesRangeOnly, this.globalFalseNegativesRangeOnly,
			// this.getSpanPrecision()*100, this.getSpanRecall()*100, this.getSpanFMeasure()*100));
			// System.out.println("\n");
		}

		public void printHeader(String header) {
			System.out.println("======================================================================================");
			System.out.println("# " + header);
			System.out.println("======================================================================================");
		}
	}

	/*
	 * Holds information (indexes) about which two inputs should be compared.
	 */
	private class NumberPair {
		public final int first, second;

		public NumberPair(int first, int second) {
			this.first = first;
			this.second = second;
		}
	}

	/* Mixes input data (indexes) in such way that each document or batch is compared with others.
	 * Eliminates redundancy (situation in which two data sets have been compared in different order)
	 * ie. (a,b) has been checked already, so (b,a) is omitted.
	 */
	private class NumberMixer implements Iterable<NumberPair>, Iterator<NumberPair> {
		int firstIndex, secondIndex, max;

		public NumberMixer(int max) {
			if (max < 2)
				throw new RuntimeException("Cannot check agreement, too few documents to compare.");

			this.max = max;
			secondIndex = 0;
			firstIndex = 0;
		}

		@Override
		public Iterator<NumberPair> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return max - firstIndex > 2 || max - secondIndex > 1;
		}

		@Override
		public NumberPair next() {

			if (++secondIndex == max)
				secondIndex = ++firstIndex + 1;

			return new NumberPair(firstIndex, secondIndex);
		}

		@Override
		public void remove() {
			// Dummy, no removing
		}
	}

}
