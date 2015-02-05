package g419.liner2.daemon;

import g419.corpus.Logger;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.lib.cli.action.Action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 12/10/14.
 */
public class ActionSQL extends ActionDaemon{

    public static final String OPTION_DB_HOST = "db_host";
    public static final String OPTION_DB_NAME = "db_name";
    public static final String OPTION_DB_PASSWORD = "db_pass";
    public static final String OPTION_DB_PORT = "db_port";
    public static final String OPTION_DB_USER = "db_user";
    public static final String OPTION_DB_URI = "db_uri";
    public static final String OPTION_IP = "ip";
    public static final String OPTION_PORT = "p";

    String db_host = null, db_port = "3306", db_user = null,
            db_pass = "", db_name = null, ip = null;
    int port;

    public ActionSQL(){
        super("sql");
        this.setDescription("Starts daemon with sql database.");

        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("sql database host name")
                .create(OPTION_DB_HOST));
        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("sql database name")
                .create(OPTION_DB_NAME));
        options.addOption(OptionBuilder.withArgName("password").hasArg()
                .withDescription("sql database password")
                .create(OPTION_DB_PASSWORD));
        options.addOption(OptionBuilder.withArgName("db_port").hasArg()
                .withDescription("sql database port number")
                .create(OPTION_DB_PORT));
        options.addOption(OptionBuilder.withArgName("address").hasArg()
                .withDescription("sql database URI address")
                .create(OPTION_DB_URI));
        options.addOption(OptionBuilder.withArgName("username").hasArg()
                .withDescription("sql database user name ")
                .create(OPTION_DB_USER));
        options.addOption(OptionBuilder.withArgName("address").hasArg().isRequired()
                .withDescription("IP address for daemon")
                .create(OPTION_IP));
        options.addOption(OptionBuilder.withArgName("wsdl_port").hasArg().isRequired()
                .withDescription("port to listen on")
                .create(OPTION_PORT));


    }

    @Override
    public void parseOptions(CommandLine line) throws Exception {
        // getGlobal access data from db_uri parameter
        String db_uri = line.getOptionValue(OPTION_DB_URI);
        if (db_uri != null) {
            Pattern dbUriPattern = Pattern.compile("([^:@]*)(:([^:@]*))?@([^:@]*)(:([^:@]*))?/(.*)");
            Matcher dbUriMatcher = dbUriPattern.matcher(db_uri);
            if (dbUriMatcher.find()) {
                db_user = dbUriMatcher.group(1);
                if (dbUriMatcher.group(3) != null)
                    db_pass = dbUriMatcher.group(3);
                db_host = dbUriMatcher.group(4);
                if (dbUriMatcher.group(6) != null)
                    db_port = dbUriMatcher.group(6);
                db_name = dbUriMatcher.group(7);
            }
        }
        else{
            db_host = line.getOptionValue(OPTION_DB_HOST);
            db_port = line.getOptionValue(OPTION_DB_PORT);
            db_user = line.getOptionValue(OPTION_DB_USER);
            db_pass = line.getOptionValue(OPTION_DB_PASSWORD);
            db_name = line.getOptionValue(OPTION_DB_NAME);
        }

        if ((db_host == null) || (db_user == null) || (db_name == null)) {
            throw new ParameterException("Daemon mode: database access data required!");
        }

        ip = line.getOptionValue(OPTION_IP);
        if (this.ip == null)
            throw new ParameterException("Daemon mode: -ip (IP address) option is obligatory!");
        String optPort = line.getOptionValue(OPTION_PORT);
        if (optPort == null)
            throw new ParameterException("Daemon mode: -p (port) option is obligatory!");
        try {
            port = Integer.parseInt(optPort);
        } catch (NumberFormatException ex) {
            throw new ParameterException("Incorrect port number: " + optPort);
        }
    }

    @Override
    public void run() throws Exception {
        new SQLDaemonThread(db_host, db_port, db_user, db_pass, db_name, ip, port, max_threads).run();
    }
}
