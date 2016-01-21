package g419.tools.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.tools.utils.JsoupWrapped;

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
//		int page = 0;
//		while ( page < 100 ){
//			String url = String.format(this.link, this.query.replace(" ", "+"), page);
//			
//			org.jsoup.nodes.Document doc = JsoupWrapped.get(url);
//			for ( Element e : doc.select("#search h3 a")){
//				String href = e.attr("href");			
//				System.out.println(href);
//			}
//			page += 10;
//		}
		
		String url = "http://www.andrzej-dabrowski.reaktywni.pl/SwiatDysku.html";
		org.jsoup.nodes.Document doc = JsoupWrapped.get(url);
		
		String[] words = this.query.toLowerCase().split("[+]");
		System.out.println(String.join(", ", words));
		
		for ( Element e : doc.select("div, p, li") ){
			if ( e.select("* > div, * > p, * > li").size() == 0 ){
				String textLower = e.text().toLowerCase();
				boolean found = true;
				for ( String word : words ){
					if ( !textLower.contains(word) ){
						found = false;
					}
				}
				if ( found ){
					System.out.println(e.text());
					System.out.println();
				}
			}
			
		}
	}
		
}
