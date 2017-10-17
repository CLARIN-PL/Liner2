package g419.spatial.action;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.log4j.Logger;

import g419.lib.cli.Action;
import g419.toolbox.sumo.Sumo;

public class ActionSumo extends Action {
	
	public ActionSumo() {
		super("sumo");
		this.setDescription("test SUMO core");
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
    }

	@Override
	public void run() throws Exception {
		Logger.getLogger(this.getClass()).info("Wczytuję SUMO ...");
		Sumo sumo = new Sumo(); 
		Logger.getLogger(this.getClass()).info("gotowe.");
		
		String line = null;
		System.out.print("Podaj nazwę konceptu: ");
		while ( ((line = System.console().readLine().trim())).length() >0 ){
			Set<String> words = new TreeSet<String>();
			words.addAll(sumo.getSuperclasses(line.toLowerCase()));
			System.out.println("Koncepty: " + String.join(", ", words));						
			System.out.println();
			System.out.print("Podaj nazwę konceptu: ");
		}
		
		
	}
	

}
