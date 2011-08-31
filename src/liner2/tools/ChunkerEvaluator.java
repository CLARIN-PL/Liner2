package liner2.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import liner2.chunker.Chunker;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;

/**
 * TODO
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
	
	private HashSet<String> keys = new HashSet<String>();
	private Chunker chunker = null;
	
	/**
	 * TODO
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
			// każdy HashSet w dwóch kopiach - jedna do iterowania, druga do modyfikacji
			HashSet<Chunk> trueChunkSet = sentence.getChunks();
			HashSet<Chunk> trueChunkSetIter = new HashSet<Chunk>(trueChunkSet);
			sentence.setChunking(new Chunking(sentence));
			HashSet<Chunk> testedChunkSet = this.chunker.chunkSentence(sentence).chunkSet();
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
						// usuń z danych
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
		}
		
		recalculateStats();
	}
	
	/**
	 * TODO
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getPrecision(){
		return globalPrecision;
	}

	/**
	 * TODO
	 * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
	 * @param type
	 * @return
	 */
	public float getPrecision(String type){
		return precision.get(type);
	}

	/**
	 * TODO 
	 * Kompletność dla wszystkich typów anotacji. 
	 */
	public float getRecall(){
		return globalRecall;
	}

	/**
	 * TODO 
	 * Kompletność dla wskazanego typu anotacji. 
	 * @param type
	 */
	public float getRecall(String type){
		return recall.get(type);
	}

	/**
	 * TODO
	 * Średnia harmoniczna dla wszystkich typów anotacji. 
	 * @return
	 */
	public float getFMeasure(){
		return globalFMeasure;
	}
	
	/**
	 * TODO
	 * Średnia harmoniczna dla wskazanego typu anotacji.
	 * @param type
	 * @return
	 */
	public float getFMeasure(String type){
		return fMeasure.get(type);
	}
	
	/**
	 * TODO
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
			+ " Precision & Recall & F$_1$  \\\\");
		System.out.println("\\hline");
		for (String key : this.keys) {
			int tp = this.truePositives.containsKey(key) ? this.truePositives.get(key) : 0;
			int fp = this.falsePositives.containsKey(key) ? this.falsePositives.get(key) : 0;
			int fn = this.falseNegatives.containsKey(key) ? this.falseNegatives.get(key) : 0;
			
			System.out.println(String.format("%-20s & %4d & %4d & %4d &"
				+ "    %5.2f%% & %5.2f%% & %5.2f%% \\\\", key, tp, fp, fn,
				this.precision.get(key), this.recall.get(key), this.fMeasure.get(key)));
		}
		System.out.println("\\hline");
		System.out.println(String.format("*TOTAL*              & %4d & %4d & %4d &"
			+ "    %5.2f%% & %5.2f%% & %5.2f%%", this.globalTruePositives,
			this.globalFalsePositives, this.globalFalseNegatives,
			this.globalPrecision, this.globalRecall, this.globalFMeasure));
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
			this.globalPrecision = this.globalTruePositives / 
				(this.globalTruePositives + this.globalFalsePositives);
			this.globalRecall = this.globalTruePositives / 
				(this.globalTruePositives + this.globalFalseNegatives);
			this.globalFMeasure = 2 * this.globalTruePositives / 
				(2 * this.globalTruePositives + this.globalFalsePositives + this.globalFalseNegatives);
		}
	}
}
