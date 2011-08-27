package liner2.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import liner2.structure.Paragraph;
import liner2.structure.Sentence;

public class FeatureGenerator {

	private String configuration = null;
	private Process p = null;
	private static BufferedReader input = null;
	private static BufferedReader error = null;
	private static BufferedWriter output = null;
	
	private static FeatureGenerator generator = null;
		
	/**
	 * Prywatny konstruktor, aby zagwarantować singleton.
	 */
	private void FeatureGenerator(){
		
	}
	
	/**
	 * Funkcja inicjalizuje generator.
	 * @param features
	 */
	public static void initialize(ArrayList<String> features){
		
	}

	/**
	 * TODO
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param sentence
	 */
	public static void generateFeatures(Paragraph p) {
		if ( FeatureGenerator.generator == null )
			return;
		
		for (Sentence s : p.getSentences())
			FeatureGenerator.generateFeatures(s);
	}
	
	/**
	 * TODO
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param sentence
	 */
	public static void generateFeatures(Sentence sentence){
		if ( FeatureGenerator.generator == null )
			return;
		
	}
	
	public static void close() throws IOException{
		if (FeatureGenerator.generator != null){
			FeatureGenerator.output.write("@EOF\n");
			FeatureGenerator.output.flush();
		}
	}
	
	
	/*** Stare funkcje ***/
	
	private FeatureGenerator(String python_path, String path_nerd, String configuration) throws IOException{
		
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
	}
	
	public ArrayList<String[]> expand(ArrayList<String[]> tokens) throws IOException{
		
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
		}
		/* XXX */
		this.input.readLine();
		this.input.readLine();
		this.input.readLine();
					
		return featuresExtended;
	}
	
//	public void close() throws IOException{
//		this.output.write("@EOF\n");
//		this.output.flush();
//	}
	
	private void writeline(String line) throws IOException{
		this.output.write(line + "\n");
	}

	
}
