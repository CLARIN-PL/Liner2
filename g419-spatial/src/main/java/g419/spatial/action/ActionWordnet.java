package g419.spatial.action;

import g419.lib.cli.action.Action;
import g419.spatial.io.Wordnet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

public class ActionWordnet extends Action {
	
	// ToDo: przerobić na parametr 
	private String wordnet = "/nlp/resources/plwordnet/plwordnet_2_1_0/plwordnet_2_1_0_pwn_format";
	
	public ActionWordnet() {
		super("wordnet");
		this.setDescription("test wordnet api");
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
    }

	@Override
	public void run() throws Exception {
		Wordnet w = new Wordnet(this.wordnet);
	
		for (String word : w.getHyponymWords("instytucja", 1))
			System.out.println(word);		
	}
	

}
