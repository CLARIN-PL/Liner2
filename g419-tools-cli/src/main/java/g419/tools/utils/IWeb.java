package g419.tools.utils;

import org.jsoup.nodes.Document;

public interface IWeb {

	public Document get(String url);
	
	public void close();
	
}
