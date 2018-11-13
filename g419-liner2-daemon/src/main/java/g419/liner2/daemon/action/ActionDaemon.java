package g419.liner2.daemon.action;

import g419.lib.cli.Action;
import g419.liner2.core.LinerOptions;
import g419.liner2.daemon.utils.DaemonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;

/**
 * Created by michal on 12/10/14.
 */
public abstract class ActionDaemon extends Action {

    public static final String OPTION_MODELS = "m";
    public static final String OPTION_MODELS_LONG = "models";
    public static final String OPTION_MODELS_DESC = "multiple models config for daemon";
    public static final String OPTION_MODELS_ARG = "path";

    public static final String OPTION_MAX_THREADS = "T";
    public static final String OPTION_MAX_THREADS_LONG = "max_threads";
    public static final String OPTION_MAX_THREADS_DESC = "maximum number of processing threads";
    public static final String OPTION_MAX_THREADS_ARG = "num";

    int maxThreads = 1;

    public ActionDaemon(final String name) {
        super(name);
        options.addOption(getModelsOption());
        options.addOption(getThreadsOption());
    }

    private static Option getModelsOption() {
        return Option.builder(OPTION_MODELS).longOpt(OPTION_MODELS_LONG).required()
                .hasArg().argName(OPTION_MODELS_ARG).desc(OPTION_MODELS_DESC).build();
    }

    private static Option getThreadsOption() {
        return Option.builder(OPTION_MAX_THREADS).longOpt(OPTION_MAX_THREADS_LONG)
                .hasArg().argName(OPTION_MAX_THREADS_ARG).desc(OPTION_MAX_THREADS_DESC).build();
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        parseModelsIni(line.getOptionValue(OPTION_MODELS));
        maxThreads = io.vavr.control.Option.of(line.getOptionValue(OPTION_MAX_THREADS))
                .map(Integer::parseInt)
                .getOrElse(1);
        parseOptions(line);
    }

    @Override
    public void run() throws Exception {

    }

    public void parseModelsIni(final String iniFile) throws Exception {
        final String iniPath = new File(iniFile).getAbsoluteFile().getParentFile().getAbsolutePath();
        DaemonOptions.getGlobal().models = new HashMap<>();
        final Ini ini = new Ini(new File(iniFile));
        DaemonOptions.getGlobal().defaultModel = ini.get("main", "default");
        final Profile.Section modelsDef = ini.get("models");
        for (final String model : modelsDef.keySet()) {
            final LinerOptions modelConfig = new LinerOptions();
            modelConfig.parseModelIni(modelsDef.get(model).replace("{INI_DIR}", iniPath));
            DaemonOptions.getGlobal().models.put(model, modelConfig);
        }
    }

    @Override
    public void printOptions() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(98);
        formatter.printHelp(
                String.format("./liner2-daemon %s [options]", getName()), options);
    }
}
