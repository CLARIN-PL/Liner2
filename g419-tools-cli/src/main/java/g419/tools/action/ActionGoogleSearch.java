package g419.tools.action;

import g419.lib.cli.Action;
import g419.tools.utils.IWeb;
import g419.tools.utils.JsoupWrapped;
import g419.tools.utils.WebSelenium;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionGoogleSearch extends Action {

  private final String OPTION_QUERY_LONG = "query";
  private final String OPTION_QUERY = "q";
  private final String OPTION_WORKDIR_LONG = "workdir";
  private final String OPTION_WORKDIR = "w";
  private final String OPTION_PHRASE_LONG = "phrase";
  private final String OPTION_PHRASE = "p";

  private String query = null;
  private String workdir = null;
  private final Set<String> phrases = new HashSet<>();

  private final String link = "https://www.google.pl/search?q=%s&start=%d&filter=0";

  public ActionGoogleSearch() {
    super("google-search");
    setDescription("wyszukuje w Google dokumenty z określoną frazą");
    options.addOption(Option.builder(OPTION_QUERY).longOpt(OPTION_QUERY_LONG).hasArg().argName("phrase")
        .desc("fraza do znalezienia").required().required().build());
    options.addOption(Option.builder(OPTION_WORKDIR).longOpt(OPTION_WORKDIR_LONG).hasArg().argName("path")
        .desc("ścieżka do katalogu roboczego").required().build());
    options.addOption(Option.builder(OPTION_PHRASE).longOpt(OPTION_PHRASE_LONG).hasArg().argName("path")
        .desc("wymagane frazy w treści").required().build());
  }

  /**
   * Parse action options
   *
   * @param line The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    query = line.getOptionValue(OPTION_QUERY_LONG);
    workdir = line.getOptionValue(OPTION_WORKDIR_LONG);

    String phrases = line.getOptionValue(OPTION_PHRASE);
    if (phrases == null) {
      phrases = "";
    } else {
      phrases = phrases.replaceAll("_", " ").toLowerCase();
    }
    for (final String phrase : phrases.split(",")) {
      this.phrases.add(phrase.trim());
    }
  }

  @Override
  public void run() throws Exception {
    File workdirSource = null;
    File workdirText = null;
    if (workdir != null) {
      final File workdir = new File(this.workdir);
      if (!workdir.exists()) {
        workdir.mkdirs();
      }
      workdirSource = new File(workdir, "source");
      if (!workdirSource.exists()) {
        workdirSource.mkdirs();
      }
      workdirText = new File(workdir, "text");
      if (!workdirText.exists()) {
        workdirText.mkdirs();
      }
    }

    final String[] phrases = query.toLowerCase().split("[+]");

    final IWeb web = new WebSelenium();
    boolean keepSearching = true;

    int page = 0;
    while (keepSearching) {
      String query = this.query.replace(" ", "+");
      query = "\"" + query + "\"";
      final String url = String.format(link, query, page);

      final org.jsoup.nodes.Document doc = web.get(url);
      final Elements elements = doc.select("#search h3 a");
      keepSearching = doc.select(".navend a#pnnext").size() > 0;
      for (final Element e : elements) {
        String href = e.attr("href");
        if (href.startsWith("/")) {
          href = "https://www.google.pl" + href;
        }

        final Document docPage = JsoupWrapped.get(href);
        String pagePath = href.replaceAll("[^a-zA-Z0-9-]", "_");
        if (pagePath.length() > 240) {
          pagePath = pagePath.substring(0, 240);
        }
        if (workdirSource != null && docPage != null) {
          final File pageFolder = new File(workdirSource, pagePath);
          if (!pageFolder.exists()) {
            pageFolder.mkdirs();
          }
          FileUtils.writeStringToFile(new File(pageFolder, "index.html"), docPage.html(), "utf8");
        }

        final List<String> snippets = findSnippetsOnPage(docPage, this.phrases);
        if (snippets.size() > 0) {
          System.out.println();
          System.out.println("##==================");
          System.out.println("## URL: " + href);
          for (final String snippet : snippets) {
            System.out.println();
            System.out.println("##------------------");
            System.out.println(snippet);
            System.out.println("##------------------");
          }

          final String content = String.join("\n\n", snippets);
          final StringBuilder sb = new StringBuilder("[metadata]\n");
          sb.append("source = " + href);

          FileUtils.writeStringToFile(
              new File(workdirText, pagePath + ".txt"), content, "utf8");
          FileUtils.writeStringToFile(
              new File(workdirText, pagePath + ".ini"), sb.toString(), "utf8");
        }
      }
      page += 10;
    }

    web.close();
  }

  /**
   * Zwraca listę bloków tekstu wyciągniętych ze strony HTML
   *
   * @param doc Obiekt reprezentujący stronę HTML, z której mają zostać wyciągnięte bloki tekstu
   * @return
   */
  public List<String> getTextBlocsk(final Document doc) {
    final List<String> blocks = new ArrayList<>();
    if (doc != null) {
      for (final Element e : doc.select("div, p, li")) {
        if (e.select("* > div, * > p, * > li, table").size() == 0
            && isNavBar(e.text()) == false) {

          blocks.add(e.text());
        }
      }
    }
    return blocks;
  }

  /**
   * @param phrases
   * @return
   */
  public List<String> findSnippetsOnPage(final Document doc, final Set<String> phrases) {
    final List<String> snippets = new ArrayList<>();
    for (final String block : getTextBlocsk(doc)) {
      boolean found = false;
      final String textLower = block.toLowerCase();
      for (final String word : phrases) {
        if (textLower.contains(word)) {
          found = true;
          break;
        }
      }
      if (found) {
        snippets.add(block.trim());
      }
    }
    return snippets;
  }

  /**
   * Sprawdza, czy dany fragment tekstu może być tekstem z paska nawigacji.
   *
   * @param content
   * @return
   */
  public boolean isNavBar(final String content) {
    boolean longText = false;
    for (final String chunk : content.split("[»›]")) {
      longText = longText || chunk.trim().length() > 30;
    }
    return !longText;
  }
}
