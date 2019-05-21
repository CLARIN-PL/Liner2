package g419.liner2.daemon.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.daemon.utils.RabbitMqWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class ActionRabbitMq extends Action {

  public static final String OPTION_QUEUE = "q";
  public static final String OPTION_QUEUE_LONG = "queue";
  public static final String OPTION_QUEUE_DESC = "name of the RabbitMQ queue (liner2 is the default name)";
  public static final String OPTION_QUEUE_ARG = "name";
  public static final String OPTION_QUEUE_DEFAULT = "liner2-input";

  public static final String OPTION_HOSTNAME = "H";
  public static final String OPTION_HOSTNAME_LONG = "hostname";
  public static final String OPTION_HOSTNAME_DESC = "RabbitMQ hostname";
  public static final String OPTION_HOSTNAME_ARG = "name";
  public static final String OPTION_HOSTNAME_DEFAULT = "localhost";

  public static final String OPTION_OUTPUT_QUEUE_DEFAULT = "liner2-output";

  String inputQueueName;
  String outputQueueName;
  String modelPath = null;
  String inputFormat;
  String rabbitMqHostname = "localhost";

  public ActionRabbitMq() {
    super("rabbitmq");
    setDescription("Starts Liner2 daemon processing requests from a RabbitMQ queue");
    options.addOption(getHostnameOption());
    options.addOption(getQueueNameOption());
    options.addOption(CommonOptions.getModelFileOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
  }

  private static Option getQueueNameOption() {
    return Option.builder(OPTION_QUEUE).longOpt(OPTION_QUEUE_LONG)
        .hasArg().argName(OPTION_QUEUE_ARG).desc(OPTION_QUEUE_DESC).build();
  }

  private static Option getHostnameOption() {
    return Option.builder(OPTION_HOSTNAME).longOpt(OPTION_HOSTNAME_LONG)
        .hasArg().argName(OPTION_HOSTNAME_ARG).desc(OPTION_HOSTNAME_DESC).build();
  }


  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputQueueName = line.getOptionValue(OPTION_QUEUE, OPTION_QUEUE_DEFAULT);
    outputQueueName = OPTION_OUTPUT_QUEUE_DEFAULT;
    modelPath = line.getOptionValue(CommonOptions.OPTION_MODEL);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    rabbitMqHostname = line.getOptionValue(OPTION_HOSTNAME, OPTION_HOSTNAME_DEFAULT);
  }

  @Override
  public void run() throws Exception {
    final RabbitMqWorker worker =
        new RabbitMqWorker(rabbitMqHostname, inputQueueName, outputQueueName, modelPath, inputFormat);
    (new Thread(worker)).start();
  }
}
