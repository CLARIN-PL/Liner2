package g419.liner2.cli;

import g419.lib.cli.ActionSelector;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;


/**
 * Run the module.
 *
 * @author Michał Marcińczuk
 */
public class Main {

  /**
   * Here the story begins.
   */
  public static void main(String[] args) throws Exception {

    File log4jFile = new File("log4j.properties");
    if (log4jFile.exists()) {
      PropertyConfigurator.configure(log4jFile.getAbsolutePath());
    } else {
      System.err.println("log4j.properties not found in the current location");
      System.err.println("Expected location: " + log4jFile.getAbsoluteFile());
    }

    StringBuilder info = new StringBuilder();
    info.append("*-----------------------------------------------------------------------------------------------*\n");
    info.append("* A framework for multitask sequence labeling, including: named entities, temporal expressions. *\n");
    info.append("*                                                                                               *\n");
    info.append("* Authors: Michał Marcińczuk (2010–2016), Jan Kocoń (2014–2016), Adam Kaczmarek (2014–2015)     *\n");
    info.append("*    Past: Michał Krautforst (2013-2015), Dominik Piasecki (2013), Maciej Janicki (2011)        *\n");
    info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *\n");
    info.append("*                                                                                               *\n");
    info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
    info.append("*-----------------------------------------------------------------------------------------------*\n");

    ActionSelector main = new ActionSelector("./liner2-cli");
    main.setCredits(info.toString());
    main.addActions("g419.liner2.cli.action");
    main.run(args);
  }

}
