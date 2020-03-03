package g419.corpus;

import com.google.common.collect.Maps;
import g419.corpus.structure.*;
import java.util.Map;

/**
 * Utility to fix missing element identifiers.
 * Handle paragraphs, sentences, tokens, relations and annotations.
 */
public class DocumentElementIdFixer {

  class IdGenerator {
    int value = 1;
    final String pattern;

    public IdGenerator(final String pattern) {
      this.pattern = pattern;
    }

    public String next() {
      return String.format(pattern, value++);
    }

    public void reset() {
      value = 1;
    }
  }

  final IdGenerator nextParagraphId = new IdGenerator("par-%03d");
  final IdGenerator nextSentenceId = new IdGenerator("sen-%03d");
  final IdGenerator nextTokenId = new IdGenerator("tok-%04d");
  final IdGenerator nextRelationId = new IdGenerator("rel-%03d");
  final IdGenerator nextAnnotationId = new IdGenerator("ann-%03d");
  final String defaultLexId = "morph_%d.%d.%d.%d-lex";

  Map<String, IdentifiableElement> usedIdentifiers = Maps.newHashMap();

  public void fixIds(final Document document) {
    collectExistingIds(document);
    assignMissingIds(document);
    reset();
  }

  private void reset() {
    nextParagraphId.reset();
    nextSentenceId.reset();
    nextTokenId.reset();
    nextRelationId.reset();
    nextAnnotationId.reset();
  }

  private void collectExistingIds(final Document document) {
    document.getParagraphs().stream().filter(p -> p.getId() != null)
        .forEach(p -> usedIdentifiers.put(p.getId(), p));
    document.getSentences().stream().filter(s -> s.getId() != null)
        .forEach(s -> usedIdentifiers.put(s.getId(), s));
    document.getSentences().stream().forEach(s -> s.getTokens().stream().filter(t -> t.getId() != null)
        .forEach(t -> usedIdentifiers.put(t.getId(), t)));
    document.getRelations().getRelations().stream().filter(r -> r.getId() != null)
        .forEach(r -> usedIdentifiers.put(r.getId(), r));
    document.getAnnotations().stream().filter(a -> a.getId() != null)
        .forEach(a -> usedIdentifiers.put(a.getId(), a));
  }

  private void assignMissingIds(final Document document) {
    document.getParagraphs().stream().filter(p -> p.getId() == null)
        .forEach(p -> assignNextAvailableIdentifier(nextParagraphId, p));
    document.getSentences().stream().filter(s -> s.getId() == null)
        .forEach(s -> assignNextAvailableIdentifier(nextSentenceId, s));
    document.getSentences().stream().forEach(s -> s.getTokens().stream().filter(t -> t.getId() == null)
        .forEach(t -> assignNextAvailableIdentifier(nextTokenId, t)));
    document.getRelations().getRelations().stream().filter(r -> r.getId() == null)
        .forEach(r -> assignNextAvailableIdentifier(nextRelationId, r));
    document.getAnnotations().stream().filter(a -> a.getId() == null)
        .forEach(a -> assignNextAvailableIdentifier(nextAnnotationId, a));
    assignMissingLexIds(document);
  }

  private void assignMissingLexIds(final Document document) {
    int pIndex = 0;
    int sIndex = 0;
    for (final Paragraph p : document.getParagraphs()) {
      pIndex++;
      for (final Sentence s : p.getSentences()) {
        sIndex++;
        int tIndex = 0;
        for (final Token t : s.getTokens()) {
          tIndex++;
          int tagIndex = 0;
          for (final Tag tag : t.getTags()) {
            tagIndex++;
            if (tag.getId() == null) {
              tag.setId(getLexTag(pIndex, sIndex, tIndex, tagIndex));
            }
          }
        }
      }
    }
  }

  private String getLexTag(final int paragraph, final int sentence, final int token, final int lex) {
    return String.format(defaultLexId, paragraph, sentence, token, lex);
  }

  private void assignNextAvailableIdentifier(final IdGenerator idGenerator, final IdentifiableElement object) {
    String id;
    do {
      id = idGenerator.next();
    } while (usedIdentifiers.containsKey(id));
    usedIdentifiers.put(id, object);
    object.setId(id);
  }

}
