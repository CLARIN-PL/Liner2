package g419.spatial.action;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.action.Action;
import g419.spatial.io.ConllDocumentReader;
import g419.spatial.structure.NodeToken;

public class ActionTest extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private Set<String> verbs = new HashSet<String>(); 
	private Set<String> object_poses = new HashSet<String>();
	private Set<String> verb_instytucje = null;
	
	private String filename = null;

	public ActionTest() {
		super("test");
		this.setDescription("query conll corpus");
		this.options.addOption(this.getOptionInputFilename());
		
		this.verbs.add("fin");
		this.verbs.add("praet");
		this.verbs.add("pact");
		
		this.object_poses.add("subst");
		this.object_poses.add("ign");
		
		// ToDo: przerobić na parametr 
		String wordnet = "/nlp/resources/plwordnet/plwordnet_2_1_0/plwordnet_2_1_0_pwn_format";
		//Wordnet w = new Wordnet(wordnet);
		//this.verb_instytucje = w.getHyponymWords("instytucja", 1);
		//this.verb_instytucje.addAll(w.getHyponymWords("konstrukcja", 1));
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(OPTION_FILENAME).longOpt(OPTION_FILENAME_LONG)
				.hasArg().argName("filename").required().desc("path to the input file").build();
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionTest.OPTION_FILENAME);
    }

	@Override
	public void run() throws Exception {
		System.out.println(this.filename);
		InputStream stream = new FileInputStream(this.filename);
		ConllDocumentReader reader = new ConllDocumentReader(stream);
		Document document = reader.nextDocument();
		reader.close();
		
		for (Paragraph paragraph : document.getParagraphs()){
			for (Sentence sentence : paragraph.getSentences()){
				if ( this.matchSentenceWithPredSubLoc(sentence) ){
					this.printSentence(sentence);
					System.out.println();
				}
			}
		}
	}
	
	public boolean matchSentenceWithPredSubLoc(Sentence sentence){
		boolean found = false;
		List<Token> tokens = sentence.getTokens();
		for ( Token token : sentence.getTokens() ){
			NodeToken nodeToken = (NodeToken) token;
			boolean subj = false;
			boolean obj = false;
			boolean loc = false;
			for ( NodeToken t : nodeToken.getChildren() ){
				if ( t.getAttributeValue("relation").equals("obj")) {
					obj = true;
				}
				else if ( t.getAttributeValue("relation").equals("subj")) {
					subj = true;
				}
				else if ( (t.getAttributeValue("relation").equals("comp") || t.getAttributeValue("relation").equals("adjunct")) 
							&& t.getAttributeValue("ctag").contains(":loc")){
					loc = true;
				}
			}
			if ( subj && obj && !loc){
				found = true;
				System.out.println("Pred: " + token.getAttributeValue("base"));
			}
		}
		return found;
	}
	
	public void matchSentence(Sentence sentence){
		for ( Token token : sentence.getTokens() ){
			NodeToken nodeToken = (NodeToken) token;
			if (token.getAttributeValue("pos2").equals("prep")
					&& nodeToken.getParent() != null
					&& this.object_poses.contains(nodeToken.getParent().getAttributeValue("pos2"))
					&& this.verb_instytucje.contains(nodeToken.getParent().getAttributeValue("base"))){
				Set<NodeToken> tokens = new HashSet<NodeToken>();
				tokens.add(nodeToken);
				tokens.add(nodeToken.getParent());
//				if ( nodeToken.getParent() != null
//						&& this.verbs.contains(nodeToken.getParent().getAttributeValue("pos2"))){
//					for ( NodeToken t : nodeToken.getParent().getChildren() )
//						if ( t.getAttributeValue("relation").equals("subj"))
//							tokens.add(t);
//				}
				tokens.addAll(nodeToken.getChildren());
				this.printSentence(sentence);
				System.out.println();
			}
				
		}
	}
	
	public void printSentence(Sentence sentence){
		int tokenIndex = 1;
		List<Token> tokens = sentence.getTokens();
		for ( Token token : sentence.getTokens() ){
			if ( tokens.contains(token))
				System.out.println(String.format("%2d\t%-15s\t%-6s\t%-2s\t%-10s\t%s", 
						tokenIndex, 
						token.getAttributeValue("orth"), 
						token.getAttributeValue("pos2"),
						token.getAttributeValue("parent"), 
						token.getAttributeValue("relation"),
						token.getAttributeValue("ctag")));
			tokenIndex++;
		}
	}

}
