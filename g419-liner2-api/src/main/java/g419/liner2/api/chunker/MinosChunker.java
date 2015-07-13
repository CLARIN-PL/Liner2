package g419.liner2.api.chunker;

import g419.corpus.io.writer.ConllStreamWriter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.Node;


/**
 * Null verb mention detector.
 * @author Adam Kaczmarek
 * 
 */
public class MinosChunker extends Chunker {
	
	private Document document = null;
	public final static String Annotation = "wyznacznik_null_verb";
	public final static String NegativeAnnotation = "wyznacznik_notnull_verb";
	public final static String MaltModel = "skladnica_liblinear_stackeager_final.mco";
	protected final String nonSubjectFile;
	protected final String nonSubjectReflexiveFile;
	private MaltParserService maltService = null;
	
	public final static String OPTION_TYPE = "type";
	public final static String CHUNKER_NAME = "minos";
	public final static String OPTION_MALT_MODEL_PATH = "malt_model_path";
	
	public MinosChunker(String maltModelPath, String nonSubjectFile, String nonSubjectReflexiveFile) {
		this.nonSubjectFile = nonSubjectFile;
		this.nonSubjectReflexiveFile = nonSubjectReflexiveFile;
		
		try {
			this.maltService = new MaltParserService();
			this.maltService.initializeParserModel("-c " + MaltModel +" -m parse -w " + maltModelPath + " -lfi parser.log");
		} catch (MaltChainedException e) {
			System.out.println("Malt parser could not be initialized.");
			e.printStackTrace();
		}
		
		//maltService.terminateParserModel();
	}
	
	private String[] conllSentence(Sentence sentence){
		return ConllStreamWriter.convertSentence(sentence);
	}
	
