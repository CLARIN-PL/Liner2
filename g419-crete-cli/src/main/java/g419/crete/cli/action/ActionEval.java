package g419.crete.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.annotation.comparator.factory.AnnotationComparatorFactory;
import g419.crete.api.annotation.mapper.AnnotationMapper;
import g419.crete.api.evaluation.IEvaluator;
import g419.crete.api.evaluation.factory.EvaluatorFactory;
import g419.crete.api.refine.CoverAnnotationDocumentRefiner;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;


public class ActionEval extends Action {

	public static final String EVALUATOR = "evaluator";
	public static final String PRE_FILTER_SELECTOR = "prefilter_selector";
	public static final String MAPPER_SELECTOR = "mapper_selector";
	public static final String MATCHER = "matcher";


	private String input_file = null;

	public ActionEval() {
		super("eval");
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

	private void verifyPath(Document referenceDocument, Document systemDocument) throws InputMismatchException{
		Path referencePath = Paths.get(referenceDocument.getName());
		Path systemPath = Paths.get(systemDocument.getName());

//		if(!referencePath.getFileName().equals(systemPath.getFileName())) throw new InputMismatchException();
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
		Comparator<Annotation> matcher = AnnotationComparatorFactory.getFactory().getComparator(CreteOptions.getOptions().getProperties().getProperty(MATCHER));
		AnnotationMapper mapper = new AnnotationMapper(
				matcher,
				AnnotationSelectorFactory.getFactory().getInitializedSelector(
						CreteOptions.getOptions().getProperties().getProperty(MAPPER_SELECTOR)
				)
		);




		// Refiner new PatternAnnotationSelector(new String[]{"anafora_wyznacznik", "anafora_verb_null.*"})
		CoverAnnotationDocumentRefiner refiner = new CoverAnnotationDocumentRefiner(
				AnnotationSelectorFactory.getFactory().getInitializedSelector(
						CreteOptions.getOptions().getProperties().getProperty(PRE_FILTER_SELECTOR)
				)
		);

		// Evaluator
		IEvaluator evaluator = EvaluatorFactory.getFactory().getEvaluator(CreteOptions.getOptions().getProperties().getProperty(EVALUATOR), mapper);

		// Loop over all document pairs and evaluate
		while(referenceDocument != null && systemResponseDocument != null){
			verifyPath(referenceDocument,systemResponseDocument);
//			if(!referenceDocument.getName().equals(systemResponseDocument.getName())){throw new NullPointerException();}

			// Generate features
			gen.generateFeatures(referenceDocument);
			gen.generateFeatures(systemResponseDocument);

			// Refine documents
			referenceDocument = refiner.refineDocument(referenceDocument);
			systemResponseDocument = refiner.refineDocument(systemResponseDocument);

			// Evaluate

			try {
				evaluator.evaluate(systemResponseDocument, referenceDocument);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			// Read next pair of documents
			referenceDocument = goldReader.nextDocument();
			systemResponseDocument = sysReader.nextDocument();
		}

		// Print the final result
		System.out.println("");
		evaluator.printTotal();
	}

}

