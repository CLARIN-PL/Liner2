package g419.tools.action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.tools.utils.Counter;

public class ActionMorfeuszInflectionByProbability extends Action {
	
	private final String OPTION_MORFEUSZ_LONG = "morfeusz";
	private final String OPTION_MORFEUSZ = "m";
	
	private String morfeusz = null;
	
	public ActionMorfeuszInflectionByProbability() {
		super("morfeusz-inflection-probability");
		this.setDescription("tworzy słownik alternacji końcówek dla formy odmienionej i formy bazowej");
        this.options.addOption(Option.builder(OPTION_MORFEUSZ).longOpt(OPTION_MORFEUSZ_LONG).hasArg().argName("filename")
        		.desc("ścieżka do słownika Morfeusz").required().required().build());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        
        this.morfeusz = line.getOptionValue(OPTION_MORFEUSZ_LONG);
    }

	@Override
	public void run() throws Exception {
		
		List<MorphEntry> morfeusz = this.loadMorfeusz(this.morfeusz);
		Map<String, List<MorphEntry>> entriesByCtag = new HashMap<String, List<MorphEntry>>();		

		// Zgrupuj po tagach morfologicznych
		for ( MorphEntry entry : morfeusz ){
			List<MorphEntry> entries = entriesByCtag.get(entry.getCtag());
			if ( entries == null ){
				entries = new ArrayList<MorphEntry>();
				entriesByCtag.put(entry.getCtag(), entries);
			}
			entries.add(entry);
		}
		
		for ( String ctag : entriesByCtag.keySet()){
			Map<String, Counter> inflections = new HashMap<String, Counter>();
			List<MorphEntry> entries = entriesByCtag.get(ctag);
			for ( MorphEntry entry : entries ){
				String form = entry.getOrth();
				String base = entry.getBase();
				if ( !form.equals(base) ){
					int i=0;
					while ( i< form.length() && i < base.length() && form.charAt(i) == base.charAt(i) ){
						i++;
						if ( i > 1 ){
							String template = form.substring(i) + " " + base.substring(i);
							
							Counter counter = inflections.get(template);
							if ( counter == null ){
								counter = new Counter();
								inflections.put(template, counter);
							}
							counter.increment();
						}
					}
				}
			}
			
			Set<String> patterns = new TreeSet<String>();
			patterns.addAll(inflections.keySet());
			for ( String pattern : patterns ){
				if ( inflections.get(pattern).getValue() > 1 ){
					String[] cols = pattern.split(" ");					
					String formEnding = cols[0];
					String baseEnding = cols.length < 2 ? "" : cols[1];
					double prob = this.evaluateInflection(formEnding, baseEnding, entries);
					System.out.println(String.format("%s %s %d %2.2f", ctag, pattern, inflections.get(pattern).getValue(), prob));
				}
			}			
		}		
	}

	private double evaluateInflection(String formEnding, String baseEnding, List<MorphEntry> entries){
		int tp = 0;
		int fp = 0;
		for ( MorphEntry entry : entries ){
			String base = this.inflect(entry.getOrth(), formEnding, baseEnding);
			if ( base != null ){
				if ( base.equals(entry.getBase())){
					tp++;
				}
				else{
					fp++;
				}
			}
		}
		if ( tp + fp == 0 ){
			return 0;
		}
		else{
			return (float)(tp)/(float)(tp+fp);
		}
	}
	
	
	private String inflect(String form, String formEnding, String baseEnding){
		if ( form.endsWith(formEnding) ){
			return form.substring(0, form.length()-formEnding.length()) + baseEnding;
		}
		else{
			return null;
		}
	}
	
	
	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private List<MorphEntry> loadMorfeusz(String path) throws IOException{
		List<MorphEntry> entries = new ArrayList<MorphEntry>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		boolean morph = true;
		String line = null;
		Map<String, Counter> inflections = new HashMap<String, Counter>();
		while ( (line = br.readLine()) != null ){
			line = line.trim();
			
			if ( line.contains("<COPYRIGHT>") ){
				morph = false;
			}
			
			if ( line.startsWith("#") ){
				// Pomiń komentarze
			}
			else if ( morph ){
				String[] cols = line.split("\t");
				String orth = cols[0];
				String base = cols[1].split(":")[0];
				String ctag = cols[2].trim();
				MorphEntry entry = new MorphEntry(orth, base, ctag);
				entries.add(entry);
			}
			else if ( line.equals("#</COPYRIGHT>")){
				morph = true;
			}
		}
		br.close();		
		return entries;
	}
	
	/**
	 * 
	 * @author czuk
	 *
	 */
	protected class MorphEntry{
		private String orth = null;
		private String base = null;
		private String ctag = null;
		
		public MorphEntry(String orth, String base, String ctag){
			this.orth = orth;
			this.base = base;
			this.ctag = ctag;
		}
		
		public String getBase(){
			return this.base;
		}
		
		public String getOrth(){
			return this.orth;
		}
		
		public String getCtag(){
			return this.ctag;
		}
	}
}
