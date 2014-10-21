package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationTokenListComparator;
import g419.corpus.structure.Document;
import g419.liner2.cli.CommonOptions;
import g419.liner2.relations.evaluation.ParentEvaluator;
import g419.liner2.relations.evaluation.ParentEvaluator.AgpPronounAndZeroCriterion;
import g419.liner2.relations.evaluation.ParentEvaluator.NamedEntityCriterion;
import g419.liner2.relations.evaluation.ParentEvaluator.RelationUnitCriterion;

import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

public class ActionEvalParent extends Action {

	private String input_file = null;
    private String input_format = null;
	
	public ActionEvalParent() {
		super("eval-parent");
		this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
		CommandLine line = new GnuParser().parse(this.options, args);
		parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");

	}

	@Override
	public void run() throws Exception {
		
		RelationUnitCriterion identifyingUnitsCriterion = new NamedEntityCriterion();
		RelationUnitCriterion referencingUnitsCriterion = new AgpPronounAndZeroCriterion();
		Comparator<Annotation> matcher = new AnnotationTokenListComparator(true);
		ParentEvaluator evaluator = new ParentEvaluator(identifyingUnitsCriterion, referencingUnitsCriterion, matcher);
		
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
		Document referenceDocument = reader.nextDocument();
		Document systemResponseDocument = reader.nextDocument();
		
		evaluator.evaluate(systemResponseDocument, referenceDocument);

	}

}
