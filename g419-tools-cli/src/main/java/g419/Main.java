package g419;

import g419.lib.cli.ActionSelector;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * Selektor akcji dla g419-tools.
 * 
 * @author Michał Marcińczuk
 *
 */
public class Main{

	
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {      

		File log4jFile = new File("log4j.properties");
		if ( log4jFile.exists() ){		
			PropertyConfigurator.configure("log4j.properties");
		}
		else{
			System.err.println("log4j.properties not found in the current location");
			System.err.println("Expected location: " + log4jFile.getAbsoluteFile());
		}
		
        StringBuilder info = new StringBuilder();        
        info.append("*-----------------------------------------------------------------------------------------------*\n");
        info.append("* Set of tools related to information extraction tasks.                                         *\n");
        info.append("*                                                                                               *\n");
        info.append("* Authors: Michał Marcińczuk (2010–2015), Michał Krautforst (2013-2015)                         *\n");
        info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *\n");
        info.append("*                                                                                               *\n");
        info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
        info.append("*-----------------------------------------------------------------------------------------------*\n");
        		
        ActionSelector main = new ActionSelector("./tools");
        main.setCredits(info.toString());
        main.addActions("g419.tools.action");
        main.run(args);         
    }
  
}