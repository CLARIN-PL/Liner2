package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.DocumentToSpatialExpressionConverter;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionSpatialPatterns2 extends Action {

	private List<Pattern> annotationsPrep = Lists.newLinkedList();
	private List<Pattern> annotationsNg = Lists.newLinkedList();
	private List<Pattern> annotationsNp = Lists.newLinkedList();

	private String filename = null;
	private String inputFormat = null;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final String ngs = "(AdjG|NumG[rzbde]?|NGdata|NGadres|Ngg|Ngs|NG[agspbcnxk])";

	private final Set<String> spejdTypesToReplace = Sets.newHashSet("PrepNG", "NG", "Verbfin", "Ppas", "Pact", "Inf", "Imps");

	private final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

	public ActionSpatialPatterns2() {
		super("spatial-patterns2");
		this.setDescription("new implementation of pattern generator for spatial expressions (includes static and dynamic expressions)");
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		
		this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
		this.annotationsNg.add(Pattern.compile("^NG.*"));		
		this.annotationsNp.add(Pattern.compile("chunk_np"));
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
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        filename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);
		Document document;
		while ( (document = reader.nextDocument()) != null ){
			List<SpatialExpression> ses = converter.convert(document);
			ses.stream().map(r -> generatePattern(r)).forEach(System.out::println);
		}
		reader.close();
	}

	/**
	 * Tworzy wzorzec kontekstu zawierającego wszystkie wskazane anotacje.
     * @param se
	 * @return
	 */
	public String generatePattern(SpatialExpression se){
		StringBuilder sb = new StringBuilder("PATTERN: ");
		List<Annotation> ans = Lists.newArrayList(se.getAnnotations());

		if ( ans.size() < 2 ){
			ans.stream().map(an->an.toString()).forEach(logger::warn);
			return null;
		}

        Sentence sentence = ans.get(0).getSentence();
        List<Token> tokens = sentence.getTokens();
        SentenceAnnotationIndexTypePos spejdChunks = new SentenceAnnotationIndexTypePos(sentence);

        if (!ans.stream().filter(an->an.getSentence()!=sentence).collect(Collectors.toList()).isEmpty()){
            logger.warn("Expressions has annotations assigned to different sentences: {}", se);
            return null;
        }

        Integer firstToken = ans.stream().map(a -> a.getBegin()).min(Integer::compare).get();
		Integer lastToken = ans.stream().map(a -> a.getEnd()).max(Integer::compare).get();
		
        int i=firstToken;
        while (i<=lastToken){
            sb.append(String.format("[pos=%s]", tokens.get(i).getDisambTag().getPos()));
            i++;
        }

		return sb.toString().trim();
	}

}
