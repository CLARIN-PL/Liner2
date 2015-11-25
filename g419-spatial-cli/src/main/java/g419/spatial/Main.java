package g419.spatial;

import org.apache.log4j.PropertyConfigurator;

import g419.lib.cli.ActionSelector;

public class Main {

	/**
	 * Module entry point.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");

        StringBuilder info = new StringBuilder();        
        
        info.append("*-----------------------------------------------------------------------------------------------*\n");
        info.append("* Tools for spatial expression recognition and related                                          *\n");
        info.append("*                                                                                               *\n");
        info.append("* Authors: Michał Marcińczuk (2015)                                                             *\n");
        info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *\n");
        info.append("*                                                                                               *\n");
        info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
        info.append("*-----------------------------------------------------------------------------------------------*\n");
                		
        ActionSelector main = new ActionSelector("./spatial-cli");
        main.setCredits(info.toString());
        main.addActions("g419.spatial.action");
        main.run(args);    		
	}
	
}