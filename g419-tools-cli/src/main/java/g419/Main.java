package g419;

import g419.lib.cli.ActionSelector;
import java.io.File;
import org.apache.log4j.PropertyConfigurator;

public class Main {


  public static void main(final String[] args) throws Exception {

    final File log4jFile = new File("log4j.properties");
    if (log4jFile.exists()) {
      PropertyConfigurator.configure("log4j.properties");
    } else {
      System.err.println("log4j.properties not found in the current location");
      System.err.println("Expected location: " + log4jFile.getAbsoluteFile());
    }

    final StringBuilder info = new StringBuilder();
    info.append("*------------------------------------------------------------------------*\n");
    info.append("* A set of tools related to information extraction tasks.                *\n");
    info.append("*                                                                        *\n");
    info.append("* Authors: Michał Marcińczuk (2010–2020), Michał Krautforst (2013-2015)  *\n");
    info.append("* Contact: michal.marcinczuk@pwr.edu.pl / marcinczuk@gmail.com           *\n");
    info.append("*                                                                        *\n");
    info.append("* G4.19 Research Group, Wrocław University of Science and Technology     *\n");
    info.append("*------------------------------------------------------------------------*\n");

    final ActionSelector main = new ActionSelector("./tools-cli");
    main.setCredits(info.toString());
    main.addActions("g419.tools.action");
    main.run(args);
  }

}