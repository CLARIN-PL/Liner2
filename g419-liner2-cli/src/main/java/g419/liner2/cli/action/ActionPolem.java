package g419.liner2.cli.action;

import g419.lib.cli.Action;
import g419.liner2.core.tools.PolemLemmatizer;
import org.apache.commons.cli.CommandLine;

public class ActionPolem extends Action {

  public ActionPolem() {
    super("polem");
    this.setDescription("test lemmatization module powered by Polem");
  }

  @Override
  public void parseOptions(CommandLine line) throws Exception {

  }

  @Override
  public void run() throws Exception {
    PolemLemmatizer polem = new PolemLemmatizer();
    System.out.println(polem.lemmatize("Rady Ministrów", "rada minister", "subst:sg:gen:f subst:pl:gen:m1", false));
  }

}
