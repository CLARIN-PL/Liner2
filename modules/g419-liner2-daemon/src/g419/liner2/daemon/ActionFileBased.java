package g419.liner2.daemon;

import g419.lib.cli.action.Action;
import g419.liner2.api.tools.ParameterException;
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

    File db_path;

    public ActionFileBased() {
        super("file");
        this.setDescription("Starts daemon with file database.");

        options.addOption(OptionBuilder.withArgName("db_path").hasArg().isRequired()
                .withDescription("path to database directory (with following folders created within: requests, processing, errors, results")
                .create(OPTION_DB_PATH));
    }

    @Override
    public void parseOptions(CommandLine line) throws Exception {
        db_path = new File(line.getOptionValue(OPTION_DB_PATH));
        if(!db_path.exists()){
            throw new ParameterException("Database directory does not exist:" + line.getOptionValue(OPTION_DB_PATH));
        }

    }

    @Override
    public void run() throws Exception {
        new FilebasedDaemonThread(db_path, max_threads).run();
    }
}
