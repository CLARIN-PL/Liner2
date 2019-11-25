package g419.spatial;

import g419.lib.cli.ActionSelector;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class Main {

  /**
   * Module entry point.
   *
   * @param args
   */
  public static void main(final String[] args) throws Exception {

    final File log4jFile = new File("log4j.properties");
    if (log4jFile.exists()) {
      PropertyConfigurator.configure(log4jFile.getAbsolutePath());
    } else {
      System.err.println("log4j.properties not found in the current location");
      System.err.println("Expected location: " + log4jFile.getAbsoluteFile());
    }

    final StringBuilder info = new StringBuilder();

    info.append("*-----------------------------------------------------------------------------------------------*\n");
    info.append("* Tools for spatial expression processing and automatic recognition                             *\n");
    info.append("*                                                                                               *\n");
    info.append("* Authors: Michał Marcińczuk (2015–2019)                                                        *\n");
    info.append("* Contact: michal.marcinczuk@pwr.edu.pl                                                         *\n");
    info.append("*                                                                                               *\n");
    info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
    info.append("*-----------------------------------------------------------------------------------------------*\n");

    final ActionSelector main = new ActionSelector("./spatial-cli");
    main.setCredits(info.toString());
    main.addActions("g419.spatial.action");
    main.run(args);
  }

}