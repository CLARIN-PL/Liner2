package g419.tools.action;

import com.google.common.io.Files;
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
import java.nio.charset.Charset;
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
	private Set<String> phrases = new HashSet<String>();

	private String link = "https://www.google.pl/search?q=%s&start=%d&filter=0";
	
	public ActionGoogleSearch() {
		super("google-search");
		this.setDescription("wyszukuje w Google dokumenty z określoną frazą");
        this.options.addOption(Option.builder(OPTION_QUERY).longOpt(OPTION_QUERY_LONG).hasArg().argName("phrase")
        		.desc("fraza do znalezienia").required().required().build());
        this.options.addOption(Option.builder(OPTION_WORKDIR).longOpt(OPTION_WORKDIR_LONG).hasArg().argName("path")
        		.desc("ścieżka do katalogu roboczego").required().build());
		this.options.addOption(Option.builder(OPTION_PHRASE).longOpt(OPTION_PHRASE_LONG).hasArg().argName("path")
				.desc("wymagane frazy w treści").required().build());	}
	
	/**
	 * Parse action options
	 * @param line The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.query = line.getOptionValue(OPTION_QUERY_LONG);
        this.workdir = line.getOptionValue(OPTION_WORKDIR_LONG);
        
        String phrases = line.getOptionValue(OPTION_PHRASE);
        if ( phrases == null ){
        	phrases = "";
        }
        else{
        	phrases = phrases.replaceAll("_", " ").toLowerCase();
        }
        for ( String phrase : phrases.split(",")){
        	this.phrases.add(phrase.trim());
        }
    }

	@Override
	public void run() throws Exception {
		File workdirSource = null;
		File workdirText = null;
		if ( this.workdir != null ){
			File workdir = new File(this.workdir);
			if ( !workdir.exists() ){
				workdir.mkdirs();
			}
			workdirSource = new File(workdir, "source");
			if ( !workdirSource.exists() ){
				workdirSource.mkdirs();
			}
			workdirText = new File(workdir, "text");
			if ( !workdirText.exists() ){
				workdirText.mkdirs();
			}
		}
		
		String[] phrases = this.query.toLowerCase().split("[+]");
		
		IWeb web = new WebSelenium();
		boolean keepSearching = true;
		
		int page = 0;
		while ( keepSearching ){
			String query = this.query.replace(" ", "+");
			query = "\"" + query + "\"";
			String url = String.format(this.link, query, page);
			
			org.jsoup.nodes.Document doc = web.get(url);
			Elements elements = doc.select("#search h3 a");
			keepSearching = doc.select(".navend a#pnnext").size() > 0;
			for ( Element e : elements ){
				String href = e.attr("href");
				if ( href.startsWith("/") ){
					href = "https://www.google.pl" + href;
				}
				
				Document docPage = JsoupWrapped.get(href);
				String pagePath = href.replaceAll("[^a-zA-Z0-9-]", "_");
				if ( pagePath.length() > 240 ){
					pagePath = pagePath.substring(0, 240);
				}
				if ( workdirSource != null && docPage != null ){
					File pageFolder = new File(workdirSource, pagePath);
					if ( !pageFolder.exists() ){
						pageFolder.mkdirs();
					}
					FileUtils.write(new File(pageFolder, "index.html"), docPage.html());
				}

				List<String> snippets = this.findSnippetsOnPage(docPage, this.phrases);
				if ( snippets.size() > 0 ){
					System.out.println();					
					System.out.println("##==================");
					System.out.println("## URL: " + href);
					for ( String snippet : snippets ){
						System.out.println();
						System.out.println("##------------------");					
						System.out.println(snippet);
						System.out.println("##------------------");					
					}
					
					String content = String.join("\n\n", snippets);
					StringBuilder sb = new StringBuilder("[metadata]\n");
					sb.append("source = " + href);
					
					Files.write(content, new File(workdirText, pagePath + ".txt"), Charset.forName("utf8"));
					Files.write(sb.toString(), new File(workdirText, pagePath + ".ini"), Charset.forName("utf8"));
					
				}
			}
			page += 10;
		}
		
		web.close();
	}
	
	/**
	 * Zwraca listę bloków tekstu wyciągniętych ze strony HTML
	 * @param doc Obiekt reprezentujący stronę HTML, z której mają zostać wyciągnięte bloki tekstu
	 * @return
	 */
	public List<String> getTextBlocsk(Document doc){
		List<String> blocks = new ArrayList<String>();
		if ( doc != null ){
			for ( Element e : doc.select("div, p, li") ){
				if ( e.select("* > div, * > p, * > li, table").size() == 0
						&& this.isNavBar(e.text()) == false ){
					
					blocks.add(e.text());
				}
			}
		}
		return blocks;
	}

	/**
	 * 
	 * @param phrases
	 * @return
	 */
	public List<String> findSnippetsOnPage(Document doc, Set<String> phrases){
		List<String> snippets = new ArrayList<String>();
		for ( String block : this.getTextBlocsk(doc) ) {
			boolean found = false;
			String textLower = block.toLowerCase();
			for ( String word : phrases ){
				if ( textLower.contains(word) ){
					found = true;
					break;
				}
			}
			if ( found ){
				snippets.add(block.trim());
			}
		}
		return snippets;
	}

	/**
	 * Sprawdza, czy dany fragment tekstu może być tekstem z paska nawigacji.
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
