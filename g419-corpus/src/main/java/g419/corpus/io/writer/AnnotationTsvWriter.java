package g419.corpus.io.writer;

import com.google.common.collect.Lists;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Token;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AnnotationTsvWriter extends AbstractDocumentWriter {

  private final CSVPrinter writer;
  private int sentenceOffset = 0;

  public AnnotationTsvWriter(final OutputStream os) throws IOException {
    writer = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.TDF);
  }

  @Override
  public void close() {
    flush();
    try {
      writer.close();
    } catch (final IOException ex) {
      getLogger().error("Failed to close AnnotationTsvWriter", ex);
    }
  }

  @Override
  public void writeDocument(final Document document) {
    sentenceOffset = 0;
    document.getAnnotations().forEach(a -> writeAnnotation(document, a));
    flush();
  }

//  public void writeParagraph(final Document doc, final Paragraph paragraph) {
//    paragraph.getSentences()
//        .forEach(s -> writeSentence(doc, s));
//  }
//
//  private void writeSentence(final Document doc, final Sentence sentence) {
//    Arrays.stream(Annotation.sortChunks(sentence.getChunks()))
//        .forEach(a -> writeAnnotation(doc, a));
//    sentenceOffset += sentence.getTokens().stream().mapToInt(t -> t.getOrth().length()).sum();
//  }

  private void writeAnnotation(final Document doc, final Annotation an) {
    try {
      final List<String> cols = Lists.newArrayList(doc.getName());
      cols.addAll(getAnnotationFields(an));
      writer.printRecord(cols);
    } catch (final IOException ex) {
      getLogger().error("Failed to writeAnnotation", ex);
    }
  }

  private List<String> getAnnotationFields(final Annotation c) {
    final List<String> cols = Lists.newArrayList();
    final List<Token> tokens = c.getSentence().getTokens();
    final int begin = sentenceOffset + IntStream.range(0, c.getBegin()).map(i -> tokens.get(i).getOrth().length()).sum();
    final int end = begin + IntStream.rangeClosed(c.getBegin(), c.getEnd()).map(i -> tokens.get(i).getOrth().length()).sum() - 1;
    cols.add("" + begin);
    cols.add("" + end);
    cols.add(c.getText());
    cols.add(c.getBaseText());
    cols.add(c.getLemma());
    cols.add(c.getType());
    return cols;
  }

  @Override
  public void flush() {
    try {
      writer.flush();
    } catch (final IOException ex) {
      getLogger().error("Failed to flush AnnotationTsvWriter", ex);
    }
  }
}
