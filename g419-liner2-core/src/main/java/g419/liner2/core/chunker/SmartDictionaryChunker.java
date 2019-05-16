package g419.liner2.core.chunker;

import g419.corpus.structure.*;
import g419.liner2.core.tools.TrieDictNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Chunker rozpoznaje frazy w oparciu o słownik fraz.
 *
 * @author Michał Marcińczuk
 */
public class SmartDictionaryChunker extends Chunker {

  /* Słownik fraz */
  private TrieDictNode trieDict = null;

  /* Kategoria anotacji przypisana frazom znalezionym w tekście obecnych w słowniku */
  private String annotationName = null;

  /**
   * @param node           słownik fraz do oznaczenia
   * @param annotationName typ anotacji przypisany rozpoznanym frazom
   */
  public SmartDictionaryChunker(TrieDictNode node, String annotationName) {
    this.trieDict = node;
    this.annotationName = annotationName;
  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document document) {
    HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
    for (Paragraph paragraph : document.getParagraphs()) {
      for (Sentence sentence : paragraph.getSentences()) {
        chunkings.put(sentence, this.chunkSentence(sentence));
      }
    }
    return chunkings;
  }

  /**
   * @param sentence
   * @return
   */
  private AnnotationSet chunkSentence(Sentence sentence) {
    AnnotationSet chunking = new AnnotationSet(sentence);
    List<Annotation> foundAnnotations = new LinkedList<Annotation>();
    int i = 0;

    while (i < sentence.getTokenNumber()) {
      int matchLength = this.match(sentence.getTokens(), i);
      if (matchLength > 1) {
        Annotation an = new Annotation(i, i + matchLength - 1, this.annotationName, sentence);
        foundAnnotations.add(an);
        i = an.getTokens().last();
      }
      i++;
    }

    for (Annotation an : foundAnnotations) {
      chunking.addChunk(an);
    }

    return chunking;
  }

  /**
   * Funkcja sprawdza, czy sekwencja tokenów zaczynająca się od indeksu index znajduje się w słowniku.
   *
   * @param tokens
   * @param index
   * @return
   */
  private int match(List<Token> tokens, int index) {
    TrieDictNode currentNode = this.trieDict;
    int longestMatch = 0;
    int offset = 0;
    while (currentNode != null && index + offset < tokens.size()) {
      String word = tokens.get(index + (offset++)).getOrth();
      TrieDictNode nextNode = currentNode.getChild(word);

      if (nextNode != null && nextNode.isTerminal()) {
        longestMatch = offset;
      }
      currentNode = nextNode;
    }
    return longestMatch;
  }


  /**
   * Kompiluje inteligenty słownik na podstawie dostarczonych danych. Do kompilowania słownika potrzebne są:
   * <p>
   * a) plik z frazami podzielonymi na segmenty --- w pliku występują dwie rodzaje linii. Linie zaczynające się
   * od znaku # rozpoczynają nowy wpisa. Po znaku # występuje sekwencja tokenów tworząca kategorię frazy
   * (segmenty rozdzielone znakiem _) oraz sekwencja tokenów tworzących frazę. Linie, które nie zaczynają się od #
   * są kontynuacją poprzedniej linii.
   * <p>
   * b)
   *
   * @param filename
   * @throws IOException
   */
  public static SmartDictionaryChunker compile(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filename));
    String current = "";
    lines.add("#"); // Dodaj sztuczny element, który wymusi przetworzenie ostatniego elementu z listy
    TrieDictNode dictionary = new TrieDictNode();
    for (String line : lines) {
      line = line.trim();
      if (line.length() == 0) {
        continue;
      }

      if (line.startsWith("#")) {
        // Nowy element
        if (current.length() > 0) {
          // Sparsuj poprzedni element
          String[] cols = current.split(" ");
          StringBuilder category = new StringBuilder();
          int i = 1;
          category.append(cols[i]);
          while (i + 2 < cols.length && "_".equals(cols[i + 1])) {
            i += 2;
            category.append("_");
            category.append(cols[i]);
          }
          List<String> entry = new ArrayList<String>();
          while (i + 1 < cols.length) {
            entry.add(cols[++i]);
          }
          if (entry.size() > 1) {
            dictionary.addPhrase(entry.toArray(new String[entry.size()]));
          }
        }
        current = line;
      } else {
        // Kontynuacja poprzedniej linii
        current += " " + line;
      }
    }
    return new SmartDictionaryChunker(dictionary, "nam");
  }
}
