package g419.liner2.daemon.action;

import g419.lib.cli.ParameterException;
import g419.liner2.daemon.FilebasedDaemonThread;
import g419.lib.cli.Action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import java.io.File;
import java.util.HashSet;

/**
 * Created by michal on 12/10/14.
 */
public class ActionFileBased extends ActionDaemon {

    public static final String OPTION_DB_PATH = "db_path";
    public static final String OPTION_URL = "url";

    File db_path;
    String tasker_url;

    public ActionFileBased() {
        super("file");
        this.setDescription("Starts daemon with file database.");

        OptionBuilder.withArgName("db_path");
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withDescription("path to database directory (with following folders created within: queue, progress, errors, done");
        options.addOption(OptionBuilder.create(OPTION_DB_PATH));

        OptionBuilder.withArgName("url");
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withDescription("url adress for tasker");
        options.addOption(OptionBuilder.create(OPTION_URL));
    }

    @Override
    public void parseOptions(CommandLine line) throws Exception {
        db_path = new File(line.getOptionValue(OPTION_DB_PATH));
        tasker_url = line.getOptionValue(OPTION_URL);
        if(!db_path.exists()){
            throw new ParameterException("Database directory does not exist:" + line.getOptionValue(OPTION_DB_PATH));
        }

    }

    @Override
    public void run() throws Exception {
        new FilebasedDaemonThread(db_path, tasker_url, max_threads).run();
    }
}
