package g419.corpus.io;

public class DataFormatException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 4581L;

    public DataFormatException(final String message) {
        super(message);
    }

    public DataFormatException(final String message, final Exception e) {
        super(message, e);
    }
}
