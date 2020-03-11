package g419.liner2.core.chunker;


import g419.corpus.ConsolePrinter;
import g419.corpus.structure.*;
import g419.liner2.core.chunker.interfaces.DeserializableChunkerInterface;
import g419.liner2.core.chunker.interfaces.SerializableChunkerInterface;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class FullDictionaryChunker extends Chunker
    implements DeserializableChunkerInterface, SerializableChunkerInterface {

  private HashMap<String, HashSet<String>> dictionary = null;

  public FullDictionaryChunker() {
  }

  private AnnotationSet chunkSentence(final Sentence sentence) {
    final AnnotationSet chunking = new AnnotationSet(sentence);
    final List<Token> tokens = sentence.getTokens();
    final int sentenceLength = sentence.getTokenNumber();

    // [długość] -> początek => n-gram
    final ArrayList<HashMap<Integer, String>> nGrams = new ArrayList<>();

    // wygeneruj unigramy
    nGrams.add(new HashMap<>());
    for (int i = 0; i < sentenceLength; i++) {
      nGrams.get(0).put(i, tokens.get(i).getOrth());
    }
    // wygeneruj n-gramy
    for (int n = 1; n < sentenceLength; n++) {
      nGrams.add(new HashMap<>());
      for (int j = 0; j < sentenceLength - n; j++) {
        nGrams.get(n).put(j,
            nGrams.get(n - 1).get(j) + " " + tokens.get(j + n).getOrth());
      }
    }

    // chunkuj (poczynając od najdłuższych n-gramów) - DO SPRAWDZENIA
    for (int n = sentenceLength - 1; n >= 0; n--) {
      for (int i = 0; i < sentenceLength - n; i++) {
        final int idx = i;

        // jeśli danego n-gramu nie ma w tablicy, to kontynuuj
        if (nGrams.get(n).get(idx) == null) {
          continue;
        }

        // jeśli znaleziono w słowniku
        if (dictionary.containsKey(nGrams.get(n).get(idx))) {

          // dodaj wszystkie chunki
          final HashSet<String> types = dictionary.get(nGrams.get(n).get(idx));
          for (final String type : types) {
            chunking.addChunk(new Annotation(i, i + n, type, sentence));
          }

          // usuń z tablicy wszystkie krótsze n-gramy, które zahaczają o to miejsce
          // j - dł. odrzucanego n-gramu - 1, k - pozycja startowa
          for (int j = n; j >= 0; j--) {
            for (int k = i - j; k <= i + n; k++) {
              nGrams.get(j).remove(k);
            }
          }
        }
      }
    }

    return chunking;
  }

  public void loadDictionary(final String dictFile) {
    dictionary = new HashMap<>();
    try {
      final BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
      String line = dictReader.readLine();
      int added = 0;
      while (line != null) {
        final String[] content = line.split("\t");
        if (content.length >= 2) {
          if (!dictionary.containsKey(content[1])) {
            dictionary.put(content[1], new HashSet<>());
          }
          if (!dictionary.get(content[1]).contains(content[0].toUpperCase())) {
            dictionary.get(content[1]).add(content[0].toUpperCase());
            added += 1;
          }
        }
        line = dictReader.readLine();
      }
      ConsolePrinter.log("Full dictionary chunker compiled.", true);
      ConsolePrinter.log("Added: " + added, true);
      dictReader.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Wczytuje chunker z modelu binarnego.
   *
   * @param filename
   */
  @Override
  @SuppressWarnings("unchecked")
  public void deserialize(final String filename) {
    try {
      final ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
      dictionary = (HashMap<String, HashSet<String>>) in.readObject();
      in.close();
    } catch (final ClassNotFoundException ex) {
      ex.printStackTrace();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void serialize(final String filename) {
    try {
      final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
      out.writeObject(dictionary);
      out.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() {

  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(final Document ps) {
    final HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();
    for (final Paragraph paragraph : ps.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        chunkings.put(sentence, chunkSentence(sentence));
      }
    }
    return chunkings;
  }

}	
