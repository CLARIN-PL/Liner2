package g419.spatial.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.chunker.IobberChunker;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.DocumentToSpatialExpressionConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ActionSpatialPatterns2 extends Action {

	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";

	private List<Pattern> annotationsPrep = Lists.newLinkedList();
	private List<Pattern> annotationsNg = Lists.newLinkedList();
	private List<Pattern> annotationsNp = Lists.newLinkedList();

	private String filename = null;
	private String inputFormat = null;

	private Logger logger = Logger.getLogger("ActionSpatial");

	private final String ngs = "(AdjG|NumG[rzbde]?|NGdata|NGadres|Ngg|Ngs|NG[agspbcnxk])";
	private final Pattern generaliseToPrepNG = Pattern.compile("Prep" + this.ngs);
	private final Pattern generaliseToNG = Pattern.compile(this.ngs);
	private final Pattern generaliseRepeatedNG = Pattern.compile("\\[NG\\]( \\[NG\\])");
	private final Pattern generaliseRepeatedPrepNG = Pattern.compile("\\[PrepNG\\]( \\[PrepNG\\])");
	private final Pattern generaliseVerb = Pattern.compile("pos=(fin|bedzie|aglt|praet|impt|imps|inf|pcon|pant|ger|pact|ppas)");
	private final Pattern generalisePrepNGtrajector = Pattern.compile("\\[PrepNG#TR\\]");
	private final Pattern generaliseToVerbfin = Pattern.compile("\\[(Ppas|Pact|Inf|Imps)\\]");
	private final Pattern generaliseIgnoreQubNum = Pattern.compile(" \\[pos=qub\\] \\[pos=num\\]");
	private final Pattern generaliseIgnoreQub = Pattern.compile(" \\[pos=qub\\]");
	private final Pattern generaliseVerbfinVerbfin = Pattern.compile("\\[Verbfin\\] \\[Verbfin\\]");
	private final Pattern generalisePrepXLandmark = Pattern.compile("\\[pos=prep\\] \\[pos=(num|adj|burk)\\] \\[NG#landmark\\]");
	private final Pattern generaliseIgnoreBrackets = Pattern.compile(" \\[orth=\\(\\]( \\[[^]]+\\])+ \\[orth=\\)\\]");

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
		return Option.builder(ActionSpatialPatterns2.OPTION_FILENAME).hasArg().argName("FILENAME").required()
				.desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();			
	}

	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionSpatialPatterns2.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
