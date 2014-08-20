package g419.liner2.daemon;

import g419.liner2.api.LinerOptions;
import g419.liner2.api.tools.ParameterException;
import org.apache.commons.cli.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by michal on 6/25/14.
 */
public class DaemonOptions extends LinerOptions {

    static protected final LinerOptions daemonOptions = new DaemonOptions();

    Options options = new Options();

    public static final String OPTION_MODELS = "models";
    public static final String OPTION_DB_HOST = "db_host";
    public static final String OPTION_DB_NAME = "db_name";
    public static final String OPTION_DB_PASSWORD = "db_pass";
    public static final String OPTION_DB_PORT = "db_port";
    public static final String OPTION_DB_USER = "db_user";
    public static final String OPTION_DB_URI = "db_uri";
    public static final String OPTION_IP = "ip";
    public static final String OPTION_MAX_THREADS = "max_threads";
    public static final String OPTION_PORT = "p";
    public static final String OPTION_VERBOSE = "v";
    public static final String OPTION_VERBOSE_LONG = "verbose";

    public DaemonOptions(){
        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("database host name")
                .create(OPTION_DB_HOST));
        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("database name")
                .create(OPTION_DB_NAME));
        options.addOption(OptionBuilder.withArgName("password").hasArg()
                .withDescription("database password")
                .create(OPTION_DB_PASSWORD));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("database port number")
                .create(OPTION_DB_PORT));
        options.addOption(OptionBuilder.withArgName("address").hasArg()
                .withDescription("database URI address")
                .create(OPTION_DB_URI));
        options.addOption(OptionBuilder.withArgName("username").hasArg()
                .withDescription("database user name ")
                .create(OPTION_DB_USER));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("maximum number of processing threads")
                .create(OPTION_MAX_THREADS));
        options.addOption(OptionBuilder.withArgName("address").hasArg()
                .withDescription("IP address for daemon")
                .create(OPTION_IP));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("port to listen on")
                .create(OPTION_PORT));
        options.addOption(OptionBuilder.withArgName("models").hasArg()
                .withDescription("multiple models config for daemon")
                .create(OPTION_MODELS));
        options.addOption(OptionBuilder
                .withLongOpt(OPTION_VERBOSE_LONG).withDescription("print help")
                .create(OPTION_VERBOSE));
    }

    public static LinerOptions getGlobal(){
        return daemonOptions;
    }

    public void parse(String[] args) throws Exception{
        CommandLine line = new GnuParser().parse(this.options, args);
        Iterator<?> i_options = line.iterator();
        while (i_options.hasNext()) {
            Option o = (Option) i_options.next();
            if(o.getOpt().equals(OPTION_VERBOSE)){
                DaemonOptions.getGlobal().verbose = true;
            }
            else if(o.getOpt().equals(OPTION_MODELS)){
                parseModelsIni(o.getValue());
            }
            else{
                this.properties.setProperty(o.getOpt(), o.getValue());
            }
        }


        LinerOptions.getGlobal().verbose = DaemonOptions.getGlobal().verbose;
    }


    private void parseModelsIni(String iniFile) throws Exception {
        String iniPath = new File(iniFile).getAbsoluteFile().getParentFile().getAbsolutePath();
        models = new HashMap<String, LinerOptions>();
        Ini ini = new Ini(new File(iniFile));
        this.defaultModel = ini.get("main", "default");
        Profile.Section modelsDef = ini.get("models");
        for(String model: modelsDef.keySet()){
            LinerOptions modelConfig = new LinerOptions();
            modelConfig.parseModelIni(modelsDef.get(model).replace("{INI_DIR}", iniPath));
            models.put(model, modelConfig);
        }
    }


    protected void printModes(){
        System.out.println("Liner2 service daemon - listen and process requests from a given database");
        System.out.println("Parameteres: -ip, -p, -db_*, -models");
    }

}
