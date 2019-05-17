package g419.corpus.io.writer;

import g419.corpus.structure.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;


/*
 * Drukowanie wyników w postaci obiektu JSON.
 * @author Jan Kocoń
 */
public class JsonAnnotationsStreamWriter extends AbstractDocumentWriter {
  private final BufferedWriter ow;
  private int sentenceOffset = 0;

  public JsonAnnotationsStreamWriter(final OutputStream os) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  private JSONObject getChunk(final Annotation c, final Sentence s) throws IOException {
    int begin = sentenceOffset;
    int end = sentenceOffset;
    final JSONObject o = new JSONObject();

    final List<Token> tokens = s.getTokens();
    for (int i = 0; i < c.getBegin(); i++) {
      begin += tokens.get(i).getOrth().length();
    }
    end = begin;
    for (int i = c.getBegin(); i <= c.getEnd(); i++) {
      end += tokens.get(i).getOrth().length();
    }
    o.put("from", begin);
    o.put("to", (end - 1));
    o.put("type", c.getType());
    o.put("text", c.getText());

    //this.ow.write("(" + begin + "," + (end-1) + "," + c.getType() + ",\"" + c.getText() + "\")");
    if (!c.getMetadata().isEmpty()) {
      final JSONObject o1 = new JSONObject();
      for (final Map.Entry<String, String> entry : c.getMetadata().entrySet()) {
        o1.put(entry.getKey(), entry.getValue());
      }
      o.put("metadata", o1);
    }
    //this.ow.write(o.toString());
    return o;
  }

  @Override
  public void writeDocument(final Document document) {
    final JSONArray a = new JSONArray();
    sentenceOffset = 0;
    for (final Paragraph paragraph : document.getParagraphs()) {
      for (final Sentence s : paragraph.getSentences()) {
        try {
          final Annotation[] chunks = Annotation.sortChunks(s.getChunks());
          for (final Annotation c : chunks) {
            a.put(getChunk(c, s));
          }

          for (final Token t : s.getTokens()) {
            sentenceOffset += t.getOrth().length();
          }

        } catch (final IOException ex) {
          ex.printStackTrace();
        }
      }
    }
    try {
      ow.write(a.toString());
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void flush() {
    try {
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      ow.flush();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }
}
