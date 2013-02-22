package liner2.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import liner2.chunker.Chunker;
import liner2.reader.FeatureGenerator;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.LinerOptions;
import liner2.Main;

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
		
		HashMap<String, ArrayList<Chunk>> chunks = new HashMap<String, ArrayList<Chunk>>();
		
		public void add(Chunk chunk){
			if ( !this.chunks.containsKey(chunk.getType()) ){
				ArrayList<Chunk> list = new ArrayList<Chunk>();
				list.add(chunk);
				this.chunks.put(chunk.getType(), list);
			}
			else{
				this.chunks.get(chunk.getType()).add(chunk);
			}				
		}
		
		public void addAll(ArrayList<Chunk> chunks){
			for (Chunk chunk : chunks)
				this.add(chunk);
		}
		
		public ArrayList<Chunk> getChunks(String name){
			if ( this.chunks.containsKey(name) )
				return this.chunks.get(name);
			else
				return new ArrayList<Chunk>();
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
			for ( ArrayList<Chunk> list : this.chunks.values())
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
	
	/**
	 * @param chunker
	 */
	public ChunkerEvaluatorMuc() {
	}
	
	/**
	 * Ocenia nerowanie całego dokumentu.
	 * @param set
	 */
	public void evaluate(HashMap<Sentence, Chunking> chunkings, HashMap<Sentence, Chunking> chunkigsRef){
		for ( Sentence sentence : chunkings.keySet()){
			this.evaluate(sentence, chunkings.get(sentence), chunkigsRef.get(sentence));
		}
	}
		
	/**
	 * 
	 */
	private void evaluate(Sentence sentence, Chunking chunking, Chunking chunkingRef) {
	
		// każdy HashSet w dwóch kopiach - jedna do iterowania, druga do modyfikacji
		HashSet<Chunk> trueChunkSet = chunkingRef.chunkSet();
		HashSet<Chunk> trueChunkSetIter = new HashSet<Chunk>(trueChunkSet);

		chunking.filter(LinerOptions.get().filters);			
		HashSet<Chunk> testedChunkSet = chunking.chunkSet();
		HashSet<Chunk> testedChunkSetIter = new HashSet<Chunk>(testedChunkSet);
		
		// usuń z danych wszystkie poprawne chunki
		for (Chunk trueChunk : trueChunkSetIter)
			for (Chunk testedChunk : testedChunkSetIter)
				if (trueChunk.equals(testedChunk)) {
					
					this.chunksTruePositives.add(testedChunk);
					this.globalTruePositives += 2;
					trueChunkSet.remove(trueChunk);
					testedChunkSet.remove(testedChunk);
					this.chunks.add(trueChunk);
					
				}
				
		// w testedChunkSet zostały falsePositives
		for (Chunk testedChunk : testedChunkSet) {
			
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
		for (Chunk trueChunk : trueChunkSet) {

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
	
	private boolean chunksOverlaps(Chunk a, Chunk b){
		return !(a.getEnd() < b.getBegin() || a.getBegin() > b.getEnd());
	}
	
	private boolean existsPartialyMatched(HashSet<Chunk> chunks, Chunk chunk){
		for ( Chunk test : chunks )
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
		return types;
	}
	
//	/**
//	 * Kompletność dla wszystkich typów anotacji. 
//	 */
//	public float getRecall(){
//		return globalRecall;
//	}
//
//	/**
//	 * Kompletność dla wskazanego typu anotacji. 
//	 * @param type
//	 */
//	public float getRecall(String type){
//		return recall.get(type);
//	}
//
//	/**
//	 * Średnia harmoniczna dla wszystkich typów anotacji. 
//	 * @return
//	 */
//	public float getFMeasure(){
//		return globalFMeasure;
//	}
//	
//	/**
//	 * Średnia harmoniczna dla wskazanego typu anotacji.
//	 * @param type
//	 * @return
//	 */
//	public float getFMeasure(String type){
//		return fMeasure.get(type);
//	}
	
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
		System.out.println("====================================================");
		System.out.println("# MUC match evaluation #");
		System.out.println("====================================================");
		System.out.println("Annotation           &  COR &  ACT &  POS &"
			+ " Precision & Recall  & F$_1$   \\\\");
		System.out.println("\\hline");
		for (String type : this.getTypes()) {
			int tp = this.chunksTruePositives.getChunkCount(type) * 2 + this.chunksTruePartially.getChunkCount(type);
			int fp = this.chunksFalsePositives.getChunkCount(type) * 2 + this.chunksTruePartially.getChunkCount(type);
			int fn = this.chunksFalseNegatives.getChunkCount(type) * 2 + this.chunksFalsePartially.getChunkCount(type);
			
			float p = (float)tp / ((float)tp + (float)fp);
			float r = (float)tp / ((float)tp + (float)fn);
			float f = 2*p*r / ( p + r);
			
			System.out.println(String.format("%-20s & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\", 
					type, tp, fp, fn, p*100, r*100, f*100));
		}
		System.out.println("\\hline");
		
		
		{
			float p = (float)this.globalTruePositives / ((float)this.globalTruePositives + (float)this.globalFalsePositives);
			float r = (float)this.globalTruePositives / ((float)this.globalTruePositives + (float)this.globalFalseNegatives);
			float f = 2*p*r / ( p + r);

			System.out.println(String.format("*TOTAL*              & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%%", 
					this.globalTruePositives, this.globalFalsePositives, this.globalFalseNegatives, p*100, r*100, f*100));
		}		
		System.out.println("----------------------------------------------------");
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
		HashSet<Chunk> truePositives, HashSet<Chunk> falsePositives, 
		HashSet<Chunk> falseNegatives) {
		
		String sentenceHeader = "Sentence #" + this.sentenceNum;
		if (paragraphId != null)
			sentenceHeader += " from " + paragraphId;
		Main.log(sentenceHeader);
		Main.log("");
		StringBuilder tokenOrths = new StringBuilder();
		StringBuilder tokenNums = new StringBuilder();
		int idx = 0;
		for (Token token : sentence.getTokens()) {
			idx++;
			String tokenOrth = token.getFirstValue();
			String tokenNum = ""+idx;
			while (tokenOrth.length() < tokenNum.length())
				tokenOrth += " ";
			while (tokenNum.length() < tokenOrth.length())
				tokenNum += "_";
			tokenOrths.append(tokenOrth + " ");
			tokenNums.append(tokenNum + " ");
		}
		Main.log("Text  : " + tokenOrths.toString().trim());
		Main.log("Tokens: " + tokenNums.toString().trim());
		Main.log("");
		Main.log("Chunks:");
		
		for (Chunk chunk : Chunk.sortChunks(truePositives)) {
			Main.log(String.format("  TruePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Chunk chunk : Chunk.sortChunks(falsePositives)) {
			Main.log(String.format("  FalsePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Chunk chunk : Chunk.sortChunks(falseNegatives)) {
			Main.log(String.format("  FalseNegative %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		
		Main.log("");
		Main.log("Features:", true);
		StringBuilder featuresHeader = new StringBuilder("       ");
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++)
			featuresHeader.append(String.format("[%d]_%s ", i+1, sentence.getAttributeIndex().getName(i)));
		Main.log(featuresHeader.toString(), true);
		
		idx = 0;
		for (Token token : sentence.getTokens()) {
			StringBuilder tokenFeatures = new StringBuilder(String.format("  %3d) ", ++idx));
			for (int i = 0; i < token.getNumAttributes(); i++)
				tokenFeatures.append(String.format("[%d]_%s ", i+1, token.getAttributeValue(i)));
			Main.log(tokenFeatures.toString(), true);
		}
		Main.log("", true);
	}
	
	private String printChunk(Chunk chunk) {
		ArrayList<Token> tokens = chunk.getSentence().getTokens();
		StringBuilder result = new StringBuilder();
		for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++)
			result.append(tokens.get(i).getFirstValue() + " ");
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
				ArrayList<Chunk> chunks = foreign.chunksTruePositives.getChunks(foreignKey);
				this.chunksTruePositives.addAll(chunks);
			}

			if (foreign.chunksTruePartially.getChunkCount(foreignKey)>0) {
				ArrayList<Chunk> chunks = foreign.chunksTruePartially.getChunks(foreignKey);
				this.chunksTruePartially.addAll(chunks);
			}

			if (foreign.chunksFalsePositives.getChunkCount(foreignKey)>0) {
				ArrayList<Chunk> chunks = foreign.chunksFalsePositives.getChunks(foreignKey);
				this.chunksFalsePositives.addAll(chunks);
			}
			
			if (foreign.chunksFalsePartially.getChunkCount(foreignKey)>0) {
				ArrayList<Chunk> chunks = foreign.chunksFalsePartially.getChunks(foreignKey);
				this.chunksFalsePartially.addAll(chunks);
			}
			
			if (foreign.chunksFalseNegatives.getChunkCount(foreignKey)>0) {
				ArrayList<Chunk> chunks = foreign.chunksFalseNegatives.getChunks(foreignKey);
				this.chunksFalseNegatives.addAll(chunks);
			}
			
		}
		
		this.globalTruePositives += foreign.globalTruePositives;
		this.globalFalsePositives += foreign.globalFalsePositives;
		this.globalFalseNegatives += foreign.globalFalseNegatives;
	}	
	
}
