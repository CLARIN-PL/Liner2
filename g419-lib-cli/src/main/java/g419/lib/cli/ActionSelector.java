package g419.lib.cli;

import g419.corpus.TerminateException;
import g419.corpus.io.UnknownFormatException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

public class ActionSelector {

  /**
   * List of known actions.
   */
  private final HashMap<String, Action> actions = new HashMap<>();

  private String credits = null;
  private String cliScriptName = null;

  public ActionSelector(final String cliScriptName) {
    this.cliScriptName = cliScriptName;
  }

  public void setCredits(final String credits) {
    this.credits = credits;
  }

  public void run(final String[] args) {
    if (args.length == 0) {
      System.out.println(credits);
      System.out.println();
      System.out.println("[Error] Tool not given. \n\nUse one of the following tools:");
      printTools();
      System.out.println();
      System.out.println(String.format("usage: %s [action] [options]", cliScriptName));
      System.out.println();
    } else {
      final String name = args[0];
      final Action tool = actions.get(name);
      if (tool == null) {
        System.out.println(credits);
        System.out.println();
        System.out.println(String.format("[Error] Tool '%s' does not exist. \n\nUse one of the following tools:", name));
        printTools();
        System.out.println();
        System.out.println(String.format("usage: %s [action] [options]", cliScriptName));
        System.out.println();
      } else {
        try {
          tool.parseOptions(args);
          tool.run();
        } catch (final ParameterException e) {
          System.err.println("Error: " + e.getMessage());
        } catch (final UnknownFormatException e) {
          System.err.println("Error: " + e.getMessage());
        } catch (final TerminateException e) {
          System.err.println("Error: " + e.getMessage());
        } catch (final ParseException | MissingOptionException | UnrecognizedOptionException e) {
          System.out.println(credits);
          System.out.println();
          System.out.println(String.format("[Option error] %s\n", e.getMessage()));
          tool.printOptions();
          System.out.println();
        } catch (final Exception e) {
          System.err.println("Error: " + e.getMessage());
          System.err.println(StringUtils.repeat("-", 60));
          e.printStackTrace();
          System.err.println(StringUtils.repeat("-", 60));
        }
      }
    }
  }

  /**
   * Dodaje instancje klas rozszerzające klasę Action znajdujących się we wskazanym pakiecie.
   *
   * @param packageName
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws IOException
   */
  public void addActions(final String packageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    for (final Action action : ActionFinder.find(packageName)) {
      add(action);
    }
  }

  /**
   * Register a new action. The action must have unique name.
   */
  public void add(final Action action) {
    actions.put(action.getName(), action);
  }

  /**
   * Prints a list of available actions.
   */
  public void printTools() {
    int maxLength = 1;
    for (final String name : actions.keySet()) {
      maxLength = Math.max(maxLength, name.length());
    }

    final String lineFormat = " - %-" + maxLength + "s -- %s";

    final Set<String> actionNames = new TreeSet<>();
    for (final Action tool : actions.values()) {
      actionNames.add(tool.getName());
    }

    for (final String name : actionNames) {
      final Action tool = actions.get(name);
      final List<String> lines = splitIntoLines(tool.getDescription(), 90 - maxLength);
      System.out.println(String.format(lineFormat, tool.getName(), lines.size() == 0 ? "" : lines.get(0)));
      for (int i = 1; i < lines.size(); i++) {
        System.out.println(StringUtils.repeat(" ", maxLength + 7) + lines.get(i));
      }
    }
  }

  /**
   * Dzieli tekst po spacjach na linie nie dłuższe niż maxLength znaków.
   *
   * @param text      -- tekst do podziału
   * @param maxLength -- maksymalna długość linii
   * @return tekst podzielony na linie
   */
  public List<String> splitIntoLines(String text, final int maxLength) {
    if (text == null) {
      text = "brak opisu";
    }
    final List<String> lines = new ArrayList<>();
    int i = -1;
    int lineStarts = 0;
    int lastPossibleLineEnd = 0;
    while (++i < text.length()) {
      if (i - lineStarts >= maxLength) {
        // Trzeba uciąc obecny tekst
        if (lastPossibleLineEnd == lineStarts) {
          System.out.println("cut");
        } else {
          lines.add(text.substring(lineStarts, lastPossibleLineEnd));
          lineStarts = lastPossibleLineEnd + 1;
          lastPossibleLineEnd = lineStarts;
        }
      }
      if (text.charAt(i) == ' ') {
        lastPossibleLineEnd = i;
      }
    }
    if (lineStarts < i) {
      lines.add(text.substring(lineStarts, i));
    }
    return lines;
  }

}
