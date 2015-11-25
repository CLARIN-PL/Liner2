package g419.liner2.daemon;

import org.apache.log4j.PropertyConfigurator;

import g419.lib.cli.ActionSelector;

/**
 * Run the module. 
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 */
public class Main {
    
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {

		PropertyConfigurator.configure("log4j.properties");

        StringBuilder info = new StringBuilder();        
        info.append("*-----------------------------------------------------------------------------------------------*");
        info.append("* A daemon for Liner2                                                                           *");
        info.append("*                                                                                               *");
        info.append("* Authors: Michał Marcińczuk (2011–2015)                                                        *");
        info.append("*    Past: Michał Krautforst (2015), Maciej Janicki (2011)                                      *");
        info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *");
        info.append("*                                                                                               *");
        info.append("*          G4.19 Research Group, Wrocław University of Technology                               *");
        info.append("*-----------------------------------------------------------------------------------------------*");

        ActionSelector main = new ActionSelector("./liner2-daemon");
        main.setCredits(info.toString());
        main.addActions("g419.liner2.daenib");
        main.run(args);         
    }
    
}