	private DependencyStructure parseMalt(Sentence sentence) throws MaltChainedException{
		String[] conllSentence = conllSentence(sentence);
		try{
			DependencyStructure graph = this.maltService.parse(conllSentence);
			return graph;
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param sentence
	 * @return
	 * @throws MaltChainedException 
	 */
	private AnnotationSet chunkSentence(Sentence sentence) throws MaltChainedException{
		AnnotationSet chunking = new AnnotationSet(sentence);
		
		ArrayList<Token> tokens = sentence.getTokens();
		TokenAttributeIndex ai = sentence.getAttributeIndex();
		
		/** Malt Parser **/
		DependencyStructure maltGraph = parseMalt(sentence);
		
		for (int i=0; i<tokens.size(); i++ ){
			Token t = tokens.get(i);
			if (MinosVerb.isVerb(t, sentence)){
				MinosVerb vb = new MinosVerb(t, ai, sentence, maltGraph, nonSubjectFile, nonSubjectReflexiveFile);
				if (vb.isZeroAnaphora(document)){
					chunking.addChunk(new Annotation(i, MinosChunker.Annotation, sentence));
				}
				else{
					chunking.addChunk(new Annotation(i, MinosChunker.NegativeAnnotation, sentence));
				}
			}	
		}
		
		return chunking;
	}	
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		this.document = ps;
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				try {
					chunkings.put(sentence, this.chunkSentence(sentence));
				} catch (MaltChainedException e) {
					e.printStackTrace();
				}
			
		return chunkings;
	}
	
	/**
	 * Util class for MaltParser dependency graph
	 * @author Adam Kaczmarek
	 *
	 */
	static class MaltGraphUtil{
		private static final String LABEL_TABLE_NAME = "DEPREL";
		
		public static Set<Edge> getEdgesFromNode(DependencyStructure graph, int nodeIndex){
			SortedSet<Edge> edges = graph.getEdges();
			SortedSet<Edge> edgesFromNode = new TreeSet<Edge>();
			for(Edge e : edges){
				if(nodeIndex == e.getSource().getIndex()){
					edgesFromNode.add(e);
				}
			}
			return edgesFromNode;
		}
		
		public static Set<Edge> getEdgesToNode(DependencyStructure graph, int nodeIndex){
			SortedSet<Edge> edges = graph.getEdges();
			SortedSet<Edge> edgesFromNode = new TreeSet<Edge>();
			for(Edge e : edges){
				if(nodeIndex == e.getTarget().getIndex()){
					edgesFromNode.add(e);
				}
			}
			return edgesFromNode;
		}
		
		public static boolean hasRelation(Set<Edge> edges, String relationName) throws MaltChainedException{
			for(Edge e : edges)
				for(SymbolTable t : e.getLabelTypes())
					if(LABEL_TABLE_NAME.equals(t.getName()) && relationName.equals(e.getLabelSymbol(t))) return true;
			return false;
		}
		
		public static boolean hasIncomingRelation(DependencyStructure graph, int nodeIndex, String relationName) throws MaltChainedException{
			return hasRelation(getEdgesToNode(graph, nodeIndex), relationName);
		}
		
		public static boolean hasOutgoingRelation(DependencyStructure graph, int nodeIndex, String relationName) throws MaltChainedException{
			return hasRelation(getEdgesFromNode(graph, nodeIndex), relationName);
		}
		
		
		public static List<Node> getOutgoingRelationTargetNode(DependencyStructure graph, int nodeIndex, String relationName) throws MaltChainedException{
			List<Node> subjNodes = new ArrayList<Node>();
			Set<Edge> edges = getEdgesFromNode(graph, nodeIndex);
			for(Edge e : edges)
				for(SymbolTable t : e.getLabelTypes())
					if(LABEL_TABLE_NAME.equals(t.getName()) && relationName.equals(e.getLabelSymbol(t))) subjNodes.add(e.getTarget());
				
			return subjNodes;
		}
	}
	
	/**
	 * Wrapper enum for Gender
	 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
	 *
	 */
	public enum Gender{
		UNDEFINED(""),
		NEUTER("n"), 
		FEMINUM("f"),
		MASCULINUM("m"),
		MASCULINUM_1("m1"),
		MASCULINUM_2("m2"),
		MASCULINUM_3("m3");
		
		private final String label;
		
		public static Gender fromValue(String value){
			if("n".equals(value)) return NEUTER;
			if("m1".equals(value)) return MASCULINUM_1;
			if("m2".equals(value)) return MASCULINUM_2;
			if("m3".equals(value)) return MASCULINUM_3;
			if("f".equals(value)) return FEMINUM;
			return UNDEFINED;
		}
		
		Gender(String label){
			this.label = label;
		}
		
		boolean isMasculinum(){
			return this.equals(MASCULINUM_1) || this.equals(MASCULINUM_2) || this.equals(MASCULINUM_3) || this.equals(MASCULINUM);
		}
		
		boolean equals(Gender g2, boolean masculinumTolerance, boolean undefinedIsAlwaysEqual){
			if(undefinedIsAlwaysEqual && (UNDEFINED.equals(this) || UNDEFINED.equals(g2))) return true;
			if(masculinumTolerance && isMasculinum()) return g2.isMasculinum();
			return label.equals(g2.label);
		}
	}
	
	/**
	 * Nested helper class encapsulating verb classification processing 
	 * @author Adam Kaczmarek
	 *
	 */
	public static class MinosVerb{
		// --------- Settings
		private static final boolean SETTINGS_CHECK_MALT_SUBJ_AGREEMENT = true;
		public static final int SETTINGS_SUBJECT_SEARCH_RANGE_BACK = 15;
		public static final int SETTINGS_SUBJECT_SEARCH_RANGE_FWD = 9;
		public static final Set<String> SETTINGS_INTERPS_POS = new HashSet<String>(Arrays.asList(new String[]{"interp", "conj"}));
		public static final Set<String> SETTINGS_IGNORE_INTERPS_ORTH = new HashSet<String>(Arrays.asList(new String[]{}));
		public final static boolean SETTINGS_INTERP_SEPARATION = true;
		public final static boolean SETTINGS_VERB_SEPARATION = true;
		public final static boolean SETTINGS_PRECEEDING_PREDICATE = true;
		public final static boolean SETTINGS_MOOSE = true;
		public final static boolean SETTINGS_AUX_IT = true;
		public final static boolean SETTINGS_ADV_BETTER_WORSE = true;
		public final static boolean SETTINGS_TAKSIE = true;
		public final static boolean SETTINGS_MAYBE = true;
		public final static boolean SETTINGS_NEUTER_ADV = true;
		public final static boolean SETTINGS_NEUTER_INT = true;
		// -------- Endof: Settings
		
		public final static String OPTION_NON_SUBJECT_VERBS = "ns_verbs";
		public final static String OPTION_NON_SUBJECT_VERBS_REFL = "ns_verbs_refl";
		public final static String OPTION_NON_SUBJECT_VERBS_RELF_INF = "ns_verbs_refl_inf";
		
		public static final Set<String> PartsOfSpeech = new HashSet<String>(Arrays.asList(new String[]{"fin", "praet", "winien", "bedzie"}));
		private String nonSubjectFile = null;
		private String nonSubjectReflexiveFile = null;
		private static Set<String> nonSubjectVerbs = null;
		private static Set<String> nonSubjectReflexiveVerbs = null;
		private static final int reflexiveRadius = 2;
		private static final int predicateRadius = 1;
		private static final int neuterRadius = 2;
		private static final int maybeRadius = 1;
		
		private static final String BE_BASE = "być";
		private static final String BETTER = "dobrze";
		private static final String WORSE = "źle";
		
		private static final String AGLT_CLASS = "aglt";
		private static final String ADV_CLASS = "adv";
		private static final String INF_CLASS = "inf";
		private static final String PRED_CLASS = "pred";
		private static final String GER_CLASS = "ger";
		private static final String QUB_CLASS = "qub";
		
		private static final String AUX_RELATION = "aux";
		private static final String REFL_RELATION = "refl";
		private static final String REFL_ORTH = "się";
		private static final String TAK_ORTH = "tak";
		private static final String PLEASE_ORTH = "proszę";
//		private static final String SUBJ_RELATION = "subj";
		
		private static final String JUZ_ORTH = "już";
		private static final String MAY_ORTH = "może";
		private static final String NO_ORTH = "no";
		private static final String PAN_ORTH = "pan";
		private static final String PANI_ORTH = "pani";
		private static final String PANSTWO_ORTH = "państwo";
		private static final String O_ORTH = "o";
				
		private Token verb;
		private TokenAttributeIndex ai;
		private Sentence sentence;
		private DependencyStructure graph;
				
		private int positionInSentence;
		private String person;
		private Gender gender;
		private String number;
		private String partOfSpeech;
		private String orth;
		private String base;
		private boolean reflexive;
		
		public MinosVerb(Token v, TokenAttributeIndex ai, Sentence s, DependencyStructure graph, String nonSubjectFile, String nonSubjectReflexiveFile) throws MaltChainedException{
			this.verb = v;
			this.ai = ai;
			this.sentence = s;
			this.graph = graph;
			extractVerbInfo();
			
			// Load non-subject quasi verbs
			if(nonSubjectVerbs == null || nonSubjectReflexiveVerbs == null) loadNonSubjectVerbs(nonSubjectFile, nonSubjectReflexiveFile);
		}
		
		public static boolean isVerb(Token t, Sentence s){
			TokenAttributeIndex ai = s.getAttributeIndex();
			String tokenClass = ai.getAttributeValue(t, "class");
			return PartsOfSpeech.contains(tokenClass); 
		}
		
		public boolean isInterp(int tokenIndex, int intervalTokenIndex, boolean[] verbIndices){
			Token interpToken = sentence.getTokens().get(tokenIndex);
			boolean interpPos = SETTINGS_INTERPS_POS.contains(ai.getAttributeValue(interpToken, "class")); 
			boolean interpOrth = !SETTINGS_IGNORE_INTERPS_ORTH.contains(ai.getAttributeValue(interpToken, "orth").toLowerCase());
			return interpPos &&	interpOrth && validateInterp(tokenIndex, intervalTokenIndex, verbIndices);
		}
		
		public boolean validateInterp(int tokenIndex, int intervalTokenIndex, boolean[] verbIndices){
			if(tokenIndex < this.positionInSentence){
				for(int i = 0; i < intervalTokenIndex; i++){
					if(verbIndices[i]) return true;
				}
			}
			else{
				int positionInInterval = Math.min(this.positionInSentence, SETTINGS_SUBJECT_SEARCH_RANGE_BACK);
				for(int i = positionInInterval + 1; i < verbIndices.length; i++){
					if(verbIndices[i]) return true;
				}
			}
			return false;
		}
		
		public boolean checkNounVerbSeparation(int nounTokenIndex, boolean[] interps, boolean[] verbs){
			int verbTokenIndex = Math.min(this.positionInSentence, SETTINGS_SUBJECT_SEARCH_RANGE_BACK);
			assert(verbs[verbTokenIndex]);
			int min = verbTokenIndex < nounTokenIndex ? verbTokenIndex : nounTokenIndex;
			int max = verbTokenIndex < nounTokenIndex ? nounTokenIndex : verbTokenIndex;			
			
			if(SETTINGS_INTERP_SEPARATION)
				for(int i = min + 1; i < max; i++)
					if (interps[i]) return false;
			
			if(SETTINGS_VERB_SEPARATION)
				for(int i = min + 1; i < max; i++)
					if (verbs[i]) return false;
						
			return true;
		}
		
		public List<MinosNoun> findSubjectCandidates(){
			List<MinosNoun> subjectCandidates = new ArrayList<MinosNoun>();
			int startIndex = Math.max(0, this.sentence.getTokens().indexOf(this.verb) - SETTINGS_SUBJECT_SEARCH_RANGE_BACK);
			int endIndex = Math.min(this.sentence.getTokens().size() - 1, this.sentence.getTokens().indexOf(this.verb) + SETTINGS_SUBJECT_SEARCH_RANGE_FWD);
			int intervalLength = endIndex - startIndex + 1;
			
			ArrayList<Integer> subjectIndices = new ArrayList<Integer>();
			boolean[] interpsIndices = new boolean[intervalLength];
			boolean[] verbIndices = new boolean[intervalLength];
			
			for(int i = 0; i < intervalLength; i++){
				int tokenIndex = startIndex + i;
				if(MinosNoun.isNoun(sentence.getTokens().get(tokenIndex), sentence)){
					subjectIndices.add(i);
				}
				else if(MinosVerb.isVerb(sentence.getTokens().get(tokenIndex), sentence)){
					verbIndices[i] = true;
				}
			}
			
			for(int i = 0; i < intervalLength; i++){
				int tokenIndex = startIndex + i;
				if(isInterp(tokenIndex, i, verbIndices)){
					interpsIndices[i] = true;
				}
			}
			
			for(Integer i : subjectIndices){
				int tokenIndex = startIndex + i;
				if(this.checkNounVerbSeparation(i, interpsIndices, verbIndices)){
					subjectCandidates.add(new MinosNoun(sentence.getTokens().get(tokenIndex), sentence, true));
				}
			}
			return subjectCandidates;
		}
		
		private void extractAgltInfo(){
			// 1. verb + aglt
			if(this.positionInSentence + 1 < this.sentence.getTokens().size()){
				Token agltCandidate = this.sentence.getTokens().get(this.positionInSentence + 1);
				if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "class"))){
					this.person = ai.getAttributeValue(agltCandidate, "person");
					return;
				}
			}
			// 2. verb + qub + aglt
			if(this.positionInSentence + 2 < this.sentence.getTokens().size()){
				Token qubCandidate = this.sentence.getTokens().get(this.positionInSentence + 1);
				Token agltCandidate = this.sentence.getTokens().get(this.positionInSentence + 2);
				if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "class")) && 
						QUB_CLASS.equalsIgnoreCase(ai.getAttributeValue(qubCandidate, "class"))){
					this.person = ai.getAttributeValue(agltCandidate, "person");
					return;
				}
			}
			// 3. aglt + ... + verb
