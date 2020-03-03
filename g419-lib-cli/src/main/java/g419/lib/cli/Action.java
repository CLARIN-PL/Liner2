package g419.lib.cli;

import java.util.HashSet;
import org.apache.commons.cli.*;

public abstract class Action implements HasLogger {

  final protected String name;
  protected String description = "";
  protected String example = "";
  protected Options options = new Options();

  /**
   * @param name -- nazwa trybu pracy.
   */
  public Action(final String name) {
    this.name = name;
    options.addOption(CommonOptions.getVerboseOption());
  }

  /**
   * Set the description of the action.
   *
   * @param description -- the action description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * @param example
   */
  public void setExample(final String example) {
    this.example = example;
  }

  /**
   * Parse an array with options. With the set of options is valid the return true.
   * In other case return false. The getErrorMessage() can be used to obtain the
   * error message;
   *
   * @param args
   */
  public void parseOptions(final String[] args) throws Exception {
    final CommandLine line = new DefaultParser().parse(options, args);
    checkOptionRepetition(line);
    parseOptions(line);
  }

  public abstract void parseOptions(final CommandLine line) throws Exception;

  public Options getOptions() {
    return options;
  }

  /**
   * Returns name of the mode.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Get an example of the output produced by the action.
   *
   * @return
   */
  public String getExample() {
    return example;
  }

  public void printOptions() {
    final StringBuilder footer = new StringBuilder();
    if (getExample() != null && getExample().length() > 0) {
      footer.append("\n");
      footer.append("Example:\n");
      footer.append("--------\n");
      footer.append(getExample());
    }

    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(98);
    formatter.printHelp(
        String.format("./liner2-cli %s [options]",
            getName()), getDescription(), getOptions(), footer.toString());
  }

  abstract public void run() throws Exception;

  public void checkOptionRepetition(final CommandLine line) {
    try {
      final HashSet<String> argNames = new HashSet<>();
      for (final Option opt : line.getOptions()) {
        final String argName = opt.getOpt();
        if (!opt.hasArgs() && argNames.contains(argName)) {
          throw new ParameterException("Repeated argument: " + argName);
        } else {
          argNames.add(argName);
        }
      }
    } catch (final ParameterException e) {
      System.out.println(e);
      System.exit(1);
    }
  }

  public Action printHeader1(final String header) {
    return printLine().printHr1().printLine(header).printHr1();
  }

  public Action printHeader2(final String header) {
    return printLine().printLine(header).printHr2();
  }

  public Action printHr1() {
    System.out.println("====================================================================");
    return this;
  }

  public Action printHr2() {
    System.out.println("--------------------------------------------------------------------");
    return this;
  }

  public Action printLine(final String line) {
    System.out.println(line);
    return this;
  }

  public Action printLine() {
    System.out.println();
    return this;
  }
}
