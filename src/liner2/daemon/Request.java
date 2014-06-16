package liner2.daemon;

/**
 * Class for storing requests taken from database.
 * @author Maciej Janicki
 */
public class Request {
	int id;
	String inputFormat;
	String outputFormat;
	String text;
    String modelName;
	int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;	

	public Request(int id, String inputFormat, String outputFormat, String text, String modelName) {
		this.id = id;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.text = text;
        this.modelName = modelName.toLowerCase();
	}

	public int getId() { return this.id; }
	public String getInputFormat() { return this.inputFormat; }
	public String getOutputFormat() { return this.outputFormat; }
	public String getText() { return this.text; }
    public String getModelName() { return this.modelName; }

	// getGlobal stats
	public int getNumTokens() { return this.numTokens; }
	public int getNumSentences() { return this.numSentences; }
	public int getNumParagraphs() { return this.numParagraphs; }
	public int getNumChunks() { return this.numChunks; }

	public void setStats(int numTokens, int numSentences, int numParagraphs, int numChunks) {
		this.numTokens = numTokens;
		this.numSentences = numSentences;
		this.numParagraphs = numParagraphs;
		this.numChunks = numChunks;
	}

	public void setText(String text) {
		this.text = text;
	}
}
