package g419.liner2.daemon.action;

import g419.lib.cli.ParameterException;
import g419.lib.cli.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.daemon.DaemonOptions;
import g419.liner2.daemon.DaemonThread;

import org.apache.commons.cli.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by michal on 12/10/14.
 */
public abstract class ActionDaemon extends Action{

    public static final String OPTION_MODELS = "m";
    public static final String OPTION_MODELS_LONG = "models";

    public static final String OPTION_MAX_THREADS = "max_threads";

    int max_threads;

    public ActionDaemon(String name){
        super(name);

        OptionBuilder.withArgName("models");
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt(OPTION_MODELS_LONG);
        OptionBuilder.withDescription("multiple models config for daemon");
        options.addOption(OptionBuilder.create(OPTION_MODELS));
        
        OptionBuilder.withArgName("number");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximum number of processing threads");
        options.addOption(OptionBuilder.create(OPTION_MAX_THREADS));
    }
    
    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        HashSet<String> argNames = new HashSet<String>();
        for(Option opt: line.getOptions()){
            String argName = opt.getOpt();
            if(argNames.contains(argName)){
                throw new ParameterException("Repeated argument:" + argName);
            }
            else{
                argNames.add(argName);
            }
        }
        parseDefault(line);
        parseModelsIni(line.getOptionValue(OPTION_MODELS));
        String threads = line.getOptionValue(OPTION_MAX_THREADS);
        max_threads = threads != null ? Integer.parseInt(threads) : DaemonThread.DEFAULT_MAX_THREADS;
        parseOptions(line);
    }

    public abstract void parseOptions(CommandLine line) throws Exception;

    @Override
    public void run() throws Exception {

    }

    public void parseModelsIni(String iniFile) throws Exception {
        String iniPath = new File(iniFile).getAbsoluteFile().getParentFile().getAbsolutePath();
        DaemonOptions.getGlobal().models = new HashMap<String, LinerOptions>();
        Ini ini = new Ini(new File(iniFile));
        DaemonOptions.getGlobal().defaultModel = ini.get("main", "default");
        Profile.Section modelsDef = ini.get("models");
        for(String model: modelsDef.keySet()){
            LinerOptions modelConfig = new LinerOptions();
            modelConfig.parseModelIni(modelsDef.get(model).replace("{INI_DIR}", iniPath));
            DaemonOptions.getGlobal().models.put(model, modelConfig);
        }
    }

    @Override
    public void printOptions(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(98);
        formatter.printHelp(
                String.format("./liner2-daemon %s [options]", this.getName()), this.options);
    }
}
