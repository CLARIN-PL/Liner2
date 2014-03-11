package liner2.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
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
	private HashMap<String, ArrayList<Annotation>> chunksTruePositives = new HashMap<String, ArrayList<Annotation>>();
	private HashMap<String, ArrayList<Annotation>> chunksFalsePositives = new HashMap<String, ArrayList<Annotation>>();
	private HashMap<String, ArrayList<Annotation>> chunksFalseNegatives = new HashMap<String, ArrayList<Annotation>>();
	
    //<<< grupowanie bez uwzglądnienia kanałów anotacji
    private int globalTruePositivesRangeOnly = 0;
    private int globalFalsePositivesRangeOnly = 0;
    private int globalFalseNegativesRangeOnly = 0;

    private float globalPrecisionRangeOnly = 0.0f;
    private float globalRecallRangeOnly = 0.0f;
    private float globalFMeasureRangeOnly = 0.0f;
    // >>>

    //<<< grupowanie anotacji o granicach wystepujacych juz w korpusie
    private HashMap<String, Float> precisionExistingRangeOnly = new HashMap<String, Float>();
    private HashMap<String, Float> fMeasureExistingRangeOnly = new HashMap<String, Float>();

	private HashMap<String, ArrayList<Annotation>> rangeTruePositives = new HashMap<String, ArrayList<Annotation>>();
	private HashMap<String, ArrayList<Annotation>> rangeFalsePositives = new HashMap<String, ArrayList<Annotation>>();
	private HashMap<String, ArrayList<Annotation>> rangeFalseNegatives = new HashMap<String, ArrayList<Annotation>>();

    private HashMap<String, Integer> falsePositivesExistingRangeOnly = new HashMap<String, Integer>();

    private float globalPrecisionExistingRangeOnly = 0.0f;
    private float globalFMeasureExistingRangeOnly = 0.0f;

    private int globalFalsePositivesExistingRangeOnly = 0;
    //>>>
	
	private int sentenceNum = 0;
	
	private boolean quiet = false;		// print sentence results?
	private HashSet<String> keys = new HashSet<String>();
	private HashSet<String> types = new HashSet<String>();
	
	
	/**
	 * @param chunker
	 */
	public ChunkerEvaluator() {
	}
	
	public ChunkerEvaluator(HashSet<String> types) {
		this.types = types;
	}
	
	/**
	 * Ocenia nerowanie całego dokumentu.
	 * @param set
	 */
	public void evaluate(ArrayList<Sentence> order, HashMap<Sentence, AnnotationSet> chunkings, HashMap<Sentence, AnnotationSet> chunkigsRef){
		for ( Sentence sentence : order){
			this.evaluate(sentence, chunkings.get(sentence), chunkigsRef.get(sentence));
		}
	}
	
	/**
	 * 
	 */
	private void evaluate(Sentence sentence, AnnotationSet chunking, AnnotationSet chunkingRef) {

		chunking.filter(LinerOptions.getGlobal().filters);
	
		// tylko na potrzeby wyświetlania szczegółów
		HashSet<Annotation> myTruePositives = new HashSet<Annotation>();
		this.sentenceNum++;
	
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
					// wpisz klucz do tablicy, jeśli jeszcze nie ma
					if (!this.chunksTruePositives.containsKey(testedChunk.getType())) {
						this.chunksTruePositives.put(testedChunk.getType(), new ArrayList<Annotation>());
						this.keys.add(testedChunk.getType());
					}
					// dodaj do istniejącego klucza
					this.chunksTruePositives.get(testedChunk.getType()).add(testedChunk);
                    this.globalTruePositivesRangeOnly += 1;
					// oznacz jako TruePositive
					myTruePositives.add(testedChunk);
					trueChunkSet.remove(trueChunk);
					testedChunkSet.remove(testedChunk);
				}

		// w testedChunkSet zostały falsePositives
		for (Annotation testedChunk : testedChunkSet) {
			// wpisz klucz do tablicy, jeśli jeszcze nie ma
			if (!this.chunksFalsePositives.containsKey(testedChunk.getType())) {
				this.chunksFalsePositives.put(testedChunk.getType(), new ArrayList<Annotation>());
				this.keys.add(testedChunk.getType());
			}
			// dodaj do istniejącego klucza
			this.chunksFalsePositives.get(testedChunk.getType()).add(testedChunk);
            Boolean  truePositiveSkippedChannelCheck = false;
            for(Annotation trueChunk : trueChunkSet)
                if(testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()))   {
                    this.globalTruePositivesRangeOnly += 1;
                    truePositiveSkippedChannelCheck = true;
                    break;
                }
            if(!truePositiveSkippedChannelCheck)
                this.globalFalsePositivesRangeOnly += 1;
		}
				
		// w trueChunkSet zostały falseNegatives
		for (Annotation trueChunk : trueChunkSet) {
			// wpisz klucz do tablicy, jeśli jeszcze nie ma
			if (!this.chunksFalseNegatives.containsKey(trueChunk.getType())) {
				this.chunksFalseNegatives.put(trueChunk.getType(), new ArrayList<Annotation>());
				this.keys.add(trueChunk.getType());
			}
			// dodaj do istniejącego klucza
			this.chunksFalseNegatives.get(trueChunk.getType()).add(trueChunk);

            Boolean  truePositiveSkippedChannelCheck = false;
            for(Annotation testedChunk : testedChunkSet)
                if(testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()))   {
                    truePositiveSkippedChannelCheck = true;
                    break;
                }
            if(!truePositiveSkippedChannelCheck)
                this.globalFalseNegativesRangeOnly += 1;
		}

        // zlicznie falsePositives dla anotacji o granicah wystepujacych we wzorcowym korpusie
        for (Annotation testedChunk : testedChunkSet) {
            for (Annotation trueChunk : trueChunkSetIter) {
                if(testedChunk.getTokens().equals(trueChunk.getTokens())){
                    // wpisz klucz do tablicy, jeśli jeszcze nie ma
                    if (!this.falsePositivesExistingRangeOnly.containsKey(testedChunk.getType())) {
                        this.falsePositivesExistingRangeOnly.put(testedChunk.getType(), new Integer(0));
                    }
                    // dodaj do istniejącego klucza
                    this.falsePositivesExistingRangeOnly.put(testedChunk.getType(),
                            this.falsePositivesExistingRangeOnly.get(testedChunk.getType()) + 1);
                    this.globalFalsePositivesExistingRangeOnly += 1;
                    break;
                }
            }
        }
        
		if (!this.quiet)
			printSentenceResults(sentence, sentence.getId(), myTruePositives, testedChunkSet, trueChunkSet);
				
	}
	
	/**
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getPrecision(){
		float tp = this.getTruePositive();
		float fp = this.getFalsePositive();		
		return (tp+fp)==0 ? 0 : tp / (tp+fp);
	}

	/**
	 * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
	 * @param type
	 * @return
	 */
	public float getPrecision(String type){
		float tp = this.getTruePositive(type);
		float fp = this.getFalsePositive(type);		
		return (tp+fp)==0 ? 0 : tp / (tp+fp);
	}
	
	public boolean getQuiet() {
		return this.quiet;
	}

	/**
	 * Kompletność dla wszystkich typów anotacji. 
	 */
	public float getRecall(){
		float tp = this.getTruePositive();
		float fn = this.getFalseNegative();		
		return (tp+fn)==0 ? 0 : tp / (tp+fn);
	}

	/**
	 * Kompletność dla wskazanego typu anotacji. 
	 * @param type
	 */
	public float getRecall(String type){
		float tp = this.getTruePositive(type);
		float fn = this.getFalseNegatives(type);		
		return (tp+fn)==0 ? 0 : tp / (tp+fn);
	}

	/**
	 * Średnia harmoniczna dla wszystkich typów anotacji. 
	 * @return
	 */
	public float getFMeasure(){
		float p = this.getPrecision();
		float r = this.getRecall();
		return (p+r)==0 ? 0 : (2*p*r)/(p+r);
	}
	
	/**
	 * Średnia harmoniczna dla wskazanego typu anotacji.
	 * @param type
	 * @return
	 */
	public float getFMeasure(String type){
		float p = this.getPrecision(type);
		float r = this.getRecall(type);
		return (p+r)==0 ? 0 : (2*p*r)/(p+r);
	}

	public int getTruePositive(){
		int tp = 0;
		for ( String type : this.chunksTruePositives.keySet())
			tp += this.getTruePositive(type);
		return tp;
	}

	public int getTruePositive(String type){
		return this.chunksTruePositives.containsKey(type) ? this.chunksTruePositives.get(type).size() : 0;
	}
	
	public int getFalsePositive(){
		int fp = 0;
		for ( String type : this.chunksFalsePositives.keySet())
			fp += this.getFalsePositive(type);
		return fp;
	}

	public int getFalsePositive(String type){
		return this.chunksFalsePositives.containsKey(type) ? this.chunksFalsePositives.get(type).size() : 0;
	}
	
	public int getFalseNegative(){
		int fn = 0;
		for ( String type : this.chunksFalseNegatives.keySet())
			fn += this.getFalseNegatives(type);
		return fn;
	}

	public int getFalseNegatives(String type){
		return this.chunksFalseNegatives.containsKey(type) ? this.chunksFalseNegatives.get(type).size() : 0;
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
		String header = "        Annotation           &   TP &   FP &   FN & Precision & Recall  & F$_1$   \\\\";
		String line = "        %-20s & %4d & %4d & %4d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\";
		
		this.printHeader("Exact match evaluation -- annotation span and types evaluation");
		System.out.println(header);
		System.out.println("\\hline");
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(this.keys);
		Collections.sort(keys);
		for (String key : keys) {
			System.out.println(
				String.format(line, key, 
				this.getTruePositive(key), this.getFalsePositive(key), this.getFalseNegatives(key), 
				this.getPrecision(key)*100, this.getRecall(key)*100, this.getFMeasure(key)*100));
		}
		System.out.println("\\hline");
		System.out.println(String.format(line, "*TOTAL*", 
			this.getTruePositive(), this.getFalsePositive(), this.getFalseNegative(),
			this.getPrecision()*100, this.getRecall()*100, this.getFMeasure()*100));
		System.out.println("\n");

		
		this.printHeader("Exact match evaluation -- annotation span evaluation (annotation types ignored)");
        System.out.println(header);
        System.out.println("\\hline");
		System.out.println(String.format(line, "*TOTAL*", 
        		this.globalTruePositivesRangeOnly, this.globalFalsePositivesRangeOnly, this.globalFalseNegativesRangeOnly,
                this.globalPrecisionRangeOnly*100, this.globalRecallRangeOnly*100, this.globalFMeasureRangeOnly*100));
		System.out.println("\n");
        
		
		this.printHeader("Exact match evaluation -- annotation span evaluation for each type");
        System.out.println(header);
        System.out.println("\\hline");
        for (String key : keys) {
            int fp = this.falsePositivesExistingRangeOnly.containsKey(key) ? this.falsePositivesExistingRangeOnly.get(key) : 0;

            float p = this.precisionExistingRangeOnly.containsKey(key) ? this.precisionExistingRangeOnly.get(key) : 0;
            float r = this.getRecall(key);
            float f = this.fMeasureExistingRangeOnly.containsKey(key) ? this.fMeasureExistingRangeOnly.get(key) : 0; 
                      
            System.out.println(String.format(line, key, 
            		this.getTruePositive(key), fp, this.getFalseNegatives(key), 
            		p*100, r*100, f*100));
        }
        System.out.println("\\hline");
        System.out.println(String.format(line, "*TOTAL*", this.getTruePositive(),
                this.globalFalsePositivesExistingRangeOnly, this.getFalseNegative(),
                this.globalPrecisionExistingRangeOnly*100, this.getRecall()*100, this.globalFMeasureExistingRangeOnly*100));
		System.out.println("\n");
    }
	
	public void printHeader(String header){
        System.out.println("======================================================================================");
        System.out.println("# " + header);
        System.out.println("======================================================================================");
	}
	
	/**
	 * Dołącza do danych zawartość innego obiektu ChunkerEvaluator.
	 */	
	public void join(ChunkerEvaluator foreign) {
			
		for (String foreignKey : foreign.keys) {
			
			if (!this.keys.contains(foreignKey))
				this.keys.add(foreignKey);
						
			this.joinMaps(foreignKey, this.chunksTruePositives, foreign.chunksTruePositives);
			this.joinMaps(foreignKey, this.chunksFalsePositives, foreign.chunksFalsePositives);
			this.joinMaps(foreignKey, this.chunksFalseNegatives, foreign.chunksFalseNegatives);
		}
	}
	
	private void joinMaps(String key, HashMap<String, ArrayList<Annotation>> target, HashMap<String, ArrayList<Annotation>> source){
		if (source.containsKey(key)) {
			if (!target.containsKey(key)) {
				target.put(key, new ArrayList<Annotation>());
			}
			for (Annotation chunk : source.get(key))
				target.get(key).add(chunk);
		}		
	}
	
    private void recalculateRangeOnlyStats() {
        if (this.globalTruePositivesRangeOnly == 0) {
            this.globalPrecisionRangeOnly = 0;
            this.globalRecallRangeOnly = 0;
            this.globalFMeasureRangeOnly = 0;
        }
        else {
            this.globalPrecisionRangeOnly = (float)this.globalTruePositivesRangeOnly /
                    (this.globalTruePositivesRangeOnly + this.globalFalsePositivesRangeOnly);
            this.globalRecallRangeOnly = (float)this.globalTruePositivesRangeOnly /
                    (this.globalTruePositivesRangeOnly + this.globalFalseNegativesRangeOnly);
            this.globalFMeasureRangeOnly = 2 * (float)this.globalTruePositivesRangeOnly /
                    (2 * this.globalTruePositivesRangeOnly + this.globalFalsePositivesRangeOnly + this.globalFalseNegativesRangeOnly);
        }
    }

    private void recalculateExistingRangeOnlyStats() {
        for (String key : this.keys) {
            int tp = this.getTruePositive(key);
            int fp = this.falsePositivesExistingRangeOnly.containsKey(key) ? this.falsePositivesExistingRangeOnly.get(key) : 0;
            int fn = this.getFalseNegatives(key);

            // zabezpieczenie przed zerowym mianownikiem
            if (tp == 0) {
                this.precisionExistingRangeOnly.put(key, new Float(0));
                this.fMeasureExistingRangeOnly.put(key, new Float(0));
            }
            else {
                this.precisionExistingRangeOnly.put(key, new Float((float)tp) / (tp + fp));
                this.fMeasureExistingRangeOnly.put(key, new Float((float)2 * tp) / (2 * tp + fp + fn));
            }
        }
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
		
		for (Annotation chunk : Annotation.sortChunks(truePositives)) {
			Main.log(String.format("  TruePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Annotation chunk : Annotation.sortChunks(falsePositives)) {
			Main.log(String.format("  FalsePositive %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		for (Annotation chunk : Annotation.sortChunks(falseNegatives)) {
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
	
	/**
	 * 
	 * @param chunk
	 * @return
	 */
	private String printChunk(Annotation chunk) {
		ArrayList<Token> tokens = chunk.getSentence().getTokens();
		StringBuilder result = new StringBuilder();
		for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++)
			result.append(tokens.get(i).getFirstValue() + " ");
		return result.toString().trim();
	}
	
}
