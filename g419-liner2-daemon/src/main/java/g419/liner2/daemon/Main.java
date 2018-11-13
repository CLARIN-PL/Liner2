package g419.liner2.daemon;

import g419.lib.cli.ActionSelector;
import g419.liner2.daemon.action.ActionFileBased;
import g419.liner2.daemon.action.ActionRabbitMq;
import g419.liner2.daemon.action.ActionSQL;
import org.apache.log4j.PropertyConfigurator;

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
    public static void main(final String[] args) throws Exception {

        PropertyConfigurator.configure("log4j.properties");

        final StringBuilder info = new StringBuilder();
        info.append("*-----------------------------------------------------------------------------------------------*\n");
        info.append("* A daemon for Liner2                                                                           *\n");
        info.append("*                                                                                               *\n");
        info.append("* Authors: Michał Marcińczuk (2011–2016)                                                        *\n");
        info.append("*    Past: Michał Krautforst (2015), Maciej Janicki (2011)                                      *\n");
        info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *\n");
        info.append("*                                                                                               *\n");
        info.append("*          G4.19 Research Group, Wrocław University of Technology                               *\n");
        info.append("*-----------------------------------------------------------------------------------------------*\n");

        final ActionSelector main = new ActionSelector("./liner2-daemon");
        main.setCredits(info.toString());
        main.add(new ActionSQL());
        main.add(new ActionFileBased());
        main.add(new ActionRabbitMq());
        main.run(args);
    }

}
