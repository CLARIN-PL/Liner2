package g419.liner2.api.tools;


import g419.corpus.ConsolePrinter;
import g419.corpus.structure.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
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
    // >>>

    //<<< grupowanie anotacji o granicach wystepujacych juz w korpusie
    private HashMap<String, Float> precisionExistingRangeOnly = new HashMap<String, Float>();
    private HashMap<String, Float> fMeasureExistingRangeOnly = new HashMap<String, Float>();
    private HashMap<String, Integer> falsePositivesExistingRangeOnly = new HashMap<String, Integer>();
    //>>>
	
	private int sentenceNum = 0;
    private String currentDocId = "";

    /* Określa, czy szczegóły oceny mają by ukryte */
	private boolean quiet = false;
	
	/* Określa, czy mają być wypisywane wyłącznie zdania z błędami (FP i/lub FN). */
	private boolean errorsOnly = false;

	private boolean checkLemma = false;
	
	private List<Pattern> patterns = new ArrayList<Pattern>();
	
    private Set<String> types = new HashSet<String>();
	
	public ChunkerEvaluator(List<Pattern> types) {
		this.patterns = types;
	}

    public ChunkerEvaluator(List<Pattern> types, boolean quiet) {
        this.patterns = types;
        this.quiet = quiet;
    }

    /**
     * 
     * @param types Lista typów anotacji do oceny
     * @param quiet Jeżeli true, to logi z oceny nie zostają drukowane.
     * @param errorsOnly Jeżeli true, to wypisuje tylko zdania z błędami.
     */
    public ChunkerEvaluator(List<Pattern> types, boolean quiet, boolean errorsOnly) {
        this.patterns = types;
        this.quiet = quiet;
        this.errorsOnly = errorsOnly;
    }

	public void setCheckLemma(boolean checkLemma){
		this.checkLemma = checkLemma;
	}
	/**
	 * Ocenia nerowanie całego dokumentu.
	 */
	public void evaluate(Document document, HashMap<Sentence, AnnotationSet> chunkings, HashMap<Sentence, AnnotationSet> chunkigsRef){
        currentDocId = document.getName();
		for ( Sentence sentence : document.getSentences()){
			this.evaluate(sentence, chunkings.get(sentence), chunkigsRef.get(sentence));
		}
	}
	
	/**
	 * 
	 */
	public void evaluate(Sentence sentence, AnnotationSet chunking, AnnotationSet chunkingRef) {

		// tylko na potrzeby wyświetlania szczegółów
		Set<Annotation> myTruePositives = new HashSet<Annotation>();
        this.sentenceNum++;
		// Wybierz anotacje do oceny jeżeli został określony ich typ
		Set<Annotation> chunkingRefSet = new HashSet<Annotation>();
		Set<Annotation> chunkingSet = new HashSet<Annotation>();

        Set<String> newTypes = chunkingRef.getAnnotationTypes();
        newTypes.addAll(chunking.getAnnotationTypes());
        newTypes.removeAll(this.types);
        updateTypes(newTypes);

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
				if (trueChunk.equals(testedChunk) &&
						(!checkLemma || trueChunk.getLemma().equals(testedChunk.getLemma()))   ) {
					// wpisz klucz do tablicy, jeśli jeszcze nie ma
					if (!this.chunksTruePositives.containsKey(testedChunk.getType())) {
						this.chunksTruePositives.put(testedChunk.getType(), new ArrayList<Annotation>());
						//this.keys.add(testedChunk.getType());
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
				//this.keys.add(testedChunk.getType());
			}
			// dodaj do istniejącego klucza
			this.chunksFalsePositives.get(testedChunk.getType()).add(testedChunk);
            Boolean  truePositiveSkippedChannelCheck = false;
            for(Annotation trueChunk : trueChunkSet)
                if(testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()) &&
						(!checkLemma || trueChunk.getLemma().equals(testedChunk.getLemma())))   {
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
				//this.keys.add(trueChunk.getType());
			}
			// dodaj do istniejącego klucza
			this.chunksFalseNegatives.get(trueChunk.getType()).add(trueChunk);

            Boolean  truePositiveSkippedChannelCheck = false;
            for(Annotation testedChunk : testedChunkSet)
                if(testedChunk.getTokens().equals(trueChunk.getTokens()) && testedChunk.getSentence().equals(trueChunk.getSentence()) &&
						(!checkLemma || trueChunk.getLemma().equals(testedChunk.getLemma())))   {
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
                    break;
                }
            }
        }
        
		if (!this.quiet && (!this.errorsOnly || testedChunkSet.size() > 0 || trueChunkSet.size() > 0 )){
			printSentenceResults(sentence, sentence.getId(), myTruePositives, testedChunkSet, trueChunkSet);
		}
				
	}

	/**
	 * 
	 * @param newTypes
	 */
    private void updateTypes(Set<String> newTypes){
        for(String newType: newTypes){
            if(!this.types.contains(newType)){
                if(this.patterns != null && !this.patterns.isEmpty()){
                    for(Pattern patt: this.patterns){
                        if(patt.matcher(newType).find()){
                            this.chunksTruePositives.put(newType,  new ArrayList<Annotation>());
                            this.chunksFalsePositives.put(newType,  new ArrayList<Annotation>());
                            this.chunksFalseNegatives.put(newType,  new ArrayList<Annotation>());
                            this.precisionExistingRangeOnly.put(newType,  0.0f);
                            this.fMeasureExistingRangeOnly.put(newType, 0.0f);
                            this.types.add(newType);
                            break;
                        }

                    }
                }
                else{
                    this.chunksTruePositives.put(newType,  new ArrayList<Annotation>());
                    this.chunksFalsePositives.put(newType,  new ArrayList<Annotation>());
                    this.chunksFalseNegatives.put(newType,  new ArrayList<Annotation>());
                    this.precisionExistingRangeOnly.put(newType,  0.0f);
                    this.fMeasureExistingRangeOnly.put(newType, 0.0f);
                    this.types.add(newType);
                }
            }
        }
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

	/**
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getSpanPrecision(){
		float tp = this.globalTruePositivesRangeOnly;
		float fp = this.globalFalsePositivesRangeOnly;		
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
	 * Kompletność dla rozpoznawania granic anotacji.
	 */
	public float getSpanRecall(){
		float tp = this.globalTruePositivesRangeOnly;
		float fn = this.globalFalseNegativesRangeOnly;	
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

	/**
	 * Średnia harmoniczna dla rozpoznawania granic anotacji.
	 * @return
	 */
	public float getSpanFMeasure(){
		float p = this.getSpanPrecision();
		float r = this.getSpanRecall();
		return (p+r)==0 ? 0 : (2*p*r)/(p+r);
	}
	
	public int getTruePositive(){
		int tp = 0;
		for ( String type : this.chunksTruePositives.keySet()){
			tp += this.getTruePositive(type);
		}
		return tp;
	}

	public int getTruePositive(String type){
		return this.chunksTruePositives.containsKey(type) ? this.chunksTruePositives.get(type).size() : 0;
	}
	
	public int getFalsePositive(){
		int fp = 0;
		for ( String type : this.chunksFalsePositives.keySet()){
			fp += this.getFalsePositive(type);
		}
		return fp;
	}

	public int getFalsePositive(String type){
		return this.chunksFalsePositives.containsKey(type) ? this.chunksFalsePositives.get(type).size() : 0;
	}
	
	public int getFalseNegative(){
		int fn = 0;
		for ( String type : this.chunksFalseNegatives.keySet()){
			fn += this.getFalseNegatives(type);
		}
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
	 * Annotation        &amp;   TP &amp;   FP &amp;   FN &amp; Precision &amp;   Recall &amp;  F$_1$ \\
	 * \hline
	 * ROAD_NAM          &amp;  147 &amp;    8 &amp;   36 &amp;   94.84\% &amp;  80.33\% &amp;  86.98\%
	 * PERSON_LAST_NAM   &amp;  306 &amp;    9 &amp;   57 &amp;   97.14\% &amp;  84.30\% &amp;  90.27\%
	 * PERSON_FIRST_NAM  &amp;  319 &amp;    3 &amp;   29 &amp;   99.07\% &amp;  91.67\% &amp;  95.22\%
	 * COUNTRY_NAM       &amp;  160 &amp;   51 &amp;   36 &amp;   75.83\% &amp;  81.63\% &amp;  78.62\%
	 * CITY_NAM          &amp;  841 &amp;   65 &amp;   75 &amp;   92.83\% &amp;  91.81\% &amp;  92.32\%
	 * \hline
	 * *TOTAL*           &amp; 1773 &amp;  136 &amp;  233 &amp;   92.88\% &amp;  88.38\% &amp;  90.67\%
	 * 
	 */
	public void printResults(){
		String header = "        Annotation                     &      TP &      FP &      FN & Precision & Recall  & F$_1$   \\\\";
		String line = "        %-30s & %7d & %7d & %7d &   %6.2f%% & %6.2f%% & %6.2f%% \\\\";
		
		this.printHeader("Exact match evaluation -- annotation span and types evaluation");
		ConsolePrinter.println(header);
		ConsolePrinter.println("\\hline");
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(this.types);
		Collections.sort(keys);
		for (String key : keys) {
			ConsolePrinter.println(
				String.format(line, key, 
				this.getTruePositive(key), this.getFalsePositive(key), this.getFalseNegatives(key), 
				this.getPrecision(key)*100, this.getRecall(key)*100, this.getFMeasure(key)*100));
		}
		ConsolePrinter.println("\\hline");
		ConsolePrinter.println(String.format(line, "*TOTAL*", 
			this.getTruePositive(), this.getFalsePositive(), this.getFalseNegative(),
			this.getPrecision()*100, this.getRecall()*100, this.getFMeasure()*100));
		ConsolePrinter.println("\n");

		
		this.printHeader("Annotation span evaluation (annotation types are ignored)");
        ConsolePrinter.println(header);
        ConsolePrinter.println("\\hline");
		ConsolePrinter.println(String.format(line, "*TOTAL*", 
        		this.globalTruePositivesRangeOnly, this.globalFalsePositivesRangeOnly, this.globalFalseNegativesRangeOnly,
                this.getSpanPrecision()*100, this.getSpanRecall()*100, this.getSpanFMeasure()*100));
		ConsolePrinter.println("\n");		
    }
	
	public void printHeader(String header){
        ConsolePrinter.println("======================================================================================");
        ConsolePrinter.println("# " + header);
        ConsolePrinter.println("======================================================================================");
	}
	
	/**
	 * Dołącza do danych zawartość innego obiektu ChunkerEvaluator.
	 */	
	public void join(ChunkerEvaluator foreign) {
			
		for (String foreignKey : foreign.types) {
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
			for (Annotation chunk : source.get(key)){
				target.get(key).add(chunk);
			}
		}		
	}
	
    /**
     * Wypisuje wynik porównania rozpoznanych anotacji z wzorcowym zbiorem anotacji dla danego zdania.
     * @param sentence
     * @param paragraphId
     * @param truePositives
     * @param falsePositives
     * @param falseNegatives
     */
	private void printSentenceResults(Sentence sentence, String paragraphId, Set<Annotation> truePositives, Set<Annotation> falsePositives, Set<Annotation> falseNegatives) {
		String sentenceHeader = "(ChunkerEvaluator) Sentence #" + this.sentenceNum + " from " + currentDocId;
		if (paragraphId != null){
			sentenceHeader += " from " + paragraphId;
		}
		ConsolePrinter.println(sentenceHeader + "\n");
		StringBuilder tokenOrths = new StringBuilder();
		StringBuilder tokenNums = new StringBuilder();
		int idx = 0;
		for (Token token : sentence.getTokens()) {
			String tokenOrth = token.getOrth();
			String tokenNum = "" + (++idx);
			tokenOrth += StringUtils.repeat("_", Math.max(0, tokenNum.length() - tokenOrth.length()));
			tokenNum += StringUtils.repeat("_", Math.max(0, tokenOrth.length() - tokenNum.length()));
			tokenOrths.append( tokenOrth + " " );
			tokenNums.append( tokenNum + " " );
		}
		ConsolePrinter.log("Text  : " + tokenOrths.toString().trim());
		ConsolePrinter.log("Tokens: " + tokenNums.toString().trim());
		ConsolePrinter.log("");
		ConsolePrinter.log("Chunks:");
		
		AnnotationIndex fnIndex = new AnnotationIndex(falseNegatives);

		for (Annotation chunk : Annotation.sortChunks(truePositives)) {
			ConsolePrinter.log(String.format("  TruePositive %s [%d,%d] = %s (confidence=%.2f)", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk), chunk.getConfidence()));
		}
		for (Annotation chunk : Annotation.sortChunks(falsePositives)) {
			String errorType = "incorrect boundry";
			Annotation correctCategory = fnIndex.get(chunk.getBegin(), chunk.getEnd());
			if ( correctCategory != null ){
				if (!chunk.getType().equals(correctCategory.getType()))
					errorType = String.format("incorrect category: %s => %s", chunk.getType(), correctCategory.getType());
				else if (!chunk.getLemma().equals(correctCategory.getLemma()))
					errorType = String.format("incorrect lemma: %s => %s", chunk.getLemma(), correctCategory.getLemma());
			}
			ConsolePrinter.log(String.format("  FalsePositive %s [%d,%d] = %s (confidence=%.2f) (%s)", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk), chunk.getConfidence(), errorType));
		}
		for (Annotation chunk : Annotation.sortChunks(falseNegatives)) {
			ConsolePrinter.log(String.format("  FalseNegative %s [%d,%d] = %s", chunk.getType(), chunk.getBegin()+1,
				chunk.getEnd()+1, printChunk(chunk)));
		}
		
		ConsolePrinter.log("");
		ConsolePrinter.log("Features:", true);
		StringBuilder featuresHeader = new StringBuilder("       ");
		for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++){
			featuresHeader.append(String.format("[%d]_%s ", i+1, sentence.getAttributeIndex().getName(i)));
		}
		ConsolePrinter.log(featuresHeader.toString(), true);
		
		idx = 0;
		for (Token token : sentence.getTokens()) {
			StringBuilder tokenFeatures = new StringBuilder(String.format("  %3d) ", ++idx));
			for (int i = 0; i < token.getNumAttributes(); i++){
				tokenFeatures.append(String.format("[%d]_%s ", i+1, token.getAttributeValue(i)));
			}
			ConsolePrinter.log(tokenFeatures.toString(), true);
		}
		ConsolePrinter.log("", true);
	}
	
	/**
	 * 
	 * @param chunk
	 * @return
	 */
	private String printChunk(Annotation chunk) {
		List<Token> tokens = chunk.getSentence().getTokens();
		StringBuilder result = new StringBuilder();
		for (int i = chunk.getBegin(); i <= chunk.getEnd(); i++)
			result.append(tokens.get(i).getOrth() + " ");
		return result.toString().trim();
	}
	
}
