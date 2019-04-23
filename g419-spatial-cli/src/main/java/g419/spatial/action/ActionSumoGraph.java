package g419.spatial.action;

import g419.lib.cli.Action;
import g419.toolbox.sumo.Sumo;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionSumoGraph extends Action {

  private String dotFile = "sumo.dot";
  private String pdfFile = "sumo.pdf";

  public ActionSumoGraph() {
    super("sumo-graph");
    this.setDescription("generates SUMO graph for given node");
  }

  /**
   * Parse action options
   *
   * @param arg0 The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
  }

  @Override
  public void run() throws Exception {
    Logger.getLogger(this.getClass()).info("Wczytuję SUMO ...");
    Sumo sumo = new Sumo();
    Logger.getLogger(this.getClass()).info("gotowe.");

    String line = null;
    System.out.print("Podaj nazwę konceptu: ");
    while (((line = System.console().readLine().trim())).length() > 0) {
      Set<String> edges = new HashSet<String>();
      this.printGraphUp(sumo, line.toLowerCase(), edges);
      this.printGraphDown(sumo, line.toLowerCase(), edges);
      List<String> graph = new ArrayList<String>();
      graph.add("digraph G {");
      graph.addAll(edges);
      graph.add("}");
      Files.write(Paths.get(this.dotFile), graph);
      Runtime.getRuntime().exec(String.format("dot -Tps %s -o %s", this.dotFile, this.pdfFile));
      System.out.println();
      System.out.print("Podaj nazwę konceptu: ");
    }


  }

  public void printGraphUp(Sumo sumo, String concept, Set<String> edges) {
    Set<String> nodes = sumo.getSuperclasses(concept);
    for (String node : nodes) {
      String line = String.format("  %s -> %s;", node, concept);
      edges.add(line);
      this.printGraphUp(sumo, node, edges);
    }
  }

  public void printGraphDown(Sumo sumo, String concept, Set<String> edges) {
    Set<String> nodes = sumo.getSubclasses(concept);
    for (String node : nodes) {
      String line = String.format("  %s -> %s;", concept, node);
      edges.add(line);
      this.printGraphDown(sumo, node, edges);
    }
  }

}
