package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationTokenListComparator;
import g419.corpus.structure.Document;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.evaluation.ParentEvaluator;
import g419.crete.api.refine.CoverAnnotationDocumentRefiner;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.Action;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;


public class ActionEvalParent extends Action {

	public static final String IDENTIFYING_SELECTOR = "identifying_selector";
	public static final String REFERENCING_SELECTOR = "referencing_selector";
	public static final String NO_ANNOTATION_SELECTOR = "no_annotation_selector";

	private String input_file = null;

	public ActionEvalParent() {
		super("eval-parent");
		this.options.addOption(CommonOptions.getInputFileNameOption());
		this.options.addOption(CommonOptions.getModelFileOption());
    }

	@Override
	public void parseOptions(String[] args) throws Exception {
		CommandLine line = new DefaultParser().parse(this.options, args);
		parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        CreteOptions.getOptions().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}

	private String getAbsolutePath(String path, String confPath){
		if(!path.startsWith(".")) return path;
		return confPath.substring(0, confPath.lastIndexOf("/")) + path.substring(1);
	}
	
	private String[] getBatchFiles() throws IOException{
		String[] files = new String[2];
		BufferedReader ir = new BufferedReader(new InputStreamReader(new FileInputStream(this.input_file)));
		files[0] = ir.readLine();
		files[1] = ir.readLine();
		return files;
	}

	@Override
	public void run() throws Exception {
		// Input preprocessing
		String[] goldSysFiles = getBatchFiles();
		String goldInputFile = getAbsolutePath(goldSysFiles[0].split(";")[0], this.input_file);
		String goldInputFormat = goldSysFiles[0].split(";")[1];
		String sysInputFile =  getAbsolutePath(goldSysFiles[1].split(";")[0], this.input_file);
		String sysInputFormat =  goldSysFiles[1].split(";")[1];
		boolean sysTEI = sysInputFormat.contains("tei");

		// Readers
		AbstractDocumentReader goldReader = ReaderFactory.get().getStreamReader(goldInputFile, goldInputFormat);
		AbstractDocumentReader sysReader = ReaderFactory.get().getStreamReader(sysInputFile, sysInputFormat);
		Document referenceDocument = goldReader.nextDocument();
		Document systemResponseDocument = sysReader.nextDocument();

		// Token features
		LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();
		features.put("orth", "orth");
		features.put("ctag", "ctag");
		features.put("base", "base");
		features.put("pos", "pos");
		TokenFeatureGenerator gen = new TokenFeatureGenerator(features);

		// Annotation comparator
		// TODO: refactor (Factory)
		Comparator<Annotation> matcher = new AnnotationTokenListComparator(!sysTEI); // for ccl -> true, for tei -> false

		// Selectors
		AbstractAnnotationSelector identifyingSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(IDENTIFYING_SELECTOR));
		AbstractAnnotationSelector referencingSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(REFERENCING_SELECTOR));

		// Refiner
		CoverAnnotationDocumentRefiner refiner = new CoverAnnotationDocumentRefiner(AnnotationSelectorFactory.getFactory().getInitializedSelector(NO_ANNOTATION_SELECTOR));

		// Evaluator
		ParentEvaluator evaluator = new ParentEvaluator(identifyingSelector, referencingSelector, matcher);

		// Loop over all document pairs and evaluate
		while(referenceDocument != null && systemResponseDocument != null){
//			if(!referenceDocument.getName().equals(systemResponseDocument.getName())){throw new NullPointerException();}

			// Generate features
			gen.generateFeatures(referenceDocument);
			gen.generateFeatures(systemResponseDocument);

			// Refine documents
			referenceDocument = refiner.refineDocument(referenceDocument);
			systemResponseDocument = refiner.refineDocument(systemResponseDocument);

			// Evaluate
			evaluator.evaluate(systemResponseDocument, referenceDocument);

			// Read next pair of documents
			referenceDocument = goldReader.nextDocument();
			systemResponseDocument = sysReader.nextDocument();
		}

		// Print the final result
		System.out.println("");
		evaluator.printTotal();
	}

}

