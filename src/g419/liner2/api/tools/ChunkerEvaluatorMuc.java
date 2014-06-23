package g419.liner2.api.tools;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;



/**
 * 
 * Klasa służąca do oceny skuteczności chunkera. 
 * 
 * Skuteczność chunkera wyrażana jest przy pomocy trzech parametrów: precyzja, 
 * kompletność i średnia harmoniczna (F-measure) dla wszystkich anotacji 
 * łącznie i każdego typu z osobna.
 * 
 * Dodatkowowymi parametrami są statystyki true positives, 
 * true negatives i false negatives.
 * 
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 *
 */
public class ChunkerEvaluatorMuc {

	private HashSet<String> keys = new HashSet<String>();
	
	class ChunkTypedSet {
		
		HashMap<String, ArrayList<Annotation>> chunks = new HashMap<String, ArrayList<Annotation>>();
		
		public void add(Annotation chunk){
			if ( !this.chunks.containsKey(chunk.getType()) ){
				ArrayList<Annotation> list = new ArrayList<Annotation>();
				list.add(chunk);
				this.chunks.put(chunk.getType(), list);
			}
			else{
				this.chunks.get(chunk.getType()).add(chunk);
			}				
		}
		
		public void addAll(ArrayList<Annotation> chunks){
			for (Annotation chunk : chunks)
				this.add(chunk);
		}
		
		public ArrayList<Annotation> getChunks(String name){
			if ( this.chunks.containsKey(name) )
				return this.chunks.get(name);
			else
				return new ArrayList<Annotation>();
		}
		
		/**
		 * Zwraca liczbę chunków określonego typu.
		 * @param type
		 * @return
		 */
		public int getChunkCount(String type){
			if ( this.chunks.containsKey(type) )
				return this.chunks.get(type).size();
			else
				return 0;
		}
		
		/**
		 * Zwraca liczbę wszystkich chunków.
		 * @return
		 */
		public int getChunkCount(){
			int sum = 0;
			for ( ArrayList<Annotation> list : this.chunks.values())
				sum += list.size();
			return sum;
		}
		
	}
	
	
	/* typ chunku => lista chunków danego typu */
	private ChunkTypedSet chunksTruePositives = new ChunkTypedSet();
	private ChunkTypedSet chunksTruePartially = new ChunkTypedSet();
	private ChunkTypedSet chunksFalsePositives = new ChunkTypedSet();
	private ChunkTypedSet chunksFalsePartially = new ChunkTypedSet();
	private ChunkTypedSet chunksFalseNegatives = new ChunkTypedSet();
	private ChunkTypedSet chunks = new ChunkTypedSet();
	
	private int globalTruePositives = 0;
	private int globalFalsePositives = 0;
	private int globalFalseNegatives = 0;
	
	private int sentenceNum = 0;
	
	private boolean quiet = false;		// print sentence results?
	HashSet<String> types = new HashSet<String>();
	
	/**
	 * @param chunker
	 */
	public ChunkerEvaluatorMuc() {
	}

	public ChunkerEvaluatorMuc(HashSet<String> types) {
		this.types = types;
	}
	
	/**
	 * Ocenia nerowanie całego dokumentu.
	 * @param set
	 */
	public void evaluate(HashMap<Sentence, AnnotationSet> chunkings, HashMap<Sentence, AnnotationSet> chunkigsRef){
		for ( Sentence sentence : chunkings.keySet()){
			this.evaluate(sentence, chunkings.get(sentence), chunkigsRef.get(sentence));
		}
	}
		
