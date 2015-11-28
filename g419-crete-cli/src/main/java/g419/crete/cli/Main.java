package g419.crete.cli;

import g419.lib.cli.ActionSelector;

public class Main {
    	
	/**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {
		StringBuilder info = new StringBuilder();
		info.append("*-----------------------------------------------------------------------------------------------*\n");
		info.append("* Coreference Resolution and Enhanced Toolkit with Evaluation.                                  *\n");
		info.append("*                                                                                               *\n");
		info.append("* Authors: Adam Kaczmarek (2014–2015)                                                           *\n");
		info.append("* Contact: adam.kaczmarek@pwr.wroc.pl                                                           *\n");
		info.append("*                                                                                               *\n");
		info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
		info.append("*-----------------------------------------------------------------------------------------------*\n");

		ActionSelector main = new ActionSelector("./crete-cli");
		main.setCredits(info.toString());
		main.addActions("g419.crete.cli.action");
		main.run(args);
	}
    
}
