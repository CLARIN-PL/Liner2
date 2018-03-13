package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.pattern.AnnotationPatternGenerator;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.DocumentToSpatialExpressionConverter;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;
import io.vavr.control.Try;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionSpatialPatterns2 extends Action {

	private Logger logger = LoggerFactory.getLogger(getClass());
	final private AnnotationPatternGenerator generator = new AnnotationPatternGenerator();
	private String filename = null;
	private String inputFormat = null;

	private final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

	public ActionSpatialPatterns2() {
		super("spatial-patterns2");
		setDescription("new implementation of pattern generator for spatial expressions (includes static and dynamic expressions)");
		options.addOption(this.getOptionInputFilename());
		options.addOption(CommonOptions.getInputFileFormatOption());
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(CommonOptions.OPTION_INPUT_FILE).hasArg().argName("path").required()
				.desc("path to the input file").longOpt(CommonOptions.OPTION_INPUT_FILE_LONG).build();
	}

	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        filename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
	    List<String> patterns = Lists.newArrayList();
	    try (AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat)) {
            while (reader.hasNext()) {
                Document document = reader.nextDocument();
                List<SpatialExpression> ses = converter.convert(document);
                ses.stream().map(r -> generatePattern(r)).forEach(patterns::add);
            }
        }
        patterns.stream().forEach(System.out::println);
	}

	/**
	 * Tworzy wzorzec kontekstu zawierającego wszystkie wskazane anotacje.
     * @param se
	 * @return
	 */
	private String generatePattern(SpatialExpression se){
		List<Annotation> annotations = Lists.newArrayList(se.getAnnotations());
		String str = "";
		if ( annotations.size() > 0 ){
		    try {
                str = generator.generate(annotations.get(0).getSentence(), annotations);
            } catch (Exception ex){
		        logger.warn("Failed to generate pattern for spatial expression: {}", se, ex);
            }
        }
		return str;
	}

}
