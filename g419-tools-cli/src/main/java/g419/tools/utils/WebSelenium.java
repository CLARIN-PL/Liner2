package g419.tools.utils;

import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebSelenium implements IWeb {

	private WebDriver driver = null;
	
	public WebSelenium() {
		this.driver = new FirefoxDriver();
	}
	
	public Document get(String url) {		
		return JsoupWrapped.getWithSelenium(driver, url);
	}

	@Override
	public void close() {
		this.driver.close();
	}
	
	public WebDriver getDriver(){
		return this.driver;
	}
}
