package liner2.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import liner2.chunker.Chunker;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
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
public class ChunkerEvaluator {

	/* typ chunku => lista chunków danego typu */
	private HashMap<String, ArrayList<Chunk>> chunksTruePositives = new HashMap<String, ArrayList<Chunk>>();
	private HashMap<String, ArrayList<Chunk>> chunksFalsePositives = new HashMap<String, ArrayList<Chunk>>();
	private HashMap<String, ArrayList<Chunk>> chunksFalseNegatives = new HashMap<String, ArrayList<Chunk>>();
	
	private HashMap<String, Float> precision = new HashMap<String, Float>();
	private HashMap<String, Float> recall = new HashMap<String, Float>();
	private HashMap<String, Float> fMeasure = new HashMap<String, Float>();
	
	private HashMap<String, Integer> truePositives = new HashMap<String, Integer>();
	private HashMap<String, Integer> falsePositives = new HashMap<String, Integer>();
	private HashMap<String, Integer> falseNegatives = new HashMap<String, Integer>();
	
	private float globalPrecision = 0.0f;
	private float globalRecall = 0.0f;
	private float globalFMeasure = 0.0f;
	
	private int globalTruePositives = 0;
	private int globalFalsePositives = 0;
	private int globalFalseNegatives = 0;
	
	private int sentenceNum = 0;
	
	private HashSet<String> keys = new HashSet<String>();
	private Chunker chunker = null;
	private boolean quiet = false;		// print sentence results?
	
	/**
	 * @param chunker
	 */
	public ChunkerEvaluator(Chunker chunker) {
		this.chunker = chunker;
	}
	
	/**
	 * 
	 */
	public void evaluate(Paragraph paragraph) {
	
		for (Sentence sentence : paragraph.getSentences()) {
			// tylko na potrzeby wyświetlania szczegółów
			HashSet<Chunk> myTruePositives = new HashSet<Chunk>();
			this.sentenceNum++;
		
			// każdy HashSet w dwóch kopiach - jedna do iterowania, druga do modyfikacji
			HashSet<Chunk> trueChunkSet = sentence.getChunks();
			HashSet<Chunk> trueChunkSetIter = new HashSet<Chunk>(trueChunkSet);
			sentence.setChunking(new Chunking(sentence));
			Chunking chunking = this.chunker.chunkSentence(sentence);
			chunking.filter(LinerOptions.get().filters);
			HashSet<Chunk> testedChunkSet = chunking.chunkSet();
//			HashSet<Chunk> testedChunkSet = this.chunker.chunkSentence(sentence).chunkSet();
			HashSet<Chunk> testedChunkSetIter = new HashSet<Chunk>(testedChunkSet);
			
			// usuń z danych wszystkie poprawne chunki
			for (Chunk trueChunk : trueChunkSetIter)
				for (Chunk testedChunk : testedChunkSetIter)
					if (trueChunk.equals(testedChunk)) {
						// wpisz klucz do tablicy, jeśli jeszcze nie ma
						if (!this.chunksTruePositives.containsKey(testedChunk.getType())) {
							this.chunksTruePositives.put(testedChunk.getType(), new ArrayList<Chunk>());
							this.truePositives.put(testedChunk.getType(), new Integer(0));
							this.keys.add(testedChunk.getType());
						}
						// dodaj do istniejącego klucza
						this.chunksTruePositives.get(testedChunk.getType()).add(testedChunk);
						this.truePositives.put(testedChunk.getType(), 
							this.truePositives.get(testedChunk.getType()) + 1);
						this.globalTruePositives += 1;
						// oznacz jako TruePositive
						myTruePositives.add(testedChunk);
						trueChunkSet.remove(trueChunk);
						testedChunkSet.remove(testedChunk);
					}
					
			// w testedChunkSet zostały falsePositives
			for (Chunk testedChunk : testedChunkSet) {
				// wpisz klucz do tablicy, jeśli jeszcze nie ma
				if (!this.chunksFalsePositives.containsKey(testedChunk.getType())) {
					this.chunksFalsePositives.put(testedChunk.getType(), new ArrayList<Chunk>());
					this.falsePositives.put(testedChunk.getType(), new Integer(0));
					this.keys.add(testedChunk.getType());
				}
				// dodaj do istniejącego klucza
				this.chunksFalsePositives.get(testedChunk.getType()).add(testedChunk);
				this.falsePositives.put(testedChunk.getType(),
					this.falsePositives.get(testedChunk.getType()) + 1);
				this.globalFalsePositives += 1;
			}
					
			// w trueChunkSet zostały falseNegatives
			for (Chunk trueChunk : trueChunkSet) {
				// wpisz klucz do tablicy, jeśli jeszcze nie ma
				if (!this.chunksFalseNegatives.containsKey(trueChunk.getType())) {
					this.chunksFalseNegatives.put(trueChunk.getType(), new ArrayList<Chunk>());
					this.falseNegatives.put(trueChunk.getType(), new Integer(0));
					this.keys.add(trueChunk.getType());
				}
				// dodaj do istniejącego klucza
				this.chunksFalseNegatives.get(trueChunk.getType()).add(trueChunk);
				this.falseNegatives.put(trueChunk.getType(),
					this.falseNegatives.get(trueChunk.getType()) + 1);
				this.globalFalseNegatives += 1;
			}
			
			if (!this.quiet)
				printSentenceResults(sentence, paragraph.getId(),
					myTruePositives, testedChunkSet, trueChunkSet);
		}
		
		recalculateStats();
	}
	
