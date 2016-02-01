package g419.tools.action;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.tools.utils.IWeb;
import g419.tools.utils.JsoupWrapped;
import g419.tools.utils.WebSelenium;

public class ActionGoogleSearch extends Action {
	
	private final String OPTION_QUERY_LONG = "query";
	private final String OPTION_QUERY = "q";
	
	private String query = null;

	private String link = "https://www.google.pl/search?q=%s&start=%d";
	
	public ActionGoogleSearch() {
		super("google-search");
		this.setDescription("wyszukuje w Google dokumenty z określoną frazą");
        this.options.addOption(Option.builder(OPTION_QUERY).longOpt(OPTION_QUERY_LONG).hasArg().argName("phrase")
        		.desc("fraza do znalezienia").required().required().build());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.query = line.getOptionValue(OPTION_QUERY_LONG);
    }

	@Override
	public void run() throws Exception {
		String[] phrases = this.query.toLowerCase().split("[+]");
		
		IWeb web = new WebSelenium();
		boolean keepSearching = true;
		
		int page = 0;
		while ( keepSearching ){
			String url = String.format(this.link, this.query.replace(" ", "+"), page);
			
			org.jsoup.nodes.Document doc = web.get(url);
			Elements elements = doc.select("#search h3 a");
			keepSearching = doc.select(".navend a#pnnext").size() > 0;
			for ( Element e : elements ){
				String href = e.attr("href");
				
				List<String> snippets = this.findSnippetsOnPage(href, phrases);
				if ( snippets.size() > 0 ){
					System.out.println();					
					System.out.println("##==================");
					System.out.println("## URL: " + href);
					for ( String snippet : snippets ){
						System.out.println();
						System.out.println("##------------------");					
						System.out.println(snippet);
					}
				}
			}
			page += 10;
		}
		
		web.close();
	}

	public List<String> findSnippetsOnPage(String url, String[] phrases){
		List<String> snippets = new ArrayList<String>();
		org.jsoup.nodes.Document doc = JsoupWrapped.get(url);
		if ( doc != null ){
			for ( Element e : doc.select("div, p, li") ){
				if ( e.select("* > div, * > p, * > li, table").size() == 0
						&& this.isNavBar(e.text()) == false ){
					String textLower = e.text().toLowerCase();
					boolean found = true;
					for ( String word : phrases ){
						if ( !textLower.contains(word) ){
							found = false;
						}
					}
					if ( found ){
						snippets.add(e.text().trim());
					}
				}
			}
		}
		return snippets;
	}

	/**
	 * Metoda sprawdza, czy dany fragment tekstu może być tekstem z paska nawigacji.
	 * @param content
	 * @return
	 */
	public boolean isNavBar(String content){
		boolean longText = false;
		for (String chunk : content.split("[»›]") ){
			longText = longText || chunk.trim().length() > 30;
		}
		return !longText;
	}
}
