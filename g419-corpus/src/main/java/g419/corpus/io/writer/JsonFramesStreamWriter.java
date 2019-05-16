package g419.corpus.io.writer;

import com.google.gson.Gson;
import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;


/**
 * @author
 */
public class JsonFramesStreamWriter extends AbstractDocumentWriter {
  private BufferedWriter ow = null;
  private OutputStream os = null;

  public JsonFramesStreamWriter(final OutputStream os) {
    this.os = os;
    ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void writeDocument(final Document document) {

    final Map<Token, String> tokenIds = generateTokenIds(document);
    final Map<Annotation, String> annotationIds = generateAnnotationIds(document);

    final Map<String, Collection<?>> doc = new HashMap<>();
    doc.put("tokens", getTokens(document, tokenIds));
    doc.put("frames", getFrames(document, annotationIds));
    doc.put("annotations", getAnnotations(document, tokenIds, annotationIds));
    try {
      final Gson json = new Gson();
      ow.write(json.toJson(doc));
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private List<Map<String, Object>> getFrames(final Document document, final Map<Annotation, String> annotationIds) {
    final List<Map<String, Object>> frames = new ArrayList<>();
    for (final Frame<Annotation> frame : document.getFrames()) {
      final Map<String, Object> f = new HashMap<>();
      final Map<String, Object> slots = new HashMap<>();

      final Set<String> slotNames = new HashSet<>();
      slotNames.addAll(frame.getSlots().keySet());
      slotNames.addAll(frame.getSlotAttributes().keySet());

      for (final String slotName : slotNames) {
        final Annotation an = frame.getSlot(slotName);
        final Map<String, Object> annotation = new HashMap<>();
        if (an != null) {
          annotation.put("id", annotationIds.get(an));
        }

        final Map<String, String> annotationAttributes = frame.getSlotAttributes(slotName);
        if (annotationAttributes != null) {
          annotation.put("attributes", annotationAttributes);
        }

        slots.put(slotName, annotation);
      }
      f.put("id", "x");
      f.put("type", frame.getType());
      f.put("slots", slots);
      frames.add(f);
    }
    return frames;
  }

  private List<Map<String, Object>> getAnnotations(final Document document, final Map<Token, String> tokenIds, final Map<Annotation, String> annotationIds) {
    final List<Map<String, Object>> annotations = new ArrayList<>();
    for (final Annotation an : document.getAnnotations()) {
      final Map<String, Object> f = new HashMap<>();
      f.put("id", annotationIds.get(an));
      f.put("text", an.getText());
      f.put("type", an.getType());
      f.put("category", an.getGroup());
      final Set<String> tokens = new HashSet<>();
      for (final Token token : an.getTokenTokens()) {
        tokens.add(tokenIds.get(token));
      }
      f.put("tokens", tokens);
      annotations.add(f);
    }
    return annotations;
  }

  private List<List<String>> getTokens(final Document document, final Map<Token, String> tokenIds) {
    final List<List<String>> tokens = new ArrayList<>();
    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          final List<String> token = new ArrayList<>();
          token.add(tokenIds.get(t));
          token.add(t.getOrth());
          token.add(t.getDisambTag().getBase());
          token.add(t.getDisambTag().getCtag());
          token.add(t.getNoSpaceAfter() ? "1" : "0");
          tokens.add(token);
        }
      }
    }
    return tokens;
  }

  private Map<Token, String> generateTokenIds(final Document document) {
    final Map<Token, String> ids = new HashMap<>();
    int id = 1;
    for (final Paragraph p : document.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          ids.put(t, "t" + (id++));
        }
      }
    }
    return ids;
  }

  private Map<Annotation, String> generateAnnotationIds(final Document document) {
    final Map<Annotation, String> ids = new HashMap<>();
    int id = 1;
    for (final Annotation an : document.getAnnotations()) {
      ids.put(an, "a" + (id++));
    }
    return ids;
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
      ow.close();
      //((GZIPOutputStream)this.os).finish();
      os.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }
}
