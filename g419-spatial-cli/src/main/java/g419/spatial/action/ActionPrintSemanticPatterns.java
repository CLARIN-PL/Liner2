package g419.spatial.action;

import g419.lib.cli.Action;
import g419.spatial.io.CsvSpatialSchemeParser;
import g419.toolbox.sumo.Sumo;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import org.apache.commons.cli.CommandLine;

public class ActionPrintSemanticPatterns extends Action {

  public ActionPrintSemanticPatterns() {
    super("print-semantic-patterns");
    setDescription("print a list of default semantic patterns of spatial expressions");
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
  }

  @Override
  public void run() throws Exception {

    final String location = "/g419/spatial/resources/spatial_schemes.csv";
    InputStream resource = getClass().getResourceAsStream(location);

    if (resource == null) {
      throw new MissingResourceException("Resource not found: " + location,
          getClass().getName(), location);
    }

    System.out.println("=== General ===");
    (new CsvSpatialSchemeParser(new InputStreamReader(resource), new Sumo(false)))
        .parse();

    resource = getClass().getResourceAsStream(location);
    System.out.println("=== Prototype ===");
    (new CsvSpatialSchemeParser(new InputStreamReader(resource), new Sumo(false),
        false)).parse();
  }

}
