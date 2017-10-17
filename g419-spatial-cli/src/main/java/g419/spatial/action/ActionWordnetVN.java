package g419.spatial.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.lib.cli.Action;
import g419.toolbox.wordnet.Wordnet3;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

public class ActionWordnetVN extends Action {
	
	// ToDo: przerobić na parametr 
	private String wordnet = null;
	
	public ActionWordnetVN() {
		super("wordnet-vn");
		this.setDescription("test wordnet core");
		
		this.options.addOption(Option.builder("w").hasArg().argName("FILENAME").required()
						.desc("ścieżka do katalogu z wordnetem w formacie Princeton").longOpt("wordnet").build());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        
        if ( line.hasOption("w") ){
        	this.wordnet = line.getOptionValue("w");
        }
        
        parseDefault(line);
    }

	@Override
	public void run() throws Exception {
		Logger.getLogger(this.getClass()).info("Wczytuję wordnet ...");
		Wordnet3 w = new Wordnet3(this.wordnet);
		Logger.getLogger(this.getClass()).info("gotowe.");
		
		for ( PrincetonDataRaw synset : w.getSynsets() ){
			Set<PrincetonDataRaw> directs = w.getDirectSynsets(synset, "sn");
			for ( PrincetonDataRaw direct : directs ){
				if ( direct != null && synset != null ){
					for ( String word1 : w.getLexicalUnits(synset) ){
						for ( String word2 : w.getLexicalUnits(direct) ){
							System.out.println(String.format("v2n\t%s\t%s", word1, word2));
						}
					}
				}
			}
		}
		
//		w.getAllSynsets(synset, relation)
//		
//		String line = null;
//		System.out.print("Podaj słowo: ");
//		while ( ((line = System.console().readLine().trim())).length() >0 ){
//			List<PrincetonDataRaw> synsets = w.getSynsets(line);
//			
//			for ( PrincetonDataRaw synset : synsets ){
//				System.out.println();
//				System.out.println("Synset: " + String.join(", ", w.getLexicalUnits(synset)) + "; Domena=" + synset.domain);
//				Set<String> holonyms = new TreeSet<String>();
//				for ( PrincetonDataRaw s : w.getHolonyms(synset) ){
//					holonyms.addAll(w.getLexicalUnits(s));
//				}
//				System.out.println("Holonimy: " + String.join(", ", holonyms));
//
//				Set<String> hiperonyms = new TreeSet<String>();
//				for ( PrincetonDataRaw s : w.getAllSynsets(synset, Wordnet3.REL_HYPERNYM) ){
//					hiperonyms.addAll(w.getLexicalUnits(s));
//				}
//				System.out.println("Hiperonimy: " + String.join(", ", hiperonyms));
//			}
//						
//			System.out.println();
//			System.out.print("Podaj słowo: ");
//		}
		
		
	}
	

}
