package g419.corpus.io.writer;

import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationTuplesWriter extends AbstractDocumentWriter {

  private final BufferedWriter writer;

  public RelationTuplesWriter(final OutputStream os) {
    writer = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void writeDocument(final Document document) {
    final Map<Sentence, List<Integer>> index = makeOffsetIndex(document);
    for (final Relation relation : document.getRelations().getRelations()) {
      try {
        final Annotation af = relation.getAnnotationFrom();
        final Annotation at = relation.getAnnotationTo();
        final String line = String.format("(%d,%d,%s,\"%s\",%d,%d,%s,\"%s\",%s)\n",
            index.get(af.getSentence()).get(af.getBegin()),
            index.get(af.getSentence()).get(af.getEnd() + 1) - 1,
            af.getType(), af.getText(),
            index.get(at.getSentence()).get(at.getBegin()),
            index.get(at.getSentence()).get(at.getEnd() + 1) - 1,
            at.getType(), at.getText(),
            relation.getType());
        writer.write(line);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param document
   * @return
   */
  private Map<Sentence, List<Integer>> makeOffsetIndex(final Document document) {
    final HashMap<Sentence, List<Integer>> index = new HashMap<>();
    int offset = 0;
    for (final Paragraph paragraph : document.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        final ArrayList<Integer> sentenceIndex = new ArrayList<>();
        for (final Token token : sentence.getTokens()) {
          sentenceIndex.add(offset);
          offset += token.getOrth().length();
        }
        sentenceIndex.add(offset);
        index.put(sentence, sentenceIndex);
      }
    }
    return index;
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
