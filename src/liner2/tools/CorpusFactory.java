package liner2.tools;

import java.util.Hashtable;

import liner2.structure.ParagraphSet;

public class CorpusFactory {
	
	Hashtable<String, ParagraphSet> corpora = new Hashtable<String, ParagraphSet>();
	
	private static final CorpusFactory corpusFactory = new CorpusFactory();
	
	public static CorpusFactory get() {
		return corpusFactory;
	}
	
	public ParagraphSet get(String name) {
		return this.corpora.get(name);
	}
	
	public void parse(String description) {
	}
}
