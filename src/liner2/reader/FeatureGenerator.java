package liner2.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.LinerOptions;

/**
 * Klasa do generowania cech dla słów.
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 *
 */
public class FeatureGenerator {

	//private static ArrayList<String> features = null;
	private static String configuration = null;
	private static String path_python = null;
	private static String path_nerd = null;
	private static Process p = null;
	private static BufferedReader input = null;
	private static BufferedReader error = null;
	private static BufferedWriter output = null;
	
	public static final Pattern regexFeatureGeneralisation = Pattern.compile("hyp([0-9]+)");
    public static final Pattern regexFeatureDictionary = Pattern.compile("([^:]*)(:(.*))?:([^:]*)");
	
	//private static FeatureGenerator generator = null;
	private static boolean initialized = false;
	public static long initTime = 0;
	private static long time = 0;
		
	/**
	 * Prywatny konstruktor, aby zagwarantować singleton.
	 */
	private FeatureGenerator(ArrayList<String> features, String path_python, String path_nerd) throws IOException {
		
	}
	
	public static boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Funkcja inicjalizuje generator.
	 * @param features
	 */
	public static void initialize() throws IOException {
		//this.generator = new FeatureGenerator(features, path_python, path_nerd);
		
		if (FeatureGenerator.initialized)
			return;
		
		long timeInitStart = System.nanoTime();
		
		//FeatureGenerator.features = features;
		FeatureGenerator.path_python = LinerOptions.getOption(LinerOptions.OPTION_PYTHON);
		FeatureGenerator.path_nerd = LinerOptions.getOption(LinerOptions.OPTION_NERD);
		
		String path_to_nerd = FeatureGenerator.path_nerd + "/nerd.py"; 
		if ( !(new File(path_to_nerd).exists()) ) {
			throw new Error("Incorrect path to NERD: " + path_to_nerd);
		}
		
		String[] envp=new String[1];
		envp[0]="PATH=" + path_nerd;
		String cmd = path_python + " " + path_to_nerd + " --batch";
		FeatureGenerator.p = Runtime.getRuntime().exec(cmd, envp);
		
		// wygeneruj konfigurację
		String featureNames = "";
		String featureOthers = "";
		for (String feature : LinerOptions.get().features) {
			String featureName = feature;
			if (featureName.equals("syn"))
				featureOthers += " --generalization syn:::syn ";
			
			Matcher m1 = regexFeatureDictionary.matcher(feature);
			if (m1.find()) {
				featureOthers += " -g" + feature;
				featureName = m1.group(1);
			}
			
			Matcher m2 = regexFeatureGeneralisation.matcher(feature);
			if (m2.find())
				featureOthers += " --generalization hyp:" + m2.group(1) + ":full:" + feature + " ";
				
			featureNames += (featureNames.length() > 0 ? "," : "") + featureName;
		}
		FeatureGenerator.configuration = "-f " + featureNames + featureOthers;
		
		FeatureGenerator.input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		FeatureGenerator.output = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		FeatureGenerator.error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		FeatureGenerator.initialized = true;
		
		FeatureGenerator.time += System.nanoTime() - timeInitStart;
	}

