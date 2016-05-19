package g419.tools.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import g419.lib.cli.Action;
import g419.tools.utils.JsoupWrapped;
import g419.tools.utils.WebSelenium;

public class ActionCrawlPracuj extends Action {
	
	private String link = "http://oferty.praca.gov.pl/portal/index.cbop";
	private String nextSelector = "a.oferta-lista-stronicowanie-nastepna-strona";
	private String linkSelector = "a.oferta-pozycja-szczegoly-link"; 
	
	public ActionCrawlPracuj() {
		super("crawl-pup");
		this.setDescription("crawluje oferty pracy z PUP");
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
    }

	@Override
	public void run() throws Exception {
		WebSelenium web = new WebSelenium();
		boolean keepSearching = true;

		web.get(this.link);

		while ( keepSearching ){
			Thread.sleep(10000);
			List<WebElement> elements = web.getDriver().findElements(By.cssSelector(this.linkSelector));
			keepSearching = elements.size() > 0;
			for ( WebElement e : elements ){
				String href = e.getAttribute("href");
				System.out.println(href);
//				
//				List<String> snippets = this.findSnippetsOnPage(href, phrases);
//				if ( snippets.size() > 0 ){
//					System.out.println();					
//					System.out.println("##==================");
//					System.out.println("## URL: " + href);
//					for ( String snippet : snippets ){
//						System.out.println();
//						System.out.println("##------------------");					
//						System.out.println(snippet);
//					}
//				}
			}
			System.out.println("next...");
			web.getDriver().findElement(By.cssSelector(this.nextSelector)).click();
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
