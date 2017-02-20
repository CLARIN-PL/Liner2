package g419.liner2.daemon;

import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.corpus.ConsolePrinter;

import org.apache.commons.cli.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by michal on 6/25/14.
 */
public class DaemonOptions extends LinerOptions {

    static protected final DaemonOptions daemonOptions = new DaemonOptions();

    public HashMap<String, LinerOptions> models = null;
    public String defaultModel = null;

    public static DaemonOptions getGlobal(){
        return daemonOptions;
    }

//
//    protected void printModes(){
//        System.out.println("Liner2 service daemon - listen and process requests from a given database");
//        System.out.println("Daemon works in 2 modes: with sql or filebased badabase.");
//        System.out.println("Required parameteres: -db_type, -models");
//        System.out.println("Required parameteres for file mode: -db_path");
//        System.out.println("Required parameteres for sql mode: -ip, -p, -db_*");
//        System.out.println();
//        HelpFormatter formatter = new HelpFormatter();
//        formatter.setWidth(98);
//        formatter.printHelp("./liner2-daemon [options]", this.options);
//    }

}