	/**
	 * 
	 */
	private void evaluate(Sentence sentence, AnnotationSet chunking, AnnotationSet chunkingRef) {
			
		// Wybierz anotacje do oceny jeżeli został określony ich typ
		HashSet<Annotation> chunkingRefSet = new HashSet<Annotation>();
		HashSet<Annotation> chunkingSet = new HashSet<Annotation>();
		if ( this.types.size() == 0 ){
			chunkingRefSet = chunkingRef.chunkSet();
			chunkingSet = chunking.chunkSet();
		}
		else{
			for ( Annotation ann : chunkingRef.chunkSet() )
				if ( this.types.contains(ann.getType()) )
					chunkingRefSet.add(ann);
			for ( Annotation ann : chunking.chunkSet() )
				if ( this.types.contains(ann.getType()) )
					chunkingSet.add(ann);
		}
		
		// każdy HashSet w dwóch kopiach - jedna do iterowania, druga do modyfikacji
		HashSet<Annotation> trueChunkSet = new HashSet<Annotation>(chunkingRefSet);
		HashSet<Annotation> trueChunkSetIter = new HashSet<Annotation>(trueChunkSet);

		HashSet<Annotation> testedChunkSet = new HashSet<Annotation>(chunkingSet);
		HashSet<Annotation> testedChunkSetIter = new HashSet<Annotation>(testedChunkSet);
		
		// usuń z danych wszystkie poprawne chunki
		for (Annotation trueChunk : trueChunkSetIter)
			for (Annotation testedChunk : testedChunkSetIter)
				if (trueChunk.equals(testedChunk)) {
					
					this.chunksTruePositives.add(testedChunk);
					this.globalTruePositives += 2;
					trueChunkSet.remove(trueChunk);
					testedChunkSet.remove(testedChunk);
					this.chunks.add(trueChunk);
					
				}
				
		// w testedChunkSet zostały falsePositives
		for (Annotation testedChunk : testedChunkSet) {
			
			if ( this.existsPartialyMatched(trueChunkSet, testedChunk) ){
				this.chunksTruePartially.add(testedChunk);
				this.globalTruePositives += 1;
				this.globalFalsePositives += 1;
			}
			else{
				this.chunksFalsePositives.add(testedChunk);
				this.globalFalsePositives += 2;
			}
			this.chunks.add(testedChunk);			
		}
				
		// w trueChunkSet zostały falseNegatives
		for (Annotation trueChunk : trueChunkSet) {

			if ( this.existsPartialyMatched(testedChunkSet, trueChunk) ){
				this.chunksFalsePartially.add(trueChunk);
				//this.globalTruePositives += 1;
				this.globalFalseNegatives += 1;
			}
			else{
				this.chunksFalseNegatives.add(trueChunk);
				this.globalFalseNegatives += 2;
			}
			this.chunks.add(trueChunk);
		}
		
	}
	
	private boolean chunksOverlaps(Annotation a, Annotation b){
		return !(a.getEnd() < b.getBegin() || a.getBegin() > b.getEnd());
	}
	
	private boolean existsPartialyMatched(HashSet<Annotation> chunks, Annotation chunk){
		for ( Annotation test : chunks )
			if ( test.getType().equals(chunk.getType()) && this.chunksOverlaps(test, chunk) )
				return true;
		return false;
	}
	
	/**
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getPrecision(){
		return (float)this.globalTruePositives / ((float)this.globalTruePositives + (float)this.globalFalsePositives);
	}

	/**
	 * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
	 * @param type
	 * @return
	 */
	public float getPrecision(String type){
		return (float)this.chunksTruePositives.getChunkCount(type) 
				/ ((float)this.chunksTruePositives.getChunkCount(type) + (float)this.chunksFalsePositives.getChunkCount(type));
	}
	
	public boolean getQuiet() {
		return this.quiet;
	}