	/**
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getPrecision(){
		return globalPrecision;
	}

	/**
	 * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
	 * @param type
	 * @return
	 */
	public float getPrecision(String type){
		return precision.get(type);
	}
	
	public boolean getQuiet() {
		return this.quiet;
	}

	/**
	 * Kompletność dla wszystkich typów anotacji. 
	 */
	public float getRecall(){
		return globalRecall;
	}

	/**
	 * Kompletność dla wskazanego typu anotacji. 
	 * @param type
	 */
	public float getRecall(String type){
		return recall.get(type);
	}

	/**
	 * Średnia harmoniczna dla wszystkich typów anotacji. 
	 * @return
	 */
	public float getFMeasure(){
		return globalFMeasure;
	}
	
	/**
	 * Średnia harmoniczna dla wskazanego typu anotacji.
	 * @param type
	 * @return
	 */
	public float getFMeasure(String type){
		return fMeasure.get(type);
	}
	
	public void setChunker(Chunker chunker){
		this.chunker = chunker;
	}
	
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}
	
	/**
	 * Drukuje wynik w formacie:
	 * 
	 * Annotation        &   TP &   FP &   FN & Precision &   Recall &  F$_1$ \\
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
		System.out.println("Annotation           &   TP &   FP &   FN &"
			+ " Precision & Recall  & F$_1$   \\\\");
		System.out.println("\\hline");
		for (String key : this.keys) {
			int tp = this.truePositives.containsKey(key) ? this.truePositives.get(key) : 0;
			int fp = this.falsePositives.containsKey(key) ? this.falsePositives.get(key) : 0;
			int fn = this.falseNegatives.containsKey(key) ? this.falseNegatives.get(key) : 0;
			
			System.out.println(String.format("%-20s & %4d & %4d & %4d &"
				+ "   %6.2f%% & %6.2f%% & %6.2f%% \\\\", key, tp, fp, fn,
				this.precision.get(key)*100, this.recall.get(key)*100, this.fMeasure.get(key)*100));
		}
		System.out.println("\\hline");
		System.out.println(String.format("*TOTAL*              & %4d & %4d & %4d &"
			+ "   %6.2f%% & %6.2f%% & %6.2f%%", this.globalTruePositives,
			this.globalFalsePositives, this.globalFalseNegatives,
			this.globalPrecision*100, this.globalRecall*100, this.globalFMeasure*100));
	}
	
	/**
	 * Dołącza do danych zawartość innego obiektu ChunkerEvaluator.
	 */	
	public void join(ChunkerEvaluator foreign) {
			
		for (String foreignKey : foreign.keys) {
			
			if (!this.keys.contains(foreignKey))
				this.keys.add(foreignKey);
						
			if (foreign.chunksTruePositives.containsKey(foreignKey)) {
				if (!this.chunksTruePositives.containsKey(foreignKey)) {
					this.chunksTruePositives.put(foreignKey, new ArrayList<Chunk>());
					this.truePositives.put(foreignKey, new Integer(0));
				}
				for (Chunk chunk : foreign.chunksTruePositives.get(foreignKey))
					this.chunksTruePositives.get(foreignKey).add(chunk);
				this.truePositives.put(foreignKey, this.truePositives.get(foreignKey) +
					foreign.truePositives.get(foreignKey));
			}
			
			if (foreign.chunksFalsePositives.containsKey(foreignKey)) {
				if (!this.chunksFalsePositives.containsKey(foreignKey)) {
					this.chunksFalsePositives.put(foreignKey, new ArrayList<Chunk>());
					this.falsePositives.put(foreignKey, new Integer(0));
				}
				for (Chunk chunk : foreign.chunksFalsePositives.get(foreignKey))
					this.chunksFalsePositives.get(foreignKey).add(chunk);
				this.falsePositives.put(foreignKey, this.falsePositives.get(foreignKey) +
					foreign.falsePositives.get(foreignKey));
			}
			
			if (foreign.chunksFalseNegatives.containsKey(foreignKey)) {
				if (!this.chunksFalseNegatives.containsKey(foreignKey)) {
					this.chunksFalseNegatives.put(foreignKey, new ArrayList<Chunk>());
					this.falseNegatives.put(foreignKey, new Integer(0));
				}
				for (Chunk chunk : foreign.chunksFalseNegatives.get(foreignKey))
					this.chunksFalseNegatives.get(foreignKey).add(chunk);
				this.falseNegatives.put(foreignKey, this.falseNegatives.get(foreignKey) +
					foreign.falseNegatives.get(foreignKey));
			}
		}
		
		this.globalTruePositives += foreign.globalTruePositives;
		this.globalFalsePositives += foreign.globalFalsePositives;
		this.globalFalseNegatives += foreign.globalFalseNegatives;
		this.recalculateStats();
	}
	
	/**
	 * Oblicz statystyki.
	 */
	private void recalculateStats() {
		for (String key : this.keys) {
			int tp = this.truePositives.containsKey(key) ? this.truePositives.get(key) : 0;
			int fp = this.falsePositives.containsKey(key) ? this.falsePositives.get(key) : 0;
			int fn = this.falseNegatives.containsKey(key) ? this.falseNegatives.get(key) : 0;
		
			// zabezpieczenie przed zerowym mianownikiem
			if (tp == 0) {
				this.precision.put(key, new Float(0));
				this.recall.put(key, new Float(0));
				this.fMeasure.put(key, new Float(0));
			}
			else {
				this.precision.put(key, new Float((float)tp) / (tp + fp));
				this.recall.put(key, new Float((float)tp) / (tp + fn));
				this.fMeasure.put(key, new Float((float)2 * tp) / (2 * tp + fp + fn));
			}
		}
	
		if (this.globalTruePositives == 0) {
			this.globalPrecision = 0;
			this.globalRecall = 0;
			this.globalFMeasure = 0;
		}
		else {
			this.globalPrecision = (float)this.globalTruePositives / 
				(this.globalTruePositives + this.globalFalsePositives);
			this.globalRecall = (float)this.globalTruePositives / 
				(this.globalTruePositives + this.globalFalseNegatives);
			this.globalFMeasure = 2 * (float)this.globalTruePositives / 
				(2 * this.globalTruePositives + this.globalFalsePositives + this.globalFalseNegatives);
		}
	}
	
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
		
		for (Chunk chunk : sortChunks(truePositives)) {
			Main.log(String.format("  TruePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Chunk chunk : sortChunks(falsePositives)) {
			Main.log(String.format("  FalsePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Chunk chunk : sortChunks(falseNegatives)) {
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
	
	private Chunk[] sortChunks(HashSet<Chunk> chunkSet) {
		int size = chunkSet.size();
		Chunk[] sorted = new Chunk[size];
		int idx = 0;
	    for (Chunk c : chunkSet)
	    	sorted[idx++] = c;
	    for (int i = 0; i < size; i++)
	    	for (int j = i+1; j < size; j++)
	    		if ((sorted[i].getBegin() > sorted[j].getBegin()) ||
	    			((sorted[i].getBegin() == sorted[j].getBegin()) &&
	    			(sorted[i].getEnd() > sorted[j].getEnd()))) {
	    			Chunk aux = sorted[i];
	    			sorted[i] = sorted[j];
	    			sorted[j] = aux;
	    		}
		return sorted;
	}
}