//			if(){
//				return
//			}
		}
		
		private void extractVerbInfo() throws MaltChainedException{
			this.positionInSentence = this.sentence.getTokens().indexOf(this.verb);
			this.orth 			= ai.getAttributeValue(this.verb, "orth");
			this.base 			= ai.getAttributeValue(this.verb, "base");
			this.partOfSpeech 	= ai.getAttributeValue(this.verb, "class");
			this.person 		= ai.getAttributeValue(this.verb, "person");
			this.gender 		= Gender.fromValue(ai.getAttributeValue(this.verb, "gender"));
			this.number 		= ai.getAttributeValue(this.verb, "number");
			this.reflexive 		= _isReflexiveScan(reflexiveRadius) || _isReflexiveMalt();
			
			extractAgltInfo();
			if(this.person == null) this.person = "ter";
		}
		
		private boolean attributeLookup(int radius, String attributeName, String attributeValue){
			int start = Math.max(0, this.positionInSentence - radius);
			int end = Math.min(this.sentence.getTokenNumber() - 1, this.positionInSentence + radius);
			for (int i = start; i <= end; i++)
				if(attributeValue.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(i), attributeName))) return true;
				
			return false;
		}
		
		private HashSet<String> loadFileLines(String path){
			HashSet<String> lines = new HashSet<String>();
			try{
				BufferedReader br = new BufferedReader(new FileReader(path));
				String line = null;
				do{
					line = br.readLine();
					if(line != null) lines.add(line);
				}while(line != null);
				br.close();
			}
			catch(IOException ex){
				System.out.println("Cannot load non-subject verb file: " + path + " - omitting this feature.");
			}
			return lines;
		}
		
		private void loadNonSubjectVerbs(String nonSubjectFile, String nonSubjectReflexiveFile){
			nonSubjectVerbs = loadFileLines(nonSubjectFile);
			nonSubjectReflexiveVerbs = loadFileLines(nonSubjectReflexiveFile);
		}
		
		public boolean isPriSec(){
			return this.person != null && ( this.person.equals("pri") || this.person.equals("sec") ); 
		}
		
		public boolean please(){
			boolean orthCondition =  PLEASE_ORTH.equalsIgnoreCase(this.orth);
			boolean preceedingNo = false; // No proszę.
			boolean followingMr = false;  // Proszę pana
			boolean followingInf = false; // Proszę usiąść
			boolean followingOGer = false; // Proszę o zaznaczenie
			
			try{
				preceedingNo = NO_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 1), "base"));
			}
			catch(IndexOutOfBoundsException ex){}
			
			try{
				Token token = this.sentence.getTokens().get(this.positionInSentence + 1);
				
				followingMr = 
						PANSTWO_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(token, "base"))
						|| PANI_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(token, "base"))
						|| PAN_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(token, "base"));
				
				followingInf = 
						INF_CLASS.equalsIgnoreCase(this.ai.getAttributeValue(token, "class")) 
						|| INF_CLASS.equalsIgnoreCase(this.ai.getAttributeValue(token, "pos"));
			}
			catch(IndexOutOfBoundsException ex){}
			
			try{
				followingOGer = 
						O_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence + 1), "orth")) &&
						(GER_CLASS.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence + 1), "class"))
						|| GER_CLASS.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence + 1), "pos")));
			}
			catch(IndexOutOfBoundsException ex){}
			
			return orthCondition && (preceedingNo || followingMr || followingInf || followingOGer);
		}
		
		public boolean preceedingPersonPriSec(){
			if("pri".equalsIgnoreCase(this.person)){
				try{
					if("ja".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 2), "base")) 
						&& "nie".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 1), "base"))) return true;
				}
				catch(ArrayIndexOutOfBoundsException ex){}
				
				try{
					if("ja".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 1), "base"))) return true;
				}
				catch(ArrayIndexOutOfBoundsException ex){}
				
				return false;
			}
			else if ("sec".equalsIgnoreCase(this.person)){
				try{
					if("ty".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 2), "base")) 
						&& "nie".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 1), "base"))) return true;
				}
				catch(ArrayIndexOutOfBoundsException ex){}
				
				try{
					if("ty".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence - 1), "base"))) return true;
				}
				catch(ArrayIndexOutOfBoundsException ex){}
				return false;
			}
			return false;
		}
		
		public boolean hasMaltSubjectPriSec() throws MaltChainedException{
			List<MinosNoun> subjects = _getMaltSubject();
			if(subjects == null || subjects.size() <= 0) return false;
			
			for(MinosNoun subject : subjects)
				if(this.person.equalsIgnoreCase(subject.person)) return true;
			
			return false;
		}
		
		public boolean preceedingPredicate(int radius){
			return attributeLookup(predicateRadius, "class", PRED_CLASS);
		}
		
		public boolean mooseCriterion() throws MaltChainedException{
			boolean endingCondtion = this.orth.toLowerCase().endsWith("ło");
			return endingCondtion && this.reflexive;
		}
		
		public boolean auxiliaryIt() throws MaltChainedException{
			boolean outgoing = MaltGraphUtil.hasOutgoingRelation(this.graph, this.positionInSentence + 1, AUX_RELATION);
			boolean incoming = MaltGraphUtil.hasIncomingRelation(this.graph, this.positionInSentence + 1, AUX_RELATION);
			boolean toBase = false;
			
			try{
				toBase = "to".equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(this.positionInSentence + 1), "orth"));
			}catch(IndexOutOfBoundsException ex){}
			
			boolean toout = incoming && toBase;
			
			return outgoing || toout;//incoming;
		}
		
		public boolean advBetterWorse(){
			int precIndex = this.positionInSentence - 1;
			int follIndex = this.positionInSentence + 1;
			boolean baseCondition = BE_BASE.equals(this.base); 
			boolean preceeding = false;
			boolean following = false;
			
			if(precIndex >= 0){
				String preceedingBase = this.ai.getAttributeValue(this.sentence.getTokens().get(precIndex), "base");
				String preceedingPos = this.ai.getAttributeValue(this.sentence.getTokens().get(precIndex), "class");
				preceeding = (BETTER.equals(preceedingBase) || WORSE.equals(preceedingBase)) && ADV_CLASS.equals(preceedingPos);
			}
			
			if(follIndex < this.sentence.getTokens().size()){
				String followingBase = this.ai.getAttributeValue(this.sentence.getTokens().get(follIndex), "base");
				String followingPos = this.ai.getAttributeValue(this.sentence.getTokens().get(follIndex), "class");
				following = (BETTER.equals(followingBase) || WORSE.equals(followingBase)) && ADV_CLASS.equals(followingPos);
			}
			
		    return baseCondition && (preceeding || following);
		}
		
		public boolean taksie(){
			boolean sieCondition = attributeLookup(reflexiveRadius, "orth", REFL_ORTH);
			boolean takCondition = attributeLookup(reflexiveRadius, "orth", TAK_ORTH);
			
		    return sieCondition && takCondition;
		}
		
		public boolean isNonAnaphoricVerbPriSec() throws MaltChainedException{
			if(please()) return true;
			if(preceedingPersonPriSec()) return true;
			if(hasMaltSubjectPriSec()) return true;
			return false;
		}
		
		public boolean isNonAnaphoricVerb() throws MaltChainedException{
			if(isNonSubjectVerb()) return true;
			if(!PartsOfSpeech.contains(this.partOfSpeech)) return true;
			if(SETTINGS_PRECEEDING_PREDICATE && preceedingPredicate(predicateRadius)) return true;
			if(SETTINGS_MOOSE && mooseCriterion()) return true;
			if(SETTINGS_AUX_IT && auxiliaryIt()) return true;
			if(SETTINGS_ADV_BETTER_WORSE && advBetterWorse()) return true;
			if(SETTINGS_TAKSIE && taksie()) return true;
			
			if(SETTINGS_NEUTER_ADV && neuterAdv()) return true;
			if(SETTINGS_NEUTER_INT && neuterInt()) return true;
			if(SETTINGS_MAYBE && maybe()) return true;
			
			return false;
		}
		
		public boolean neuterAdv(){
			boolean neuter = Gender.NEUTER.equals(this.gender);
			boolean accompanyingAdverb = attributeLookup(neuterRadius, "pos", "adv") || attributeLookup(neuterRadius, "class", "adv");
			boolean accompanyingJuz = attributeLookup(neuterRadius, "base", JUZ_ORTH);
			
			return neuter && (accompanyingAdverb || accompanyingJuz);
		}
		
		public boolean neuterInt(){
			boolean neuter = Gender.NEUTER.equals(this.gender);
			boolean integer = false;
			
			int start = Math.max(0, this.positionInSentence - neuterRadius);
			int end = Math.min(this.sentence.getTokenNumber() - 1, this.positionInSentence + neuterRadius);
			for (int i = start; i < end; i++){
				try{
					Integer.parseInt(this.ai.getAttributeValue(this.sentence.getTokens().get(i), "orth"));
					integer = true;
					break;
				}
				catch(NumberFormatException ex){}
			}
			
			return neuter && integer;
		}
		
		public boolean maybe(){
			boolean may = MAY_ORTH.equalsIgnoreCase(this.orth); 
			boolean be = attributeLookup(maybeRadius, "orth", BE_BASE);
			
			return may && be;
		}
		
		public boolean hasSubject(Document document) throws MaltChainedException{
			if (hasMaltSubject(SETTINGS_CHECK_MALT_SUBJ_AGREEMENT)) return true;
			if (hasChunkrelSubject(document)) return true;
			if (hasContextSubject()) return true;
			
			return false;
		}
		
		public boolean isZeroAnaphora(Document document) throws MaltChainedException{
			if(isPriSec()){
				if(isNonAnaphoricVerbPriSec()) return false;
			}
			else{
				if(isNonAnaphoricVerb()) return false;
				if(hasSubject(document)) return false;
			}
		
			return true;
		}
		
		private boolean _isReflexiveMalt() throws MaltChainedException{
			return MaltGraphUtil.hasOutgoingRelation(this.graph, positionInSentence + 1, REFL_RELATION);
		}
		
		private boolean _isReflexiveScan(int radius){
			return attributeLookup(radius, "orth", REFL_ORTH);
		}
		
		public boolean isNonSubjectVerb() throws MaltChainedException{
			String base = this.verb.getAttributeValue(this.ai.getIndex("base"));
			boolean simpleNonSubject = nonSubjectVerbs.contains(base);
			boolean reflexiveNonSubject = nonSubjectReflexiveVerbs.contains(base) && this.reflexive;
			return simpleNonSubject || reflexiveNonSubject;
		}
		
		private List<MinosNoun> _getMaltSubject() throws MaltChainedException{
			List<Node> subjectNodes = MaltGraphUtil.getOutgoingRelationTargetNode(graph, positionInSentence + 1, Relation.SUBJECT);
			List<MinosChunker.MinosNoun> minosSubjects = new ArrayList<MinosChunker.MinosNoun>();
			for(Node subjectNode : subjectNodes) 
				minosSubjects.add(MinosNoun.fromNode(subjectNode, this.sentence));
			return minosSubjects;
		}
		
		
		public boolean hasMaltSubject(boolean checkSubjectAgreement) throws MaltChainedException{
			List<MinosNoun> maltSubjects = _getMaltSubject();
			if(maltSubjects == null || maltSubjects.size() <= 0) return false;
			if(!checkSubjectAgreement) return true; 
				
			for(MinosNoun subject : maltSubjects)
				if(subject.checkAgreement(this)) return true;
				
			return false;
		}
		
		public boolean hasChunkrelSubject(Document document){
			ArrayList<Annotation> chunkVP = this.sentence.getChunksAt(sentence.getTokens().indexOf(this.verb), Arrays.asList(Pattern.compile("chunk_vp")));
			if(chunkVP.isEmpty()) return false;
			Set<Relation> relationsFromChunkVP = document.getRelations().getOutgoingRelations(chunkVP.get(0));
			for(Relation relation : relationsFromChunkVP)
				if(Relation.SUBJECT.equalsIgnoreCase(relation.getType())){
//					System.out.println("CHUNKREL_SUBJECT");
					return true;
//					List<MinosNoun> chunkrelSubjects = new ArrayList<MinosChunker.MinosNoun>();
//					for(int tokenId : relation.getAnnotationTo().getTokens())
//						chunkrelSubjects.add(new MinosNoun(this.sentence.getTokens().get(tokenId), this.sentence, false, true));
//					
//					for(MinosNoun chunkrelSubject : chunkrelSubjects)
//						if(chunkrelSubject.checkAgreement(this)) return true;
					//@TODO add flag to settings
					//if(chunkrelSubject.checkAgreement(this)) return true; 
				}
			
			return false;
		}
		
		public boolean hasContextSubject(){
			List<MinosNoun> subjectCandidates = findSubjectCandidates();
			for(MinosNoun noun: subjectCandidates)
				if (noun.checkAgreement(this)) return true;
				
			return false;
		}
	}
	
	/**
	 * Nested helper class encapsulating noun classification processing 
	 * @author Adam Kaczmarek
	 *
	 */
	static class MinosNoun{
		public static final boolean SETTINGS_ALLOW_ACC_CASE = true;
		public static final boolean SETTINGS_MASCULINUM_TOLERANCE = false;
		public final static boolean SETTINGS_UNDEFINED_GENDER_ALWAYS_EQUAL = true;
		public final static boolean SETTINGS_ALLOW_ADJ_POS = true;
		
		
		public static final Set<String> ALLOWED_CLASSES = new HashSet<String>(Arrays.asList(new String[]{"subst", "ger", "depr", "num", "numcol", "ppron3", "ppron12", "xxs"}));
		public static final Set<String> NUM_CLASSES = new HashSet<String>(Arrays.asList(new String[]{"num", "numcol"}));
		public static final Set<String> ADDITIONAL_NOUN_ORTHS = new HashSet<String>(Arrays.asList(new String[]{"wszystek", "wszyscy", "który", "któryś"}));
		public static final String CASE_NOM = "nom";
		public static final String CASE_ACC = "acc";
		public static final String CASE_GEN = "gen";
		public static final String CASE_DAT = "dat";
		
		private static final String ADJ_CLASS = "adj";
		
		public static final String LO_ENDING = "ło";
		
		private static final String BE_ORTH = "było";
		private static final String HAVE_ORTH = "ma";
		public static final String NEG_ORTH = "nie";
		
		private Token noun;
		private Sentence sentence;
		private TokenAttributeIndex ai;
		
		private String person;
		private Gender gender;
		private String number;
		private String posext;
		private String pos;
		private String orth;
		private String base;
		private String grammarCase;
		private boolean fromContext;
		private boolean fromChunkrel = false;
		
		public MinosNoun(Token token, Sentence sentence, boolean fromContext, boolean fromChunkrel){
			this.noun = token;
			this.sentence = sentence;
			this.fromContext = fromContext;
			this.fromChunkrel = fromChunkrel;
			this.ai = sentence.getAttributeIndex();
			extractNounInfo();
		}
		
		public MinosNoun(Token token, Sentence sentence, boolean fromContext){
			this.noun = token;
			this.sentence = sentence;
			this.fromContext = fromContext;
			this.ai = sentence.getAttributeIndex();
			extractNounInfo();
		}
		
		
		private void extractNounInfo(){
			this.base 	= this.ai.getAttributeValue(this.noun, "base"); 
			this.orth 	= this.ai.getAttributeValue(this.noun, "orth");
			this.pos 	= this.ai.getAttributeValue(this.noun, "pos");
			this.posext = this.ai.getAttributeValue(this.noun, "class");
			this.person = this.ai.getAttributeValue(this.noun, "person");
			this.grammarCase = this.ai.getAttributeValue(this.noun, "case");
			this.gender = Gender.fromValue(this.ai.getAttributeValue(this.noun, "gender"));
			this.number = this.ai.getAttributeValue(this.noun, "number");
		}
		
		public static MinosNoun fromNode(Node node, Sentence sentence){
			if(node == null) return null;
			int tokenIndex = node.getIndex() - 1;
			Token subjToken = sentence.getTokens().get(tokenIndex);
//			TokenAttributeIndex ai = sentence.getAttributeIndex();
			return new MinosNoun(subjToken, sentence, false);
		}
		
		public static boolean isNoun(Token t, Sentence s){
			TokenAttributeIndex ai = s.getAttributeIndex();
			return ALLOWED_CLASSES.contains(ai.getAttributeValue(t, "class")) || ADDITIONAL_NOUN_ORTHS.contains(ai.getAttributeValue(t, "base").toLowerCase());
		}
		
		private boolean hasProperPos(){
			return ALLOWED_CLASSES.contains(this.pos) || ALLOWED_CLASSES.contains(this.posext);
		}
		
		private boolean hasWhichBase(){
			return ADDITIONAL_NOUN_ORTHS.contains(this.base);
		}
		
		private boolean hasProperCase(){
			boolean nom = CASE_NOM.equals(this.grammarCase);
			boolean acc = CASE_ACC.equals(this.grammarCase) && SETTINGS_ALLOW_ACC_CASE && "subst".equalsIgnoreCase(this.posext);
			
			return nom || acc;
		}
		
		private boolean isNeuterNumeral(MinosVerb verb){
			// 1. Czasownik w rodzaju nijakim, bądź kończący się na "ło"
			boolean neuterVerb = Gender.NEUTER.equals(verb.gender); 
			boolean verbLoEnd = verb.orth.endsWith(LO_ENDING);
			boolean verbCondition = neuterVerb || verbLoEnd;
			// 2. Rzeczownik w dopełniaczu?
		    boolean genitiveNoun = CASE_GEN.equals(this.grammarCase);
		    boolean dativeNoun = CASE_DAT.equals(this.grammarCase);
		    // 3. Poprzedzający numer etc.
		    boolean preceedingNumerator = false;
		    int preceedingIndex = this.sentence.getTokens().indexOf(this.noun) - 1;
		    if(preceedingIndex >= 0){
		    	Token preceedingToken = this.sentence.getTokens().get(preceedingIndex);
		    	TokenAttributeIndex ai = this.sentence.getAttributeIndex();
		    	String pos = ai.getAttributeValue(preceedingToken, "class");
		    	String orth = ai.getAttributeValue(preceedingToken, "orth");
		    	if (NUM_CLASSES.contains(pos)){
		    		preceedingNumerator = true;
		    	}
		    	else{
			    	try{
			    		Integer.parseInt(orth);
			    		preceedingNumerator = true;
			    	}
			    	catch(NumberFormatException e){}
		    	}
		    }
		    	
		    
		    return verbCondition && (genitiveNoun || dativeNoun) && preceedingNumerator;
		}
		
		public boolean allowedUseOfAdj(){
			return SETTINGS_ALLOW_ADJ_POS && !this.fromContext && (ADJ_CLASS.equalsIgnoreCase(this.posext) || ADJ_CLASS.equalsIgnoreCase(this.pos));
		}
		
		public boolean nieMaGen(MinosVerb verb){
			boolean be = BE_ORTH.equalsIgnoreCase(verb.orth);
			boolean have = HAVE_ORTH.equalsIgnoreCase(verb.orth);
			boolean genitiveCase = CASE_GEN.equalsIgnoreCase(this.grammarCase);
			boolean negation = false;
			try{
				negation = NEG_ORTH.equalsIgnoreCase(this.ai.getAttributeValue(this.sentence.getTokens().get(verb.positionInSentence - 1), "orth"));
			}
			catch(IndexOutOfBoundsException ex){}
			
			
			return negation && (have || be) && genitiveCase;
		}
		
		public boolean checkAgreement(MinosVerb verb){
			if(isNeuterNumeral(verb)) return true;
			if(nieMaGen(verb)) return true;
			if(this.fromChunkrel) return true;
			boolean correctPoS = hasProperPos() || hasWhichBase() || allowedUseOfAdj();
			boolean correctCase = hasProperCase();
			boolean numberAgreement = numberAgreement(verb);
			boolean personAgreement = personAgreement(verb);
			boolean genderAgreement = genderAgreement(verb);
			
			return correctPoS && correctCase && numberAgreement && personAgreement && genderAgreement;
		}
		
		private boolean numberAgreement(MinosVerb verb){
			if(this.number == null) return true;
			return this.number.equals(verb.number);
		}
		
		private boolean personAgreement(MinosVerb verb){
			if(this.person == null) return true;
			return this.person.equals(verb.person);
		}
		
		private boolean genderAgreement(MinosVerb verb){
			if(this.gender == null) return true;
			return this.gender.equals(verb.gender, SETTINGS_MASCULINUM_TOLERANCE, SETTINGS_UNDEFINED_GENDER_ALWAYS_EQUAL);
		}
		
		public String toString(){
			return this.orth;
		}
	}
}
