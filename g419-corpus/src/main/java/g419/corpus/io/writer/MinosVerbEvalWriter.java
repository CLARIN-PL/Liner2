package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MinosVerbEvalWriter extends AbstractDocumentWriter {
  private final BufferedWriter writer;

  public MinosVerbEvalWriter(final OutputStream os) {
    writer = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void writeDocument(final Document document) {
    for (final Sentence sentence : document.getSentences()) {
      for (final Annotation annotation : sentence.getChunks()) {
        writeAnnotation(annotation);
      }
    }
  }

  public void writeAnnotation(final Annotation annotation) {
    if ("wyznacznik_null_verb".equals(annotation.getType())) {
      try {
        writer.write("1\t" + annotation.getText() + "\n");
      } catch (final IOException e) {
        e.printStackTrace();
      }
    } else if ("wyznacznik_notnull_verb".equals(annotation.getType())) {
      try {
        writer.write("0\t" + annotation.getText() + "\n");
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public void flush() {
    try {
      writer.flush();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