//		List<Pattern> annotationNG = new ArrayList<Pattern>();
//		annotationNG.add(Pattern.compile(".*NG"));
//		annotationNG.add(Pattern.compile("Verbfin"));
//		annotationNG.add(Pattern.compile("Ppas"));
//		annotationNG.add(Pattern.compile("Pact"));
//		annotationNG.add(Pattern.compile("Inf"));
//		annotationNG.add(Pattern.compile("Imps"));

		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);
		Document document;
		while ( (document = reader.nextDocument()) != null ){
			List<SpatialExpression> ses = converter.convert(document);
			ses.stream().map(r -> generatePattern(r)).forEach(System.out::println);
		}
		reader.close();
	}

	private String generatePattern(SpatialExpression se){
        generatePattern(se, null, null);
		return se.toString();
	}
	
	/**
	 * Tworzy wzorzec kontekstu zawierającego wszystkie wskazane anotacje.
	 * @param tokens
	 * @param anns
	 * @param tokenToNg
	 * @return
	 */
	public String generatePattern(SpatialExpression se, Map<String, Annotation> tokenToNg, Map<String, Annotation> tokenToNp){
		
		StringBuilder sb = new StringBuilder("PATTERN: ");
		List<Annotation> ans = Lists.newArrayList(se.getAnnotations());

		if ( ans.size() < 2 ){
			ans.stream().forEach(logger::warn);
			return null;
		}

        Sentence sentence = ans.get(0).getSentence();
        List<Token> tokens = sentence.getTokens();

        Integer firstToken = ans.stream().map(a -> a.getBegin()).min(Integer::compare).get();
		Integer lastToken = ans.stream().map(a -> a.getEnd()).max(Integer::compare).get();
		
		Map<String, Annotation> mentionIndex = this.makeAnnotationIndex(ans);
		
		Annotation lastNP = null;
		
		int i=firstToken;
		while ( i<=lastToken){
			String tokenHashI = "" + sentence.hashCode() + "#" + i;
			
			Annotation currentNP = tokenToNp.get(tokenHashI);
			if ( currentNP != lastNP && lastNP != null ){
				sb.append(">");
			}
			sb.append(" ");
			if ( currentNP != lastNP && currentNP != null ){
				sb.append("<");
			}
			lastNP = currentNP;			
			
			
			Annotation group = tokenToNg.get(tokenHashI);
			String tokenStr = "";
			String mentionStr = "";
			
			if ( group != null ){
				// Jeżeli grupa rozpoczyna się przed begin, to ustaw begin na początek anotacji group
				firstToken = Math.min(firstToken, group.getBegin());
				
				tokenStr += group.getType();
				int j = i;
				int mentionCount = 0;
				while ( j<=group.getEnd() ){
					String tokenHashJ = "" + sentence.hashCode() + "#" + j;
					Annotation mention = mentionIndex.get(tokenHashJ);
					if ( mentionStr.length() > 0 ){
						mentionStr += "_";
					}
					if ( mention != null ){
						mentionStr += "{" + mention.getType() + "}";
						j = mention.getEnd();
						mentionCount++;
					}else{
						mentionStr += tokens.get(j).getDisambTag().getBase();
					}					
					j++;					
				}
				if ( mentionCount == 0 ){
					// Zresetuje mentionStr gdy żadna anotacja mention nie znajduje się wewnątrz anotacji group
					mentionStr = "";
				}
				i = Integer.max(i, group.getEnd());
			}else{
				if ( tokens.get(i).getDisambTag().getPos().equals("interp") ){
					tokenStr += "orth=" + tokens.get(i).getOrth();
				}else{
					tokenStr += "pos=" + tokens.get(i).getDisambTag().getPos();
				}
				Annotation mention = mentionIndex.get(tokenHashI);
				if ( mention != null ){
					mentionStr = mention.getType();
				}
			}
			
			if ( mentionStr.length() > 0 ){
				tokenStr += "#" + mentionStr.replace("trajector", "TR").replace("landmark", "LM");
			}						
			
			i++;
			sb.append("[" + tokenStr + "]");
		}
		if ( lastNP != null ){
			sb.append(">");
		}

		sb.append("\t	*** ");

		for ( int t=firstToken; t<=lastToken; t++ ){
			Token tok = tokens.get(t);
			sb.append(tok.getOrth());
			if ( !tok.getNoSpaceAfter() ){
				sb.append(" ");
			}
		}

		sb.append("\t *** ");

		for ( int t=firstToken; t<=lastToken; t++ ){
			Token tok = tokens.get(t);
			sb.append(String.format("%s:%s", tok.getOrth(), tok.getDisambTag().getPos()));
			sb.append(" ");
		}

		return sb.toString().trim();
	}
	
	/**
	 * Uogólnia postać wzorca
	 * @param pattern
	 * @return
	 */
	public String generalisePattern(String pattern){
		String genPattern = pattern;
		genPattern = this.generaliseToPrepNG.matcher(genPattern).replaceAll("PrepNG");
		genPattern = this.generaliseToNG.matcher(genPattern).replaceAll("NG");
		genPattern = this.generaliseRepeatedNG.matcher(genPattern).replaceAll("[NG]");
		genPattern = this.generaliseRepeatedPrepNG.matcher(genPattern).replaceAll("[PrepNG]");
		genPattern = this.generaliseVerb.matcher(genPattern).replaceAll("pos=@verb");
		//genPattern = this.generaliseToVerbfin.matcher(genPattern).replaceAll("[Verbfin]");
		genPattern = this.generalisePrepNGtrajector.matcher(genPattern).replaceAll("[NG#TR]");
		genPattern = this.generaliseIgnoreQubNum.matcher(genPattern).replaceAll("");
		genPattern = this.generaliseIgnoreQub.matcher(genPattern).replaceAll("");
		genPattern = this.generaliseVerbfinVerbfin.matcher(genPattern).replaceAll("[Verbfin]");
		genPattern = this.generalisePrepXLandmark.matcher(genPattern).replaceAll("[PrepNG#landmark]");
		genPattern = this.generaliseIgnoreBrackets.matcher(genPattern).replaceAll("");
		return genPattern;
	}

	/**
	 * 
	 * @param anns
	 * @return
	 */
	public Map<String, Annotation> makeAnnotationIndex(List<Annotation> anns){
		Map<String, Annotation> annotationIndex = new HashMap<String, Annotation>();
		for ( Annotation an : anns ){
			for ( int i=an.getBegin(); i<=an.getEnd(); i++){
				String hash = "" + an.getSentence().hashCode() + "#" + i;
				if ( annotationIndex.get(hash) != null ) {
					if ( annotationIndex.get(hash).getTokens().size() < an.getTokens().size() ){
						annotationIndex.remove(hash);
					}
				} else{
					annotationIndex.put(hash, an);
				}
			}
		}
		return annotationIndex;
	}
	
}
