package g419.tools.utils;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WikinewsExtractor implements PageCallbackHandler {

  String outputFolder = null;
  String tresc;
  FileWriter fw = null;
  File file = null;

  public static void extract(String wikinewsDump, String outputFolder) throws Exception {
    WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(wikinewsDump);
    wxsp.setPageCallback(new WikinewsExtractor(outputFolder));
    wxsp.parse();
  }

  public WikinewsExtractor(String outputFolder) {
    this.outputFolder = outputFolder;
  }

  @Override
  public void process(WikiPage page) {

    if (!(page.isRedirect()) && !(page.getText().contains("#PATRZ")) && !(page.getText().contains("#REDIRECT"))
        && !(page.getTitle().contains("Kategoria:")) && !(page.getTitle().contains("Szablon:"))
        && !(page.getTitle().contains("MediaWiki:")) && !(page.getTitle().contains("Plik:"))
        && !(page.getTitle().contains("Wikinews:")) && !(page.getTitle().contains("Portal:"))
        && !(page.getID().equals("929"))) //str główna
    {
      String name = page.getTitle().replaceAll("\\n    ", "");
      name = name.replaceAll(("[ /\\?:*\"><|.]"), "_");
      name = name.replaceAll("\\d{4}-\\d{2}-\\d{2}__", "");

      name = String.format("%s_%s", page.getID(), name);
      name = name.replaceAll("[^a-zA-Z0-9ążśźęćńółĄŻŚŹĘĆŃÓŁ-]", "_");
      Logger.getLogger(this.getClass()).info(name);

      try {
        file = new File(this.outputFolder, name + ".txt");
        if (!file.exists()) {
          file.createNewFile();
        }
        fw = new FileWriter(file);
        fw.write(page.getTitle());
        fw.write("\n\n");
        fw.write(this.getContent(page));
        fw.flush();
        fw.close();
      } catch (IOException e) {
        Logger.getLogger(this.getClass()).error("Bład zapisu pliku", e);
      }
    }
  }

  public String getContent(WikiPage page) {
    String content = page.getWikiText();
    content = content.replaceAll("&ndash;", "–").replaceAll("\\[\\[\\:.*\\]\\]", "");
    content = content.replaceAll("\\{{2}.*\\}{2}|(?s)<gallery.*gallery\\>", "");
    content = content.replaceAll("\\<.*\\>|\\'{3,}|\\:{2,}|\\*{2,}"
        + "|\\[{2}Image\\:.*\\]{2}|\\[{2}Grafika\\:.*\\]{2}|\\[{2}Plik\\:.*\\]{2}"
        + "|\\[{2}Media\\:.*\\]{2}|(?s)\\[{2}Kategoria\\:.*"
        + "|\\[{2}.*?\\||\\[{2}|\\]{2}|__.*__|Wikipedia\\:"
        + "| {3,}|(?s)==*Źr.*|(?s)==* Źr.*|(?s)==*Multim.*|(?s)==* Multim.*|(?s)==*Czyt.*|(?s)==* Czyt.*"
        + "|(?s)==*Arch.*|(?s)==* Arch.*|(?s)==*Przyk.*|(?s)==* Przyk.*|(?s)==*Link.*|(?s)==* Link.*"
        + "|(?s)==*Zobacz.*|(?s)==* Zobacz.*|\\'{2}|\\[http.*? |\\]|==*.*?==*|(?s)\\{\\|.*?\\|\\}", "");
    content = content.replaceAll("\\[\\.\\.\\.", "\\[\\.\\.\\.]");
    content = content.replaceAll("\n{2,}", "\n");
    content = content.replaceAll("\n", "\r\n\r\n"); //przerwy między akapitami
    return content;
  }

}