	public static void generateFeatures(ParagraphSet ps) throws Exception {
		if (!FeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		ps.getAttributeIndex().update(LinerOptions.get().featureNames);
		for (Paragraph p : ps.getParagraphs())
			FeatureGenerator.generateFeatures(p, false);
	}
	
	/**
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param p
	 */
	public static void generateFeatures(Paragraph p, boolean updateIndex) throws Exception {
		if (!FeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		if (updateIndex)
			p.getAttributeIndex().update(LinerOptions.get().featureNames);
		for (Sentence s : p.getSentences())
			FeatureGenerator.generateFeatures(s, false);
	}
	
	/**
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param sentence
	 */
	public static void generateFeatures(Sentence sentence, boolean updateIndex) throws Exception {
		
		if (!FeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		long timeGenerateStart = System.nanoTime();
		
		if (updateIndex)
			sentence.getAttributeIndex().update(LinerOptions.get().featureNames);
		
		FeatureGenerator.writeline("@FEATURES");
		FeatureGenerator.writeline(FeatureGenerator.configuration);
		FeatureGenerator.writeline("-DOCSTART CONFIG FEATURES orth base ctag");
		
		for (Token token : sentence.getTokens()) {
			String line = String.format("%s %s %s O", token.getAttributeValue(0),
				token.getAttributeValue(1), token.getAttributeValue(2));
			FeatureGenerator.writeline(line);
		}

		FeatureGenerator.writeline("@EOC");	
		FeatureGenerator.output.flush();
		
		FeatureGenerator.input.readLine();
		
		for (Token token : sentence.getTokens()) {
			String line = FeatureGenerator.input.readLine().trim();
			line = line.substring(0, line.length() - 2);
			String[] featureValues = line.split(" ");
			for (int i = 0; i < featureValues.length; i++)
				token.setAttributeValue(i, featureValues[i]);
			
		}

//		String error = null;
//		while ( (error = FeatureGenerator.error.readLine()) != null)
//			System.out.println("!!: " + error);
				
		/* XXX */
		FeatureGenerator.input.readLine();
		FeatureGenerator.input.readLine();
		FeatureGenerator.input.readLine();

		if (sentence.getAttributeIndex().getIndex("agr1") > -1){
			int cas = sentence.getAttributeIndex().getIndex("case");
			int nmb = sentence.getAttributeIndex().getIndex("number");
			int gnd = sentence.getAttributeIndex().getIndex("gender");
			int agr1 = sentence.getAttributeIndex().getIndex("agr1");
			int agr2 = sentence.getAttributeIndex().getIndex("agr2");
			
			for (int i=0; i<sentence.getTokenNumber(); i++){
				
				String agr1_value = "NULL";
				String agr2_value = "NULL";
				
				if ( i>0 ){
					agr1_value = FeatureGenerator.agree3attributes(cas, nmb, gnd, 
							sentence.getTokens().get(i), sentence.getTokens().get(i-1));						
				}
				
				if ( i + 1 < sentence.getTokenNumber() ){
					agr2_value = FeatureGenerator.agree3attributes(cas, nmb, gnd, 
							sentence.getTokens().get(i), sentence.getTokens().get(i+1));						
				}

				sentence.getTokens().get(i).setAttributeValue(agr1, agr1_value);
				sentence.getTokens().get(i).setAttributeValue(agr2, agr2_value);
			}
		}
		
		FeatureGenerator.time += System.nanoTime() - timeGenerateStart;
		// przy pierwszym zdaniu
		if (FeatureGenerator.initTime == 0)
			FeatureGenerator.initTime = FeatureGenerator.time;
	}
	
	private static String agree3attributes(int a1, int a2, int a3, Token t1, Token t2){

		if ( t1.getAttributeValue(a1).equals("null") || t2.getAttributeValue(a1).equals("null")
				|| t1.getAttributeValue(a2).equals("null") || t2.getAttributeValue(a2).equals("null")
				|| t1.getAttributeValue(a3).equals("null") || t2.getAttributeValue(a3).equals("null"))
			return "NULL";
		else if ( t1.getAttributeValue(a1).equals(t2.getAttributeValue(a1))
				&& t1.getAttributeValue(a2).equals(t2.getAttributeValue(a2)) 
				&& t1.getAttributeValue(a3).equals(t2.getAttributeValue(a3)) )
			return "1";
		else
			return "0";
	}
	
	public static void close() throws IOException{
		if (FeatureGenerator.initialized) {
			FeatureGenerator.output.write("@EOF\n");
			FeatureGenerator.output.flush();
			FeatureGenerator.initialized = false;
		}
	}
	
	public static long getTime() {
		return FeatureGenerator.time;
	}
	
		
	private static void writeline(String line) throws IOException{
		FeatureGenerator.output.write(line + "\n");
	}

}
