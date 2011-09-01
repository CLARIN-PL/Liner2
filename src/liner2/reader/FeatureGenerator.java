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
		
//		while (!FeatureGenerator.input.ready())
//			if (FeatureGenerator.error.ready())
//				System.out.println(FeatureGenerator.error.readLine());
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
	}
	
	public static void close() throws IOException{
		if (FeatureGenerator.initialized) {
			FeatureGenerator.output.write("@EOF\n");
			FeatureGenerator.output.flush();
			FeatureGenerator.initialized = false;
		}
	}
	
	
	/*** Stare funkcje ***/
	
	/*private FeatureGenerator(String python_path, String path_nerd, String configuration) throws IOException{
		
		if ( path_nerd == null ){
			throw new Error("Path to NERD not set. Provide --nerd parameter");
		}
		
		String path_to_nerd = path_nerd + "/nerd.py"; 
		if ( !(new File(path_to_nerd).exists()) ){
			throw new Error("Incorrect path to NERD: " + path_to_nerd);
		}
		
		String[] envp=new String[1];
		envp[0]="PATH=" + path_nerd;
		String cmd = python_path + " " + path_to_nerd + " --batch";
		this.p = Runtime.getRuntime().exec(cmd, envp);
		this.configuration = configuration;
		
		this.input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		this.output = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		this.error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	}*/
	
/*	public ArrayList<String[]> expand(ArrayList<String[]> tokens) throws IOException{
		
		this.writeline("@FEATURES");
		this.writeline(this.configuration);
		this.writeline("-DOCSTART CONFIG FEATURES orth base ctag");
		
		for(String[] token : tokens){
			String line = String.format("%s %s %s O", token[0], token[1], token[2]);
			this.writeline(line);
		}
		this.writeline("@EOC");				
		this.output.flush();
		
		this.input.readLine();
		
		ArrayList<String[]> featuresExtended = new ArrayList<String[]>();
	
//		String error = null;
//		while ( (error = this.error.readLine()) != null)
//			System.out.println("!!: " + error);
				
		for (int i=0; i<tokens.size(); i++){
			String line = this.input.readLine().trim();
			line = line.substring(0, line.length()-2);
			//System.err.println(line);
			featuresExtended.add(line.split(" "));
		}*/
		/* XXX */
/*		this.input.readLine();
		this.input.readLine();
		this.input.readLine();
					
		return featuresExtended;
	}*/
	
//	public void close() throws IOException{
//		this.output.write("@EOF\n");
//		this.output.flush();
//	}
	
	private static void writeline(String line) throws IOException{
		FeatureGenerator.output.write(line + "\n");
	}

	
}
