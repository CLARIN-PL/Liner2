package liner2.tools;

import java.util.Hashtable;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.structure.AttributeIndex;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;

import liner2.Main;

public class CorpusFactory {
	
	Hashtable<String, ParagraphSet> corpora = new Hashtable<String, ParagraphSet>();
	
	private static final CorpusFactory corpusFactory = new CorpusFactory();
	
	public static CorpusFactory get() {
		return corpusFactory;
	}
	
	public void parse(String description) throws Exception {
		String[] desc = description.split(":");
		if (desc.length != 3)
			throw new Exception("Invalid corpus description: " + description);
		if (corpora.containsKey(desc[0]))
			throw new Exception("Duplicate orpus definition: " + desc[0]);
		StreamReader reader = ReaderFactory.get().getStreamReader(desc[2], desc[1]);
		if (reader == null)
			throw new Exception("Error while parsing corpus description: " + description);
		ParagraphSet paragraphSet = reader.readParagraphSet();
		corpora.put(desc[0], paragraphSet);
	}
	
	/*
	 * Get a corpus defined by queryString.
	   @param queryString Corpus name or multiple names separated by commas to get a union of corpora.
	   @return Corpus specified by queryString.
	 */
	
	public ParagraphSet query(String queryString) {
		String[] names = queryString.split(",");
		ParagraphSet paragraphSet = corpora.get(names[0]);
		AttributeIndex attributeIndex = paragraphSet.getAttributeIndex();
		for (int i = 1; i < names.length; i++) {
			ParagraphSet next = corpora.get(names[i]);
			if (next.getAttributeIndex().equals(attributeIndex))
				for (Paragraph p : next.getParagraphs())
					paragraphSet.addParagraph(p);
			else {
				Main.log("Incompatible attribute indexes between " + names[0] + " and " + names[i] + ".");
				Main.log("Skipping " + names[i] + "...");
			}
		}
		return paragraphSet;
	}
}
