package g419.corpus;

public class EmptySentenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmptySentenceException(String message){
        super(message);
    }
}
