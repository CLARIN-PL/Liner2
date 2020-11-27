package g419.serel.ruleTree.listeners;


import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ThrowingErrorListener extends BaseErrorListener {

  public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

  @Override
  public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e)
      throws ParseCancellationException {
    throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
  }
}
