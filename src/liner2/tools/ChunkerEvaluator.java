package liner2.tools;

import java.util.ArrayList;
import java.util.HashMap;

import liner2.chunker.Chunker;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;

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
	
	/**
	 * TODO
	 * @param chunker
	 */
	public ChunkerEvaluator(Chunker chunker){
		
	}
	
	/**
	 * 
	 */
	public void evaluate(Paragraph paragraphs){
		
	}
	
	/**
	 * TODO
	 * Precyzja dla wszystkich typów anotacji. = TP/(TP+FP)
	 * @return
	 */
	public float getPrecision(){
		return 0;
	}

	/**
	 * TODO
	 * Precyzja dla wskazanego typu anotacji. = TP/(TP+FN)
	 * @param type
	 * @return
	 */
	public float getPrecision(String type){
		return 0;
	}

	/**
	 * TODO 
	 * Kompletność dla wszystkich typów anotacji. 
	 */
	public float getRecall(){
		return 0;
	}

	/**
	 * TODO 
	 * Kompletność dla wskazanego typu anotacji. 
	 * @param type
	 */
	public float getRecall(String type){
		return 0;
	}

	/**
	 * TODO
	 * Średnia harmoniczna dla wszystkich typów anotacji. 
	 * @return
	 */
	public float getFMeasure(){
		return 0;
	}
	
	/**
	 * TODO
	 * Średnia harmoniczna dla wskazanego typu anotacji.
	 * @param type
	 * @return
	 */
	public float getFMeasure(String type){
		return 0;
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
		
	}
	
}