	public ArrayList<String> getTypes(){
		ArrayList<String> types = new ArrayList<String>();
		types.addAll(this.chunks.chunks.keySet());
		Collections.sort(types);
		return types;
	}
	
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}
	
	/**
	 * Drukuje wynik w formacie:
	 * 
	 * Annotation        &  COR &  ACT &  POS & Precision &   Recall &  F$_1$ \\
	 * \hline
	 * ROAD_NAM          &  147 &    8 &   36 &   94.84\% &  80.33\% &  86.98\%
	 * PERSON_LAST_NAM   &  306 &    9 &   57 &   97.14\% &  84.30\% &  90.27\%
	 * PERSON_FIRST_NAM  &  319 &    3 &   29 &   99.07\% &  91.67\% &  95.22\%
	 * COUNTRY_NAM       &  160 &   51 &   36 &   75.83\% &  81.63\% &  78.62\%
	 * CITY_NAM          &  841 &   65 &   75 &   92.83\% &  91.81\% &  92.32\%
	 * \hline
	 * *TOTAL*           & 1773 &  136 &  233 &   92.88\% &  88.38\% &  90.67\%
	 * 
	 */
	public void printResults(){
		this.printHeader("MUC match evaluation");
		System.out.println("        Annotation           &  COR &  ACT &  POS &"
			+ " Precision & Recall  & F$_1$   \\\\");
		System.out.println("\\hline");
		for (String type : this.getTypes()) {
			int tp = this.chunksTruePositives.getChunkCount(type) * 2 + this.chunksTruePartially.getChunkCount(type);
			int fp = this.chunksFalsePositives.getChunkCount(type) * 2 + this.chunksTruePartially.getChunkCount(type);
			int fn = this.chunksFalseNegatives.getChunkCount(type) * 2 + this.chunksFalsePartially.getChunkCount(type);
			
			float p = (float)tp / ((float)tp + (float)fp);
			float r = (float)tp / ((float)tp + (float)fn);
			float f = 2*p*r / ( p + r);
			
			System.out.println(String.format("        %-20s & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\", 
					type, tp, fp, fn, p*100, r*100, f*100));
		}
		System.out.println("\\hline");
		
		
		{
			float p = (float)this.globalTruePositives / ((float)this.globalTruePositives + (float)this.globalFalsePositives);
			float r = (float)this.globalTruePositives / ((float)this.globalTruePositives + (float)this.globalFalseNegatives);
			float f = 2*p*r / ( p + r);

			System.out.println(String.format("        *TOTAL*              & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%%", 
					this.globalTruePositives, this.globalFalsePositives, this.globalFalseNegatives, p*100, r*100, f*100));
		}		
		System.out.println("\n");
	}

	public void printHeader(String header){
        System.out.println("======================================================================================");
        System.out.println("# " + header);
        System.out.println("======================================================================================");
	}
	
	/**
	 * 
	 * @param sentence
	 * @param paragraphId
	 * @param truePositives
	 * @param falsePositives
	 * @param falseNegatives
	 */
	private void printSentenceResults(Sentence sentence, String paragraphId, 
		HashSet<Annotation> truePositives, HashSet<Annotation> falsePositives, 
		HashSet<Annotation> falseNegatives) {
		
		String sentenceHeader = "Sentence #" + this.sentenceNum;
		if (paragraphId != null)
			sentenceHeader += " from " + paragraphId;
		Logger.log(sentenceHeader);
		Logger.log("");
		StringBuilder tokenOrths = new StringBuilder();
		StringBuilder tokenNums = new StringBuilder();
		int idx = 0;
		for (Token token : sentence.getTokens()) {
			idx++;
			String tokenOrth = token.getOrth();
			String tokenNum = ""+idx;
			while (tokenOrth.length() < tokenNum.length())
				tokenOrth += " ";
			while (tokenNum.length() < tokenOrth.length())
				tokenNum += "_";
			tokenOrths.append(tokenOrth + " ");
			tokenNums.append(tokenNum + " ");
		}
		Logger.log("Text  : " + tokenOrths.toString().trim());
		Logger.log("Tokens: " + tokenNums.toString().trim());
		Logger.log("");
		Logger.log("Chunks:");
		
		for (Annotation chunk : Annotation.sortChunks(truePositives)) {
			Logger.log(String.format("  TruePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Annotation chunk : Annotation.sortChunks(falsePositives)) {
			Logger.log(String.format("  FalsePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Annotation chunk : Annotation.sortChunks(falseNegatives)) {
			Logger.log(String.format("  FalseNegative %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		
		Logger.log("");
		Logger.log("Features:", true);
		StringBuilder featuresHeader = new StringBuilder("       ");
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++)
			featuresHeader.append(String.format("[%d]_%s ", i+1, sentence.getAttributeIndex().getName(i)));
		Logger.log(featuresHeader.toString(), true);
		
		idx = 0;
		for (Token token : sentence.getTokens()) {
			StringBuilder tokenFeatures = new StringBuilder(String.format("  %3d) ", ++idx));
			for (int i = 0; i < token.getNumAttributes(); i++)
				tokenFeatures.append(String.format("[%d]_%s ", i+1, token.getAttributeValue(i)));
			Logger.log(tokenFeatures.toString(), true);
		}
		Logger.log("", true);
	}
	
	private String printChunk(Annotation chunk) {
		ArrayList<Token> tokens = chunk.getSentence().getTokens();
		StringBuilder result = new StringBuilder();
		for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++)
			result.append(tokens.get(i).getOrth() + " ");
		return result.toString().trim();
	}
	
	/**
	 * Dołącza do danych zawartość innego obiektu ChunkerEvaluator.
	 */	
	public void join(ChunkerEvaluatorMuc foreign) {
			
		for (String foreignKey : foreign.getTypes()) {
			
			if (!this.keys.contains(foreignKey))
				this.keys.add(foreignKey);
						
			if (foreign.chunksTruePositives.getChunkCount(foreignKey)>0) {
				ArrayList<Annotation> chunks = foreign.chunksTruePositives.getChunks(foreignKey);
				this.chunksTruePositives.addAll(chunks);
			}

			if (foreign.chunksTruePartially.getChunkCount(foreignKey)>0) {
				ArrayList<Annotation> chunks = foreign.chunksTruePartially.getChunks(foreignKey);
				this.chunksTruePartially.addAll(chunks);
			}

			if (foreign.chunksFalsePositives.getChunkCount(foreignKey)>0) {
				ArrayList<Annotation> chunks = foreign.chunksFalsePositives.getChunks(foreignKey);
				this.chunksFalsePositives.addAll(chunks);
			}
			
			if (foreign.chunksFalsePartially.getChunkCount(foreignKey)>0) {
				ArrayList<Annotation> chunks = foreign.chunksFalsePartially.getChunks(foreignKey);
				this.chunksFalsePartially.addAll(chunks);
			}
			
			if (foreign.chunksFalseNegatives.getChunkCount(foreignKey)>0) {
				ArrayList<Annotation> chunks = foreign.chunksFalseNegatives.getChunks(foreignKey);
				this.chunksFalseNegatives.addAll(chunks);
			}
			
		}
		
		this.globalTruePositives += foreign.globalTruePositives;
		this.globalFalsePositives += foreign.globalFalsePositives;
		this.globalFalseNegatives += foreign.globalFalseNegatives;
	}	
	
}
