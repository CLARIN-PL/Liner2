package g419.spatial.action;

import g419.lib.cli.Action;
import g419.toolbox.sumo.Sumo;
import java.util.Set;
import org.apache.commons.cli.CommandLine;

public class ActionSumoSuperclasses extends Action {

  public ActionSumoSuperclasses() {
    super("sumo-superclasses");
    setDescription("print superclasses for a given concept");
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
  }

  @Override
  public void run() throws Exception {
    getLogger().info("Loading SUMO ...");
    final Sumo sumo = new Sumo();
    getLogger().info("Loaded {} concept(s)", sumo.getConcepts().size());

    String line;
    do {
      System.out.print("Concept: ");
      line = System.console().readLine().trim();
      if (line.length() > 0) {
        final Set<String> words = sumo.getSuperclasses(line.toLowerCase());
        System.out.println(String.format("Superclasses: %s\n", String.join(", ", words)));
      }
    } while (line.length() > 0);
  }

}
