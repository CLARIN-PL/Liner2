package g419.liner2.daemon.utils;

/**
 * Class for storing requests taken from database.
 *
 * @author Maciej Janicki
 */
public class Request {
    int id;
    String inputFormat;
    String outputFormat;
    String text;
    String modelName;
    int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;

    public Request(final int id, final String inputFormat, final String outputFormat, final String text, final String modelName) {
        this.id = id;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.text = text;
        this.modelName = modelName.toLowerCase();
    }

    public int getId() {
        return id;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getText() {
        return text;
    }

    public String getModelName() {
        return modelName;
    }

    // getGlobal stats
    public int getNumTokens() {
        return numTokens;
    }

    public int getNumSentences() {
        return numSentences;
    }

    public int getNumParagraphs() {
        return numParagraphs;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public void setStats(final int numTokens, final int numSentences, final int numParagraphs, final int numChunks) {
        this.numTokens = numTokens;
        this.numSentences = numSentences;
        this.numParagraphs = numParagraphs;
        this.numChunks = numChunks;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
